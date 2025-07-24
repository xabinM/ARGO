# main.py - ARGO RAG 마이크로서비스 메인 서버
from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, Dict, List
import logging
import asyncio
import os
from contextlib import asynccontextmanager

# ARGO 파이프라인 import
from argo_rag_pipeline import ARGOPipeline
from argo_api_handler import ARGOAPIHandler

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 전역 파이프라인 객체
pipeline_handler = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    """앱 시작/종료 시 파이프라인 관리"""
    global pipeline_handler
    
    # 시작 시 파이프라인 초기화
    logger.info("🚀 ARGO RAG 파이프라인 초기화 중...")
    try:
        pipeline_handler = ARGOAPIHandler()
        data_file = os.getenv("HERITAGE_DATA_FILE", "heritage_complete_database.json")
        await pipeline_handler.initialize_api(data_file)
        logger.info("✅ 파이프라인 초기화 완료")
    except Exception as e:
        logger.error(f"❌ 파이프라인 초기화 실패: {e}")
        raise
    
    yield
    
    # 종료 시 정리 작업
    logger.info("🔄 서버 종료 - 정리 중...")

# FastAPI 앱 생성
app = FastAPI(
    title="ARGO RAG 마이크로서비스",
    description="AR 기반 현장체험학습용 미션 생성 API",
    version="1.0.0",
    lifespan=lifespan
)

# CORS 설정 (모바일 앱 연동)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 실제 운영시 특정 도메인만 허용
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 요청/응답 모델 정의
class MissionRequest(BaseModel):
    """미션 생성 요청 모델"""
    location: str  # 장소명 (예: "경복궁")
    grade: int = 5  # 학년 (3-6)
    group_size: int = 4  # 그룹 크기
    duration_minutes: int = 30  # 활동 시간
    mission_type: str = "퀴즈"  # 미션 타입
    context: Optional[str] = None  # 추가 컨텍스트

class MissionResponse(BaseModel):
    """미션 생성 응답 모델"""
    success: bool
    mission_id: str
    student_mission: Dict  # 학생용 미션 내용
    teacher_info: Dict  # 교사용 가이드
    metadata: Dict  # 메타데이터
    error_message: Optional[str] = None

class HealthResponse(BaseModel):
    """서버 상태 응답"""
    status: str
    pipeline_ready: bool
    version: str

# API 엔드포인트 정의
@app.get("/", response_model=Dict)
async def root():
    """기본 엔드포인트"""
    return {
        "service": "ARGO RAG 마이크로서비스",
        "status": "running",
        "version": "1.0.0",
        "endpoints": {
            "health": "/health",
            "generate_mission": "/api/v1/missions/generate",
            "mission_types": "/api/v1/missions/types"
        }
    }

@app.get("/health", response_model=HealthResponse)
async def health_check():
    """서버 상태 체크"""
    pipeline_ready = pipeline_handler is not None and pipeline_handler.initialized
    
    return HealthResponse(
        status="healthy" if pipeline_ready else "initializing",
        pipeline_ready=pipeline_ready,
        version="1.0.0"
    )

@app.post("/api/v1/missions/generate", response_model=MissionResponse)
async def generate_mission(request: MissionRequest):
    """미션 생성 API - 모바일 백엔드에서 호출"""
    
    if not pipeline_handler or not pipeline_handler.initialized:
        raise HTTPException(
            status_code=503, 
            detail="파이프라인이 초기화되지 않았습니다. 잠시 후 다시 시도해주세요."
        )
    
    try:
        logger.info(f"미션 생성 요청: {request.location}, {request.grade}학년")
        
        # 요청 검증
        if request.grade < 3 or request.grade > 6:
            raise HTTPException(status_code=400, detail="학년은 3-6학년만 지원됩니다.")
        
        if request.group_size < 1 or request.group_size > 10:
            raise HTTPException(status_code=400, detail="그룹 크기는 1-10명까지 가능합니다.")
        
        # 파이프라인 호출
        request_data = {
            "location": request.location,
            "grade": request.grade,
            "group_size": request.group_size,
            "duration_minutes": request.duration_minutes,
            "mission_type": request.mission_type,
            "context": request.context
        }
        
        result = await pipeline_handler.handle_mission_request(request_data)
        
        if not result.get("success", False):
            raise HTTPException(
                status_code=500, 
                detail=result.get("error", "미션 생성 중 오류가 발생했습니다.")
            )
        
        # 성공 응답
        return MissionResponse(
            success=True,
            mission_id=result["data"]["student_mission"]["mission_id"],
            student_mission=result["data"]["student_mission"],
            teacher_info=result["data"]["teacher_info"],
            metadata=result["data"]["metadata"]
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"미션 생성 실패: {e}")
        raise HTTPException(status_code=500, detail=f"내부 서버 오류: {str(e)}")

@app.get("/api/v1/missions/types")
async def get_mission_types():
    """지원하는 미션 타입 목록 반환"""
    
    if not pipeline_handler:
        raise HTTPException(status_code=503, detail="서비스가 준비되지 않았습니다.")
    
    return pipeline_handler.get_supported_mission_types()

@app.post("/api/v1/missions/batch")
async def generate_multiple_missions(requests: List[MissionRequest]):
    """여러 미션 동시 생성 (배치 처리)"""
    
    if len(requests) > 5:
        raise HTTPException(status_code=400, detail="한 번에 최대 5개까지만 처리 가능합니다.")
    
    results = []
    
    for req in requests:
        try:
            result = await generate_mission(req)
            results.append(result)
        except Exception as e:
            logger.error(f"배치 처리 중 오류: {e}")
            results.append(MissionResponse(
                success=False,
                mission_id="",
                student_mission={},
                teacher_info={},
                metadata={},
                error_message=str(e)
            ))
    
    return {"results": results}

# 관리용 엔드포인트
@app.post("/admin/reload")
async def reload_pipeline(background_tasks: BackgroundTasks):
    """파이프라인 재로드 (관리자용)"""
    
    def reload_task():
        global pipeline_handler
        try:
            # 기존 파이프라인 정리
            pipeline_handler = None
            
            # 새로 초기화
            pipeline_handler = ARGOAPIHandler()
            asyncio.run(pipeline_handler.initialize_api("heritage_complete_database.json"))
            
            logger.info("파이프라인 재로드 완료")
        except Exception as e:
            logger.error(f"파이프라인 재로드 실패: {e}")
    
    background_tasks.add_task(reload_task)
    return {"message": "파이프라인 재로드 시작됨"}

@app.get("/admin/stats")
async def get_service_stats():
    """서비스 통계 (관리자용)"""
    return {
        "pipeline_status": "ready" if pipeline_handler and pipeline_handler.initialized else "not_ready",
        "supported_locations": ["경복궁", "서울대공원", "국립중앙박물관"],  # 실제로는 DB에서
        "supported_grades": [3, 4, 5, 6],
        "mission_types": ["퀴즈", "관찰미션", "체험미션", "사진미션"]
    }

if __name__ == "__main__":
    import uvicorn
    
    # 개발 모드 실행
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )