# main.py - ARGO AI Rapid 서버 (1-2일 완성용)
"""
🎯 ARGO AI Rapid 서버 - 퀴즈 생성 + 포즈 분석

핵심 기능:
- GPT 기반 고품질 퀴즈 생성
- YOLO + MediaPipe 포즈 분석  
- 간소화된 구조로 빠른 완성
"""

from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from contextlib import asynccontextmanager
from datetime import datetime
import logging
import sys
import os
import numpy as np
import cv2

# 경로 설정
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(BASE_DIR, "object_detect", "src"))
sys.path.insert(0, os.path.join(BASE_DIR, "data"))

# 로깅 설정
logging.basicConfig(
    level=logging.INFO, 
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

# 환경변수 로드
from dotenv import load_dotenv
load_dotenv()

# === 모듈 Import ===
# 포즈 분석 모듈
try:
    from service.AI_ObjectDetector import AI_ObjectDetector
    from service.AI_Analyze import AI_Analyze
    pose_modules_available = True
    logger.info("✅ 포즈 분석 모듈 로드 성공")
except ImportError as e:
    logger.warning(f"⚠️ 포즈 분석 모듈 로드 실패: {e}")
    pose_modules_available = False
    AI_ObjectDetector = None
    AI_Analyze = None

# 퀴즈 생성 모듈  
try:
    from expanded_educational_spots import EXPANDED_EDUCATIONAL_SPOTS
    quiz_data_available = True
    logger.info("✅ 퀴즈 데이터 로드 성공")
except ImportError as e:
    logger.warning(f"⚠️ 퀴즈 데이터 로드 실패: {e}")
    EXPANDED_EDUCATIONAL_SPOTS = []
    quiz_data_available = False

# 간소화된 퀴즈 생성기
from quiz.rapid_complete_generator import RapidQuizGenerator

# 데이터 모델
from pydantic import BaseModel, Field
from typing import List, Dict, Optional

# === 전역 변수 ===
rapid_generator = None
ai_pose_model = None
spots_database = {}
spots_by_name = {}

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

class ProblemGenerateRequestFromSpotId(BaseModel):
    """백엔드 호환: spotId 기반 요청"""
    spotId: int = Field(..., description="스팟 ID")
    problemCnt: int = Field(..., ge=1, le=10, description="생성할 문제 개수")
    grade: Optional[int] = Field(default=5, ge=1, le=6, description="학년")

class GeneratedQuizProblem(BaseModel):
    """생성된 퀴즈 문제"""
    question: str = Field(..., description="문제 텍스트")
    choices: List[str] = Field(..., description="선택지 리스트")
    correctIndex: int = Field(..., ge=0, le=2, description="정답 인덱스")
    explanation: str = Field(..., description="해설")
    generation_method: Optional[str] = Field(default="rapid", description="생성 방법")
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

# === 초기화 함수들 ===
def setup_quiz_service():
    """퀴즈 서비스 초기화"""
    global rapid_generator, spots_database, spots_by_name, system_status
    
    try:
        # RapidQuizGenerator 초기화
        from config.settings import get_settings
        settings = get_settings()
        rapid_generator = RapidQuizGenerator(settings)
        
        # 스팟 데이터베이스 로딩
        if EXPANDED_EDUCATIONAL_SPOTS:
            for idx, spot in enumerate(EXPANDED_EDUCATIONAL_SPOTS):
                spot_id = idx + 1
                spots_database[spot_id] = spot
                spots_by_name[spot["세부스팟"]] = {**spot, "spot_id": spot_id}
                if spot["메인장소"] not in spots_by_name:
                    spots_by_name[spot["메인장소"]] = {**spot, "spot_id": spot_id}
        
        system_status["quiz_service_ready"] = True
        logger.info(f"✅ 퀴즈 서비스 초기화 완료: {len(spots_database)}개 스팟")
        
    except Exception as e:
        logger.error(f"❌ 퀴즈 서비스 초기화 실패: {e}")
        system_status["quiz_service_ready"] = False

def setup_pose_service():
    """포즈 분석 서비스 초기화"""
    global ai_pose_model, system_status
    
    if not pose_modules_available:
        logger.warning("⚠️ 포즈 분석 모듈 없음")
        system_status["pose_service_ready"] = False
        return
    
    try:
        model_path = os.path.join(BASE_DIR, "object_detect", "model", "yolov8m.pt")
        if not os.path.exists(model_path):
            logger.warning(f"⚠️ YOLO 모델 파일 없음: {model_path}")
            system_status["pose_service_ready"] = False
            return
            
        ai_pose_model = AI_ObjectDetector(model_path)
        system_status["pose_service_ready"] = True
        logger.info("✅ 포즈 분석 서비스 초기화 완료")
        
    except Exception as e:
        logger.error(f"❌ 포즈 분석 서비스 초기화 실패: {e}")
        system_status["pose_service_ready"] = False

# === 헬퍼 함수들 ===
def find_spot_by_name(spot_name: str) -> Optional[Dict]:
    """스팟명으로 스팟 검색"""
    if spot_name in spots_by_name:
        return spots_by_name[spot_name]
    
    for name, spot_info in spots_by_name.items():
        if spot_name in name or name in spot_name:
            return spot_info
    return None

def find_spot_by_id(spot_id: int) -> Optional[Dict]:
    """스팟 ID로 스팟 검색"""
    return spots_database.get(spot_id)

# === 라이프사이클 관리 ===
@asynccontextmanager
async def lifespan(app: FastAPI):
    """서버 라이프사이클 관리"""
    logger.info("🚀 ARGO AI Rapid 서버 시작")
    system_status["startup_time"] = datetime.now().isoformat()
    
    # 서비스 초기화
    setup_quiz_service()
    setup_pose_service()
    
    ready_count = sum([
        system_status["quiz_service_ready"],
        system_status["pose_service_ready"]
    ])
    
    logger.info(f"✅ 초기화 완료: {ready_count}/2 서비스 준비")
    logger.info(f"   퀴즈 생성: {'✅' if system_status['quiz_service_ready'] else '❌'}")
    logger.info(f"   포즈 분석: {'✅' if system_status['pose_service_ready'] else '❌'}")
    
    yield
    
    logger.info("🔄 서버 종료")

# FastAPI 앱 생성
app = FastAPI(
    title="ARGO AI Rapid 서버",
    description="퀴즈 생성 + 포즈 분석 (1-2일 완성용)",
    version="v1.0-rapid",
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
        spot_info = find_spot_by_name(request.spotName)
        if not spot_info:
            raise HTTPException(
                status_code=404, 
                detail=f"스팟 '{request.spotName}'을 찾을 수 없습니다."
            )

        # 퀴즈 생성
        problems = []
        for i in range(request.problemCnt):
            quiz_data = await rapid_generator.generate_quiz(
                spot_info, 
                request.grade, 
                "normal"
            )
            if quiz_data:
                problems.append(GeneratedQuizProblem(**quiz_data))

        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        generation_info = {
            "spot_name": request.spotName,
            "grade": request.grade,
            "generated_count": len(problems),
            "processing_time_ms": round(processing_time, 2),
            "llm_used": rapid_generator.is_llm_ready if rapid_generator else False
        }

        logger.info(f"✅ 퀴즈 생성 완료: {len(problems)}개")
        
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
        spot_info = find_spot_by_id(request.spotId)
        if not spot_info:
            raise HTTPException(
                status_code=404, 
                detail=f"스팟 ID {request.spotId}를 찾을 수 없습니다."
            )

        # 퀴즈 생성
        problems = []
        for i in range(request.problemCnt):
            quiz_data = await rapid_generator.generate_quiz(
                spot_info, 
                request.grade, 
                "normal"
            )
            if quiz_data:
                problems.append(GeneratedQuizProblem(**quiz_data))

        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        generation_info = {
            "spot_id": request.spotId,
            "spot_name": spot_info.get("세부스팟", "Unknown"),
            "grade": request.grade,
            "generated_count": len(problems),
            "processing_time_ms": round(processing_time, 2),
            "llm_used": rapid_generator.is_llm_ready if rapid_generator else False
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
        # 이미지 읽기
        contents = await file.read()
        np_arr = np.frombuffer(contents, np.uint8)
        image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
        
        if image is None:
            raise HTTPException(status_code=400, detail="이미지 디코딩 실패")

        # YOLO + 포즈 분석
        image, results, poses_info = ai_pose_model.Load_image(image)
        
        if results is None or not poses_info:
            raise HTTPException(status_code=404, detail="사람 감지 실패")

        # 포즈 분석
        analyzer = AI_Analyze(image, results, poses_info)
        analysis_result = analyzer.print_keypoints(pose_select=pose_select)
        
        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        
        return PoseAnalysisResponse(
            success=True,
            detected_people=len(poses_info),
            pose_result=str(analysis_result),
            confidence=0.8,  # 간단한 휴리스틱
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
        "version": "v1.0-rapid",
        "services": {
            "quiz_generation": {
                "ready": system_status["quiz_service_ready"],
                "llm_available": rapid_generator.is_llm_ready if rapid_generator else False,
                "total_spots": len(spots_database)
            },
            "pose_analysis": {
                "ready": system_status["pose_service_ready"],
                "model_loaded": ai_pose_model is not None
            }
        },
        "performance": {
            "total_requests": system_status["total_requests"],
            "uptime": system_status["startup_time"]
        }
    }

@app.get("/")
async def root():
    """루트 엔드포인트"""
    return {
        "title": "ARGO AI Rapid 서버",
        "version": "v1.0-rapid",
        "description": "1-2일 완성용 퀴즈 생성 + 포즈 분석 API",
        "features": [
            "🎯 GPT 기반 고품질 퀴즈 생성",
            "📸 YOLO + MediaPipe 포즈 분석",
            "⚡ 빠른 프로토타이핑에 최적화",
            "🔧 간소화된 구조"
        ],
        "endpoints": {
            "quiz_name": "POST /generate-problem",
            "quiz_id": "POST /generate-problem-by-id",
            "pose": "POST /pose/predict", 
            "health": "GET /health"
        },
        "advantages": [
            "📈 실무적 완성도",
            "🔄 유지보수 용이",
            "⚡ 빠른 개발 사이클",
            "🎓 포트폴리오 가치"
        ],
        "status": system_status
    }

if __name__ == "__main__":
    print("🚀 ARGO AI Rapid 서버 시작")
    print("📋 특징:")
    print("   - 1-2일 완성에 최적화")
    print("   - GPT 기반 고품질 퀴즈 생성")
    print("   - YOLO + MediaPipe 포즈 분석")
    print("   - 간소화된 구조로 빠른 배포")
    
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        log_level="info"
    )