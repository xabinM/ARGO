# main.py - 수정된 통합 FastAPI 서버
"""
🎯 ARGO AI 통합 서버 - 퀴즈 생성 + 포즈 분석

수정 사항:
1. ✅ 포즈 분석 라우터 경로 수정
2. ✅ 불필요한 기능 제거 (반별 미션 관리는 백엔드에서)
3. ✅ 기존 backend_integrated_fastapi.py 코드 활용
"""

from fastapi import FastAPI, HTTPException, status, UploadFile, File, Form
from contextlib import asynccontextmanager
from pydantic import BaseModel, Field, validator
import logging
import time
from datetime import datetime
from typing import List, Dict, Optional, Union
import openai
from dotenv import load_dotenv
import uvicorn
import json
import re
import random
import traceback
import sys
import os
import numpy as np
import cv2

# === 경로 설정 (실제 구조 반영) ===
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
OBJECT_DETECT_PATH = os.path.join(BASE_DIR, "object_detect", "src")
DATA_PATH = os.path.join(BASE_DIR, "data")  # data 폴더 경로 추가
sys.path.insert(0, OBJECT_DETECT_PATH)
sys.path.insert(0, DATA_PATH)  # data 폴더를 Python 경로에 추가

# 포즈 분석 모듈 import (실제 경로)
try:
    from service.AI_ObjectDetector import AI_ObjectDetector
    from service.AI_Analyze import AI_Analyze
    pose_modules_available = True
    print("✅ 포즈 분석 모듈 로드 성공")
except ImportError as e:
    print(f"⚠️ 포즈 분석 모듈 로드 실패: {e}")
    pose_modules_available = False
    AI_ObjectDetector = None
    AI_Analyze = None

# 기존 퀴즈 생성 모듈 import (경로 수정)
try:
    sys.path.insert(0, os.path.join(BASE_DIR, "data"))  # 임시로 data 폴더 추가
    from expanded_educational_spots import EXPANDED_EDUCATIONAL_SPOTS
    from education_curriculum_integration import EducationCurriculumIntegrator
    curriculum_available = True
    print("✅ 교육과정 연계 모듈 로드 성공")
except ImportError as e:
    EXPANDED_EDUCATIONAL_SPOTS = []
    EducationCurriculumIntegrator = None
    curriculum_available = False
    print(f"⚠️ 교육과정 모듈 import 실패: {e}")
    print("ℹ️ 파일 존재 확인 중...")
    
    # 디버깅: 파일 존재 여부 확인
    spots_file = os.path.join(BASE_DIR, "data", "expanded_educational_spots.py")
    curriculum_file = os.path.join(BASE_DIR, "data", "education_curriculum_integration.py")
    
    print(f"   expanded_educational_spots.py: {'존재' if os.path.exists(spots_file) else '없음'}")
    print(f"   education_curriculum_integration.py: {'존재' if os.path.exists(curriculum_file) else '없음'}")
    print("   💡 기본 모드로 계속 진행합니다.")

# === 환경설정 ===
load_dotenv()

logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

# === 전역 변수 ===
openai_client = None
spots_database = {}
spots_by_name = {}
curriculum_integrator = None
ai_pose_model = None  # 포즈 분석 모델

system_status = {
    "quiz_service_ready": False,
    "pose_service_ready": False,
    "llm_available": False,
    "total_spots": 0,
    "startup_time": None,
    "api_provider": "None",
}

# === 백엔드 연동용 데이터 모델 (기존 유지) ===
class ProblemGenerateRequestToAI(BaseModel):
    """백엔드에서 전송하는 문제 생성 요청 (spotName 기반)"""
    spotName: str = Field(..., description="스팟명", example="근정전")
    problemCnt: int = Field(..., ge=1, le=10, description="생성할 문제 개수")

class ProblemGenerateRequestFromSpotId(BaseModel):
    """백엔드에서 전송하는 문제 생성 요청 (spotId 기반)"""
    spotId: int = Field(..., description="스팟 ID")
    problemCnt: int = Field(..., ge=1, le=10, description="생성할 문제 개수")
    grade: Optional[int] = Field(default=5, ge=1, le=6, description="학년 (기본값: 5)")

class GeneratedQuizProblem(BaseModel):
    """생성된 퀴즈 문제"""
    question: str = Field(..., description="문제 텍스트")
    choices: List[str] = Field(..., description="선택지 리스트")
    correctIndex: int = Field(..., ge=0, le=2, description="정답 인덱스")
    explanation: str = Field(..., description="해설")

class ProblemGenerateResponse(BaseModel):
    """문제 생성 응답"""
    success: bool = Field(default=True)
    problems: List[GeneratedQuizProblem] = Field(..., description="생성된 문제 리스트")
    generation_info: Dict = Field(default_factory=dict, description="생성 정보")

# === 포즈 분석용 응답 모델 (추가) ===
class PoseAnalysisResponse(BaseModel):
    """포즈 분석 응답"""
    success: bool = Field(default=True)
    detected_people: int = Field(..., description="감지된 사람 수")
    pose_result: str = Field(..., description="포즈 분석 결과")
    confidence: float = Field(..., description="신뢰도")
    analysis_time_ms: float = Field(..., description="분석 소요 시간")

# === 초기화 함수들 (기존 backend_integrated_fastapi.py에서 가져옴) ===
def setup_openai_client():
    """OpenAI/GMS 클라이언트 설정"""
    global openai_client, system_status

    api_keys = [
        ("GMS_API_KEY", "https://gms.ssafy.io/gmsapi/api.openai.com/v1"),
        ("OPENAI_API_KEY", "https://api.openai.com/v1"),
        ("API_KEY", "https://gms.ssafy.io/gmsapi/api.openai.com/v1"),
    ]

    for key_name, base_url in api_keys:
        api_key = os.getenv(key_name)
        if api_key and len(api_key.strip()) >= 20:
            try:
                openai_client = openai.OpenAI(api_key=api_key.strip(), base_url=base_url)
                test_response = openai_client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[{"role": "user", "content": "test"}],
                    max_tokens=5,
                    timeout=10,
                )
                system_status["llm_available"] = True
                system_status["api_provider"] = key_name
                logger.info(f"✅ {key_name} API 연결 성공")
                return True
            except Exception as e:
                logger.warning(f"⚠️ {key_name} API 연결 실패: {e}")
                continue

    logger.warning("⚠️ 사용 가능한 API 키 없음. 폴백 모드로 실행")
    return False

def load_spots_database():
    """스팟 데이터베이스 로딩"""
    global spots_database, spots_by_name, system_status

    if EXPANDED_EDUCATIONAL_SPOTS:
        for idx, spot in enumerate(EXPANDED_EDUCATIONAL_SPOTS):
            spot_id = idx + 1
            spots_database[spot_id] = spot
            spots_by_name[spot["세부스팟"]] = {**spot, "spot_id": spot_id}
            if spot["메인장소"] not in spots_by_name:
                spots_by_name[spot["메인장소"]] = {**spot, "spot_id": spot_id}

    system_status["total_spots"] = len(spots_database)
    logger.info(f"📊 스팟 데이터베이스 로딩 완료: {system_status['total_spots']}개")

def setup_pose_analysis():
    """포즈 분석 모델 초기화"""
    global ai_pose_model, system_status
    
    if not pose_modules_available:
        logger.warning("⚠️ 포즈 분석 모듈 없음")
        system_status["pose_service_ready"] = False
        return False
    
    try:
        model_path = os.path.join(BASE_DIR, "object_detect", "model", "yolov8m.pt")
        if not os.path.exists(model_path):
            logger.warning(f"⚠️ YOLO 모델 파일 없음: {model_path}")
            system_status["pose_service_ready"] = False
            return False
            
        ai_pose_model = AI_ObjectDetector(model_path)
        system_status["pose_service_ready"] = True
        logger.info("✅ 포즈 분석 모델 초기화 완료")
        return True
    except Exception as e:
        logger.error(f"❌ 포즈 분석 모델 초기화 실패: {e}")
        system_status["pose_service_ready"] = False
        return False

# === 퀴즈 생성 함수들 (기존 코드 유지) ===
def find_spot_by_name(spot_name: str) -> Optional[Dict]:
    """스팟명으로 스팟 정보 검색"""
    if spot_name in spots_by_name:
        return spots_by_name[spot_name]
    
    for name, spot_info in spots_by_name.items():
        if spot_name in name or name in spot_name:
            return spot_info
    return None

def find_spot_by_id(spot_id: int) -> Optional[Dict]:
    """스팟 ID로 스팟 정보 검색"""
    return spots_database.get(spot_id)

def generate_fallback_quiz(spot_info: Dict, grade: int = 5) -> Dict:
    """폴백 퀴즈 생성"""
    spot_name = spot_info.get("세부스팟", "")
    location = spot_info.get("메인장소", "")
    
    return {
        "question": f"{spot_name}은 어디에 있나요?",
        "choices": ["서울", "부산", "대구"],
        "correctIndex": 0,
        "explanation": f"{spot_name}은 {location}에 위치한 중요한 문화재입니다.",
    }

def generate_single_quiz(spot_info: Dict, grade: int = 5) -> Dict:
    """단일 퀴즈 생성 (LLM 또는 폴백)"""
    if openai_client and system_status["llm_available"]:
        try:
            spot_name = spot_info.get("세부스팟", "")
            description = spot_info.get("설명", "")
            
            prompt = f"""초등학교 {grade}학년용 삼지선다 퀴즈를 만드세요.

장소: {spot_name}
설명: {description}

형식:
문제: [질문]
1) [선택지1] 2) [선택지2] 3) [선택지3]
정답: [번호]
해설: [설명]"""

            response = openai_client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {"role": "system", "content": "당신은 교육 전문가입니다."},
                    {"role": "user", "content": prompt}
                ],
                max_tokens=400,
                temperature=0.3,
                timeout=15,
            )
            
            # 간단한 파싱 (실제로는 더 정교한 파싱 필요)
            llm_text = response.choices[0].message.content.strip()
            
            # 파싱 시도 (기본적인 정규식)
            question_match = re.search(r"문제\s*[:：]\s*(.+?)(?=1\)|①)", llm_text, re.DOTALL)
            if question_match:
                question = question_match.group(1).strip()
                
                choices_match = re.search(r"1\)\s*([^2]+?)\s*2\)\s*([^3]+?)\s*3\)\s*([^정답]+?)(?=정답|$)", llm_text, re.DOTALL)
                if choices_match:
                    choices = [choices_match.group(i).strip() for i in range(1, 4)]
                    
                    answer_match = re.search(r"정답\s*[:：]\s*(\d+)", llm_text)
                    correct_index = int(answer_match.group(1)) - 1 if answer_match else 0
                    
                    explanation_match = re.search(r"해설\s*[:：]\s*(.+?)$", llm_text, re.DOTALL)
                    explanation = explanation_match.group(1).strip() if explanation_match else "해설이 없습니다."
                    
                    return {
                        "question": question,
                        "choices": choices,
                        "correctIndex": correct_index,
                        "explanation": explanation,
                    }
            
        except Exception as e:
            logger.error(f"LLM 퀴즈 생성 실패: {e}")
    
    # 폴백 퀴즈 반환
    return generate_fallback_quiz(spot_info, grade)

# === 라이프사이클 관리 ===
@asynccontextmanager
async def lifespan(app: FastAPI):
    """통합 서버 라이프사이클"""
    logger.info("🚀 ARGO AI 통합 서버 시작")
    system_status["startup_time"] = datetime.now().isoformat()
    
    # 1. OpenAI 클라이언트 설정
    setup_openai_client()
    
    # 2. 스팟 데이터베이스 로딩
    load_spots_database()
    system_status["quiz_service_ready"] = True
    
    # 3. 포즈 분석 모델 초기화
    setup_pose_analysis()
    
    ready_count = sum([
        system_status["quiz_service_ready"],
        system_status["pose_service_ready"]
    ])
    
    logger.info(f"✅ 초기화 완료: {ready_count}/2 서비스 준비")
    logger.info(f"   퀴즈 생성: {'✅' if system_status['quiz_service_ready'] else '❌'}")
    logger.info(f"   포즈 분석: {'✅' if system_status['pose_service_ready'] else '❌'}")
    logger.info(f"   LLM 사용: {'✅' if system_status['llm_available'] else '❌ (폴백 모드)'}")
    
    yield
    
    logger.info("🔄 서버 종료")

# FastAPI 앱 생성
app = FastAPI(
    title="ARGO AI 통합 서버",
    description="퀴즈 생성 + 포즈 분석 통합 API",
    version="v1.0",
    lifespan=lifespan,
)

# === API 엔드포인트 ===

@app.post("/generate-problem", response_model=ProblemGenerateResponse)
async def generate_problem_by_spot_name(request: ProblemGenerateRequestToAI):
    """백엔드 호환: 퀴즈 생성 API"""
    start_time = datetime.now()
    logger.info(f"🎯 퀴즈 생성 요청: {request.spotName}, {request.problemCnt}개")

    try:
        spot_info = find_spot_by_name(request.spotName)
        if not spot_info:
            raise HTTPException(
                status_code=404, 
                detail=f"스팟 '{request.spotName}'을 찾을 수 없습니다."
            )

        problems = []
        for i in range(request.problemCnt):
            grade = 3 + (i % 4)  # 3,4,5,6 순환
            quiz = generate_single_quiz(spot_info, grade)
            problems.append(GeneratedQuizProblem(**quiz))

        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        generation_info = {
            "spot_name": request.spotName,
            "generated_count": len(problems),
            "processing_time_ms": round(processing_time, 2),
            "llm_used": system_status["llm_available"]
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
    """spotId 기반 문제 생성 (확장 API)"""
    start_time = datetime.now()
    logger.info(f"🎯 SpotID 기반 퀴즈 생성: spotId={request.spotId}, count={request.problemCnt}, grade={request.grade}")

    try:
        spot_info = find_spot_by_id(request.spotId)
        if not spot_info:
            raise HTTPException(
                status_code=404, 
                detail=f"스팟 ID {request.spotId}를 찾을 수 없습니다."
            )

        problems = []
        for i in range(request.problemCnt):
            quiz = generate_single_quiz(spot_info, request.grade)
            problems.append(GeneratedQuizProblem(**quiz))

        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        generation_info = {
            "spot_id": request.spotId,
            "spot_name": spot_info.get("세부스팟", "Unknown"),
            "grade": request.grade,
            "generated_count": len(problems),
            "processing_time_ms": round(processing_time, 2),
            "llm_used": system_status["llm_available"]
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
            confidence=0.8,  # 임시 값
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
        "services": {
            "quiz_generation": system_status["quiz_service_ready"],
            "pose_analysis": system_status["pose_service_ready"],
            "llm_available": system_status["llm_available"]
        },
        "system_info": {
            "total_spots": system_status["total_spots"],
            "api_provider": system_status["api_provider"],
            "startup_time": system_status["startup_time"]
        }
    }

@app.get("/")
async def root():
    """루트 엔드포인트"""
    return {
        "title": "ARGO AI 통합 서버",
        "version": "v1.0",
        "services": [
            "🎯 퀴즈 생성 (RAG 기반)",
            "📸 포즈 분석 (YOLO + MediaPipe)"
        ],
        "endpoints": {
            "quiz_name": "POST /generate-problem",
            "quiz_id": "POST /generate-problem-by-id",
            "pose": "POST /pose/predict", 
            "health": "GET /health"
        },
        "status": system_status
    }

if __name__ == "__main__":
    print("🚀 ARGO AI 통합 서버 시작")
    print("📋 지원 기능:")
    print("   - 퀴즈 생성 (이름): POST /generate-problem")
    print("   - 퀴즈 생성 (ID): POST /generate-problem-by-id")
    print("   - 포즈 분석: POST /pose/predict")
    print("   - 상태 확인: GET /health")
    
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        log_level="info"
    )