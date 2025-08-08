# fastapi_rag_server.py - 실제 RAG 시스템 연동 FastAPI 서버
"""
🚀 ARGO RAG FastAPI 서버 - Ultra Rapid 버전 연동
실제 LLM 기반 고품질 퀴즈 생성

실행: python fastapi_rag_server.py
테스트: http://localhost:8000/docs
"""

from fastapi import FastAPI, HTTPException, BackgroundTasks
from pydantic import BaseModel, Field
import json
import logging
import asyncio
from typing import List, Optional, Dict
import uvicorn
import os
from datetime import datetime

# Ultra Rapid RAG 시스템 import
from ultra_rapid_spot_rag import (
    initialize_rag_system,
    generate_quiz_api,
    generate_batch_quiz_api,
    get_system_status_api,
    get_locations_api,
    create_base_quiz_database,
    load_quiz_database,
    get_random_quiz_for_spot
)

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# FastAPI 앱 생성
app = FastAPI(
    title="ARGO RAG Pipeline API",
    description="🎯 AR 기반 현장체험학습 퀴즈 생성 API (실제 LLM 연동)",
    version="2.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# === 요청/응답 모델 ===

class QuizRequest(BaseModel):
    """퀴즈 생성 요청"""
    location: str = Field(..., description="장소명 (예: 경복궁, 서울)")
    spot_name: Optional[str] = Field(None, description="구체적인 스팟명 (예: 근정전)")
    user_grade: int = Field(5, ge=1, le=6, description="학년 (1-6)")
    problems_count: int = Field(1, ge=1, le=10, description="생성할 문제 개수")

class BatchQuizRequest(BaseModel):
    """배치 퀴즈 생성 요청"""
    location: str = Field(..., description="지역명")
    grades: List[int] = Field([1,2,3,4,5,6], description="대상 학년들")
    max_spots: int = Field(10, ge=1, le=50, description="최대 스팟 개수")
    problems_per_spot: int = Field(3, ge=1, le=10, description="스팟당 문제 개수")

class ClassMissionRequest(BaseModel):
    """반 미션 DB 생성 요청"""
    class_name: str = Field(..., description="반 이름")
    location: str = Field(..., description="현장학습 장소")
    grade: int = Field(..., ge=1, le=6, description="학년")
    spot_names: List[str] = Field(..., description="미션 스팟들")
    problems_per_spot: int = Field(2, ge=1, le=5, description="스팟당 문제 개수")

class StudentQuizRequest(BaseModel):
    """학생 퀴즈 요청 (AR 오브젝트 클릭시)"""
    class_id: str = Field(..., description="반 ID") 
    spot_name: str = Field(..., description="현재 스팟명")
    team_id: str = Field(..., description="팀 ID")

# === 전역 변수 ===
rag_system = None
base_quiz_db = None
class_missions = {}  # 반별 미션 DB

# === 서버 시작 이벤트 ===

@app.on_event("startup")
async def startup_event():
    """서버 시작시 RAG 시스템 초기화"""
    global rag_system, base_quiz_db
    
    logger.info("🚀 ARGO RAG 서버 시작")
    
    try:
        # 1. RAG 시스템 초기화
        logger.info("⚡ RAG 시스템 초기화 중...")
        rag_system = await initialize_rag_system("heritage_complete_database.json")
        
        # 2. 기본 퀴즈 DB 로드 (있으면)
        if os.path.exists("base_quiz_database.json"):
            base_quiz_db = load_quiz_database("base_quiz_database.json")
            logger.info(f"📚 기본 퀴즈 DB 로드: {len(base_quiz_db.get('problems', []))}개")
        else:
            logger.info("💡 기본 퀴즈 DB 없음 - 필요시 /generate-base-db 호출")
        
        logger.info("✅ 서버 초기화 완료")
        
    except Exception as e:
        logger.error(f"❌ 서버 초기화 실패: {e}")
        logger.info("🔧 heritage_complete_database.json 파일을 확인하세요")

# === 기본 API 엔드포인트 ===

@app.get("/")
async def root():
    """루트 엔드포인트"""
    status = await get_system_status_api() if rag_system else {"initialized": False}
    
    return {
        "message": "🎯 ARGO RAG Pipeline API v2.0",
        "description": "실제 LLM 기반 퀴즈 생성 시스템",
        "status": "ready" if status.get("initialized") else "initializing",
        "features": [
            "🧠 GPT-4o-mini LLM 기반 생성",
            "🔍 Ko-SBERT 벡터 검색",
            "📚 학년별 맞춤 퀴즈 (1-6학년)",
            "🏭 대량 배치 생성",
            "👩‍🏫 교사용 반 미션 관리",
            "🎲 학생용 랜덤 퀴즈"
        ],
        "endpoints": {
            "docs": "/docs",
            "health": "/health", 
            "quiz": "/quiz/generate",
            "batch": "/quiz/batch",
            "class": "/class/create-missions"
        }
    }

@app.get("/health")
async def health_check():
    """상세 상태 확인"""
    if not rag_system:
        return {"status": "initializing", "message": "RAG 시스템 초기화 중..."}
    
    status = await get_system_status_api()
    locations = await get_locations_api(5)
    
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "rag_system": status,
        "base_quiz_db": {
            "loaded": base_quiz_db is not None,
            "problem_count": len(base_quiz_db.get('problems', [])) if base_quiz_db else 0
        },
        "class_missions": {
            "active_classes": len(class_missions),
            "total_problems": sum(len(missions.get('problems', [])) for missions in class_missions.values())
        },
        "sample_locations": locations.get('locations', [])[:5]
    }

# === 퀴즈 생성 API ===

@app.post("/quiz/generate")
async def generate_quiz_endpoint(request: QuizRequest):
    """실시간 퀴즈 생성 (LLM 기반)"""
    
    if not rag_system:
        raise HTTPException(status_code=503, detail="RAG 시스템이 초기화되지 않았습니다")
    
    logger.info(f"🎯 퀴즈 생성: {request.location} - {request.user_grade}학년")
    
    try:
        # RAG 시스템으로 퀴즈 생성
        result = await generate_quiz_api(request.dict())
        
        if not result['success']:
            raise HTTPException(status_code=400, detail=result.get('error', '퀴즈 생성 실패'))
        
        quiz_data = result['data']
        
        return {
            "success": True,
            "quiz": {
                "question": quiz_data['question'],
                "choices": quiz_data['choices'],
                "correct_index": quiz_data['correct_index'],
                "explanation": quiz_data.get('explanation'),
                "grade": quiz_data['grade'],
                "spot_name": quiz_data.get('spot_name'),
                "quality_score": quiz_data.get('quality_score', 0),
                "generation_time": quiz_data.get('generation_time', 0)
            },
            "metadata": {
                "method": "llm_generated",
                "timestamp": datetime.now().isoformat(),
                "location": request.location
            }
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ 퀴즈 생성 오류: {e}")
        raise HTTPException(status_code=500, detail=f"퀴즈 생성 실패: {str(e)}")

@app.post("/quiz/batch")
async def generate_batch_quiz_endpoint(request: BatchQuizRequest):
    """대량 퀴즈 생성 (배치 처리)"""
    
    if not rag_system:
        raise HTTPException(status_code=503, detail="RAG 시스템이 초기화되지 않았습니다")
    
    logger.info(f"🏭 배치 생성: {request.location} - {len(request.grades)}학년, {request.max_spots}스팟")
    
    try:
        # 배치 생성 실행 (백그라운드에서 처리)
        result = await generate_batch_quiz_api(request.dict())
        
        if not result['success']:
            raise HTTPException(status_code=400, detail=result.get('error', '배치 생성 실패'))
        
        batch_data = result['data']
        
        return {
            "success": True,
            "batch_result": {
                "total_requested": batch_data['total_requested'],
                "total_generated": batch_data['total_generated'],
                "success_rate": batch_data['success_rate'],
                "problems": batch_data['problems'][:100]  # 응답 크기 제한 (처음 100개만)
            },
            "summary": {
                "locations_processed": 1,
                "grades_processed": len(request.grades),
                "average_quality": sum(p.get('quality_score', 0) for p in batch_data['problems']) / len(batch_data['problems']) if batch_data['problems'] else 0,
                "generation_time": result.get('metadata', {}).get('generation_time', 0)
            },
            "timestamp": datetime.now().isoformat()
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ 배치 생성 오류: {e}")
        raise HTTPException(status_code=500, detail=f"배치 생성 실패: {str(e)}")

# === 교사용 반 미션 관리 API ===

@app.post("/class/create-missions")
async def create_class_missions(request: ClassMissionRequest):
    """반 미션 DB 생성 (교사 요청시)"""
    
    global class_missions
    
    logger.info(f"👩‍🏫 반 미션 생성: {request.class_name} - {request.location}")
    
    try:
        class_problems = []
        
        # 각 스팟별로 문제 생성
        for spot_name in request.spot_names:
            logger.info(f"   📍 {spot_name} 문제 생성 중...")
            
            for i in range(request.problems_per_spot):
                # 1. 기본 DB에서 먼저 찾기
                if base_quiz_db:
                    cached_quiz = get_random_quiz_for_spot(
                        "base_quiz_database.json",
                        request.location,
                        spot_name,
                        request.grade
                    )
                    
                    if cached_quiz:
                        cached_quiz['problem_id'] = f"{request.class_name}_{spot_name}_{i+1}"
                        cached_quiz['source'] = 'cached'
                        class_problems.append(cached_quiz)
                        continue
                
                # 2. 기본 DB에 없으면 실시간 생성
                if rag_system:
                    quiz_request = {
                        "location": request.location,
                        "spot_name": spot_name,
                        "user_grade": request.grade
                    }
                    
                    result = await generate_quiz_api(quiz_request)
                    
                    if result['success']:
                        quiz_data = result['data']
                        quiz_data['problem_id'] = f"{request.class_name}_{spot_name}_{i+1}"
                        quiz_data['source'] = 'generated'
                        class_problems.append(quiz_data)
        
        # 반 미션 DB 저장
        class_missions[request.class_name] = {
            "class_name": request.class_name,
            "location": request.location,
            "grade": request.grade,
            "spots": request.spot_names,
            "problems": class_problems,
            "created_at": datetime.now().isoformat(),
            "total_problems": len(class_problems)
        }
        
        # 스팟별 통계
        spot_stats = {}
        for problem in class_problems:
            spot = problem.get('spot_name', 'Unknown')
            spot_stats[spot] = spot_stats.get(spot, 0) + 1
        
        logger.info(f"✅ 반 미션 DB 생성 완료: {len(class_problems)}개 문제")
        
        return {
            "success": True,
            "class_mission": {
                "class_name": request.class_name,
                "total_problems": len(class_problems),
                "spots_coverage": spot_stats,
                "problems_preview": class_problems[:5]  # 처음 5개만 미리보기
            },
            "statistics": {
                "cached_problems": sum(1 for p in class_problems if p.get('source') == 'cached'),
                "generated_problems": sum(1 for p in class_problems if p.get('source') == 'generated'),
                "average_quality": sum(p.get('quality_score', 0.8) for p in class_problems) / len(class_problems) if class_problems else 0
            },
            "message": f"반 '{request.class_name}' 미션 DB 생성 완료"
        }
        
    except Exception as e:
        logger.error(f"❌ 반 미션 생성 오류: {e}")
        raise HTTPException(status_code=500, detail=f"반 미션 생성 실패: {str(e)}")

@app.get("/class/{class_name}/missions")
async def get_class_missions(class_name: str):
    """반 미션 목록 조회"""
    
    if class_name not in class_missions:
        raise HTTPException(status_code=404, detail=f"반 '{class_name}'을 찾을 수 없습니다")
    
    class_data = class_missions[class_name]
    
    return {
        "success": True,
        "class_info": {
            "class_name": class_data["class_name"],
            "location": class_data["location"],
            "grade": class_data["grade"],
            "total_problems": class_data["total_problems"],
            "created_at": class_data["created_at"]
        },
        "problems": class_data["problems"]
    }

# === 학생용 랜덤 퀴즈 API ===

@app.post("/student/quiz")
async def get_student_quiz(request: StudentQuizRequest):
    """학생 랜덤 퀴즈 (AR 오브젝트 클릭시)"""
    
    logger.info(f"🎲 학생 퀴즈 요청: {request.class_id} - {request.spot_name}")
    
    try:
        # 해당 반의 미션 DB에서 랜덤 선택
        if request.class_id not in class_missions:
            raise HTTPException(status_code=404, detail=f"반 '{request.class_id}' 미션 DB를 찾을 수 없습니다")
        
        class_data = class_missions[request.class_id]
        
        # 해당 스팟의 문제들 필터링
        spot_problems = [
            p for p in class_data['problems'] 
            if request.spot_name.lower() in p.get('spot_name', '').lower()
        ]
        
        if not spot_problems:
            # 해당 스팟 문제 없으면 전체에서 랜덤 선택
            spot_problems = class_data['problems']
        
        if not spot_problems:
            raise HTTPException(status_code=404, detail="이용 가능한 퀴즈가 없습니다")
        
        # 랜덤 선택
        import random
        selected_quiz = random.choice(spot_problems)
        
        return {
            "success": True,
            "quiz": {
                "problem_id": selected_quiz.get('problem_id'),
                "question": selected_quiz['question'],
                "choices": selected_quiz['choices'],
                "correct_index": selected_quiz['correct_index'],
                "explanation": selected_quiz.get('explanation'),
                "spot_name": selected_quiz.get('spot_name'),
                "grade": selected_quiz.get('grade')
            },
            "metadata": {
                "class_id": request.class_id,
                "team_id": request.team_id,
                "spot_name": request.spot_name,
                "available_problems": len(spot_problems),
                "timestamp": datetime.now().isoformat()
            }
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ 학생 퀴즈 오류: {e}")
        raise HTTPException(status_code=500, detail=f"퀴즈 조회 실패: {str(e)}")

# === 관리자용 유틸리티 API ===

@app.post("/admin/generate-base-db")
async def generate_base_database(background_tasks: BackgroundTasks):
    """기본 퀴즈 DB 생성 (백그라운드 작업)"""
    
    if not rag_system:
        raise HTTPException(status_code=503, detail="RAG 시스템이 초기화되지 않았습니다")
    
    async def create_db():
        """백그라운드에서 DB 생성"""
        try:
            logger.info("🏭 기본 퀴즈 DB 생성 시작")
            db_file = await create_base_quiz_database("base_quiz_database.json")
            
            global base_quiz_db
            base_quiz_db = load_quiz_database(db_file)
            
            logger.info(f"✅ 기본 퀴즈 DB 생성 완료: {len(base_quiz_db.get('problems', []))}개")
            
        except Exception as e:
            logger.error(f"❌ 기본 DB 생성 실패: {e}")
    
    background_tasks.add_task(create_db)
    
    return {
        "success": True,
        "message": "기본 퀴즈 DB 생성 작업이 시작되었습니다",
        "status": "백그라운드에서 처리 중...",
        "estimated_time": "10-20분 소요 예상"
    }

@app.get("/admin/locations")
async def get_available_locations():
    """이용 가능한 장소 목록"""
    
    if not rag_system:
        raise HTTPException(status_code=503, detail="RAG 시스템이 초기화되지 않았습니다")
    
    result = await get_locations_api(50)
    
    return {
        "success": True,
        "locations": result.get('locations', []),
        "total_count": len(result.get('locations', [])),
        "message": "퀴즈 생성 가능한 장소 목록"
    }

@app.get("/admin/stats")
async def get_system_statistics():
    """전체 시스템 통계"""
    
    stats = {
        "rag_system": {
            "initialized": rag_system is not None,
            "status": await get_system_status_api() if rag_system else {"initialized": False}
        },
        "base_quiz_db": {
            "loaded": base_quiz_db is not None,
            "problem_count": len(base_quiz_db.get('problems', [])) if base_quiz_db else 0
        },
        "class_missions": {
            "active_classes": len(class_missions),
            "total_class_problems": sum(len(missions.get('problems', [])) for missions in class_missions.values()),
            "classes": list(class_missions.keys())
        },
        "timestamp": datetime.now().isoformat()
    }
    
    return {"success": True, "statistics": stats}

# === 에러 핸들러 ===

@app.exception_handler(Exception)
async def global_exception_handler(request, exc):
    """전역 예외 처리"""
    logger.error(f"❌ Unhandled exception: {exc}")
    return {
        "success": False,
        "error": "서버 내부 오류가 발생했습니다",
        "detail": str(exc) if app.debug else "Internal Server Error",
        "timestamp": datetime.now().isoformat()
    }

# === 서버 실행 ===

if __name__ == "__main__":
    print("🚀 ARGO RAG FastAPI 서버 시작")
    print("=" * 50)
    print("📍 서버 주소: http://localhost:8000")
    print("📖 API 문서: http://localhost:8000/docs")
    print("🔧 관리자: http://localhost:8000/admin/stats")
    print("🔧 서버 종료: Ctrl+C")
    print("=" * 50)
    
    # 개발 모드에서는 reload=True, 프로덕션에서는 reload=False
    uvicorn.run(
        app, 
        host="0.0.0.0", 
        port=8000, 
        log_level="info",
        reload=False  # 프로덕션에서는 False
    )