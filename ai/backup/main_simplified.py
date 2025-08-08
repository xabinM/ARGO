# main.py - 간소화된 메인 서버 (라우팅 중심)
"""
🎯 ARGO AI 통합 서버 - 리팩토링 버전

주요 변경사항:
- 비즈니스 로직을 서비스 레이어로 분리
- 고품질 퀴즈 생성기 적용
- 모듈화된 구조로 유지보수성 향상
"""

from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from contextlib import asynccontextmanager
from datetime import datetime
import logging
import sys
import os

# 경로 설정
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(BASE_DIR, "object_detect", "src"))
sys.path.insert(0, os.path.join(BASE_DIR, "data"))

# 설정 및 서비스 import
from config.settings import get_settings, validate_environment
from services.quiz_service import QuizService
from services.spot_service import SpotService
from services.pose_service import PoseService

# 데이터 모델
from pydantic import BaseModel, Field
from typing import List, Dict, Optional

# 환경변수 로드
from dotenv import load_dotenv
load_dotenv()

# 로깅 설정
logging.basicConfig(
    level=logging.INFO, 
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

# 전역 설정 및 서비스
settings = get_settings()
quiz_service = None
spot_service = None
pose_service = None

# 시스템 상태
system_status = {
    "quiz_service_ready": False,
    "pose_service_ready": False,
    "startup_time": None,
    "total_requests": 0
}

# === 데이터 모델 ===
class ProblemGenerateRequestToAI(BaseModel):
    """백엔드 호환: spotName 기반 요청"""
    spotName: str = Field(..., description="스팟명", example="근정전")
    problemCnt: int = Field(..., ge=1, le=10, description="생성할 문제 개수")
    grade: Optional[int] = Field(default=5, ge=1, le=6, description="학년")
    difficulty: Optional[str] = Field(default="normal", description="난이도")

class ProblemGenerateRequestFromSpotId(BaseModel):
    """백엔드 호환: spotId 기반 요청"""
    spotId: int = Field(..., description="스팟 ID")
    problemCnt: int = Field(..., ge=1, le=10, description="생성할 문제 개수")
    grade: Optional[int] = Field(default=5, ge=1, le=6, description="학년")
    difficulty: Optional[str] = Field(default="normal", description="난이도")

class GeneratedQuizProblem(BaseModel):
    """생성된 퀴즈 문제"""
    question: str = Field(..., description="문제 텍스트")
    choices: List[str] = Field(..., description="선택지 리스트")
    correctIndex: int = Field(..., ge=0, le=2, description="정답 인덱스")
    explanation: str = Field(..., description="해설")
    generation_method: Optional[str] = Field(default="unknown", description="생성 방법")
    quality_score: Optional[float] = Field(default=0.0, description="품질 점수")

class ProblemGenerateResponse(BaseModel):
    """문제 생성 응답"""
    success: bool = Field(default=True)
    problems: List[GeneratedQuizProblem] = Field(..., description="생성된 문제 리스트")
    generation_info: Dict = Field(default_factory=dict, description="생성 정보")

class PoseAnalysisResponse(BaseModel):
    """포즈 분석 응답"""
    success: bool = Field(default=True)
    detected_people: int = Field(..., description="감지된 사람 수")
    pose_result: str = Field(..., description="포즈 분석 결과")
    confidence: float = Field(..., description="신뢰도")
    analysis_time_ms: float = Field(..., description="분석 소요 시간")

# === 라이프사이클 관리 ===
@asynccontextmanager
async def lifespan(app: FastAPI):
    """서버 라이프사이클 관리"""
    global quiz_service, spot_service, pose_service, system_status
    
    logger.info("🚀 ARGO AI 통합 서버 시작 (리팩토링 버전)")
    system_status["startup_time"] = datetime.now().isoformat()
    
    # 환경 검증
    validation_errors = validate_environment()
    if validation_errors:
        logger.warning("⚠️ 환경 검증 경고:")
        for error in validation_errors:
            logger.warning(f"   - {error}")
    
    # 서비스 초기화
    try:
        # 스팟 서비스 (기본)
        spot_service = SpotService()
        spot_service.initialize()
        logger.info("✅ 스팟 서비스 초기화 완료")
        
        # 퀴즈 서비스 (고품질)
        quiz_service = QuizService(settings)
        await quiz_service.initialize()
        system_status["quiz_service_ready"] = quiz_service.is_ready
        logger.info(f"✅ 퀴즈 서비스 초기화: {'성공' if quiz_service.is_ready else '부분 성공 (폴백 모드)'}")
        
        # 포즈 서비스 (선택적)
        pose_service = PoseService(settings)
        await pose_service.initialize()
        system_status["pose_service_ready"] = pose_service.is_ready
        logger.info(f"✅ 포즈 서비스 초기화: {'성공' if pose_service.is_ready else '실패 (서비스 비활성화)'}")
        
        # 시스템 정보 출력
        system_info = settings.get_system_info()
        logger.info("📊 시스템 정보:")
        for key, value in system_info.items():
            logger.info(f"   {key}: {value}")
            
    except Exception as e:
        logger.error(f"❌ 서비스 초기화 실패: {e}")
    
    ready_count = sum([
        system_status["quiz_service_ready"],
        system_status["pose_service_ready"]
    ])
    logger.info(f"🎯 초기화 완료: {ready_count}/2 서비스 준비")
    
    yield
    
    # 종료 시 정리
    logger.info("🔄 서버 종료 중...")
    if quiz_service:
        await quiz_service.cleanup()
    if pose_service:
        await pose_service.cleanup()

# FastAPI 앱 생성
app = FastAPI(
    title="ARGO AI 통합 서버",
    description="퀴즈 생성 + 포즈 분석 통합 API (리팩토링 버전)",
    version="v2.0",
    lifespan=lifespan,
)

# 미들웨어 - 요청 카운팅
@app.middleware("http")
async def count_requests(request, call_next):
    system_status["total_requests"] += 1
    response = await call_next(request)
    return response

# === API 엔드포인트 ===

@app.post("/generate-problem", response_model=ProblemGenerateResponse)
async def generate_problem_by_spot_name(request: ProblemGenerateRequestToAI):
    """백엔드 호환: spotName 기반 퀴즈 생성"""
    start_time = datetime.now()
    logger.info(f"🎯 퀴즈 생성 요청: {request.spotName}, {request.problemCnt}개, {request.grade}학년")

    if not system_status["quiz_service_ready"]:
        raise HTTPException(status_code=503, detail="퀴즈 생성 서비스가 준비되지 않았습니다")

    try:
        # 스팟 정보 조회
        spot_info = spot_service.find_spot_by_name(request.spotName)
        if not spot_info:
            raise HTTPException(
                status_code=404, 
                detail=f"스팟 '{request.spotName}'을 찾을 수 없습니다."
            )

        # 퀴즈 생성
        problems = await quiz_service.generate_multiple_quizzes(
            spot_info=spot_info,
            count=request.problemCnt,
            grade=request.grade,
            difficulty=request.difficulty
        )

        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        generation_info = {
            "spot_name": request.spotName,
            "grade": request.grade,
            "difficulty": request.difficulty,
            "generated_count": len(problems),
            "processing_time_ms": round(processing_time, 2),
            "llm_used": quiz_service.is_llm_available,
            "average_quality": round(sum(p.quality_score for p in problems) / len(problems), 2) if problems else 0
        }

        logger.info(f"✅ 퀴즈 생성 완료: {len(problems)}개 (평균 품질: {generation_info['average_quality']:.2f})")
        
        return ProblemGenerateResponse(
            success=True,
            problems=problems,
            generation_info=generation_info
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ 퀴즈 생성 실패: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/generate-problem-by-id", response_model=ProblemGenerateResponse)
async def generate_problem_by_spot_id(request: ProblemGenerateRequestFromSpotId):
    """백엔드 호환: spotId 기반 퀴즈 생성"""
    start_time = datetime.now()
    logger.info(f"🎯 SpotID 퀴즈 생성: ID={request.spotId}, {request.problemCnt}개, {request.grade}학년")

    if not system_status["quiz_service_ready"]:
        raise HTTPException(status_code=503, detail="퀴즈 생성 서비스가 준비되지 않았습니다")

    try:
        # 스팟 정보 조회
        spot_info = spot_service.find_spot_by_id(request.spotId)
        if not spot_info:
            raise HTTPException(
                status_code=404, 
                detail=f"스팟 ID {request.spotId}를 찾을 수 없습니다."
            )

        # 퀴즈 생성
        problems = await quiz_service.generate_multiple_quizzes(
            spot_info=spot_info,
            count=request.problemCnt,
            grade=request.grade,
            difficulty=request.difficulty
        )

        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        generation_info = {
            "spot_id": request.spotId,
            "spot_name": spot_info.get("세부스팟", "Unknown"),
            "grade": request.grade,
            "difficulty": request.difficulty,
            "generated_count": len(problems),
            "processing_time_ms": round(processing_time, 2),
            "llm_used": quiz_service.is_llm_available,
            "average_quality": round(sum(p.quality_score for p in problems) / len(problems), 2) if problems else 0
        }

        logger.info(f"✅ SpotID 퀴즈 생성 완료: {len(problems)}개")
        
        return ProblemGenerateResponse(
            success=True,
            problems=problems,
            generation_info=generation_info
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ SpotID 퀴즈 생성 실패: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/pose/predict", response_model=PoseAnalysisResponse)
async def analyze_pose(
    file: UploadFile = File(...), 
    pose_select: str = Form(...)
):
    """포즈 분석 API"""
    start_time = datetime.now()
    
    if not system_status["pose_service_ready"]:
        raise HTTPException(
            status_code=503, 
            detail="포즈 분석 서비스가 준비되지 않았습니다"
        )
    
    try:
        # 포즈 분석 수행
        result = await pose_service.analyze_pose(file, pose_select)
        
        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        
        return PoseAnalysisResponse(
            success=True,
            detected_people=result["detected_people"],
            pose_result=result["pose_result"],
            confidence=result["confidence"],
            analysis_time_ms=round(processing_time, 2)
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ 포즈 분석 실패: {e}")
        raise HTTPException(status_code=500, detail=f"포즈 분석 오류: {str(e)}")

@app.get("/health")
async def health_check():
    """시스템 상태 확인"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "version": "v2.0",
        "services": {
            "quiz_generation": {
                "ready": system_status["quiz_service_ready"],
                "llm_available": quiz_service.is_llm_available if quiz_service else False,
                "total_spots": spot_service.get_total_spots() if spot_service else 0
            },
            "pose_analysis": {
                "ready": system_status["pose_service_ready"],
                "model_loaded": pose_service.is_model_loaded if pose_service else False
            }
        },
        "system_info": settings.get_system_info(),
        "performance": {
            "total_requests": system_status["total_requests"],
            "uptime": system_status["startup_time"]
        }
    }

@app.get("/spots")
async def get_available_spots(
    location: Optional[str] = None,
    grade: Optional[int] = None
):
    """사용 가능한 스팟 목록 조회"""
    if not spot_service:
        raise HTTPException(status_code=503, detail="스팟 서비스가 준비되지 않았습니다")
    
    try:
        spots = spot_service.get_spots(location=location, grade=grade)
        return {
            "total_spots": len(spots),
            "filters": {
                "location": location,
                "grade": grade
            },
            "spots": spots
        }
    except Exception as e:
        logger.error(f"❌ 스팟 조회 실패: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/")
async def root():
    """루트 엔드포인트"""
    return {
        "title": "ARGO AI 통합 서버",
        "version": "v2.0 (리팩토링)",
        "description": "고품질 퀴즈 생성 + 포즈 분석 통합 API",
        "features": [
            "🎯 고품질 퀴즈 생성 (학년별 맞춤)",
            "📚 교육과정 연계 강화",
            "📸 포즈 분석 (YOLO + MediaPipe)",
            "🔧 모듈화된 서비스 구조",
            "✅ 품질 검증 시스템"
        ],
        "endpoints": {
            "quiz_name": "POST /generate-problem",
            "quiz_id": "POST /generate-problem-by-id",
            "pose": "POST /pose/predict", 
            "spots": "GET /spots",
            "health": "GET /health"
        },
        "improvements": [
            "📈 퀴즈 품질 대폭 향상",
            "🔄 모듈화로 유지보수성 개선",
            "⚡ 성능 최적화",
            "🎓 교육적 가치 강화"
        ],
        "status": system_status
    }

if __name__ == "__main__":
    print("🚀 ARGO AI 통합 서버 v2.0 시작")
    print("📋 개선사항:")
    print("   - 고품질 퀴즈 생성기 적용")
    print("   - 모듈화된 서비스 구조")
    print("   - 향상된 품질 검증")
    print("   - 학년별 맞춤 최적화")
    
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        log_level="info"
    ) 