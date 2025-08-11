# main.py - 경로 문제 해결된 FastAPI 서버
"""
🎯 ARGO AI 통합 서버 - Import 에러 해결 완료

수정사항:
1. ✅ 경로 문제 해결 - 모든 필요 경로를 즉시 sys.path에 추가
2. ✅ 안전한 import - try/except로 import 실패 시 graceful fallback
3. ✅ 디버깅 정보 추가
"""

import sys
import os

# === 경로 설정 (최우선으로 처리) ===
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# 필수 경로들을 sys.path에 추가
sys.path.insert(0, os.path.join(BASE_DIR, "data"))              # 스팟 데이터
sys.path.insert(0, os.path.join(BASE_DIR, "object_detect", "src"))  # 포즈 분석
sys.path.insert(0, os.path.join(BASE_DIR, "services"))          # 서비스 모듈
sys.path.insert(0, os.path.join(BASE_DIR, "quiz"))              # 퀴즈 모듈
sys.path.insert(0, BASE_DIR)                                    # 루트 디렉토리

# === 기본 imports ===
from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from contextlib import asynccontextmanager
from pydantic import BaseModel, Field
import logging
from datetime import datetime
from typing import List, Dict, Optional
from dotenv import load_dotenv
import uvicorn

# === 스팟 데이터 안전한 import ===
try:
    from expanded_educational_spots import EXPANDED_EDUCATIONAL_SPOTS
    from education_curriculum_integration import EducationCurriculumIntegrator
    spots_available = True
    print("✅ 스팟 데이터 모듈 로드 성공")
except ImportError as e:
    print(f"⚠️ 스팟 데이터 import 실패: {e}")
    EXPANDED_EDUCATIONAL_SPOTS = []
    EducationCurriculumIntegrator = None
    spots_available = False

# === 포즈 분석 모듈 안전한 import ===
try:
    from service.AI_ObjectDetector import AI_ObjectDetector
    from service.AI_Analyze import AI_Analyze
    pose_modules_available = True
    print("✅ 포즈 분석 모듈 로드 성공")
except ImportError as e:
    print(f"⚠️ 포즈 분석 모듈 로드 실패: {e}")
    AI_ObjectDetector = None
    AI_Analyze = None
    pose_modules_available = False

# === 환경설정 ===
load_dotenv()
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

# === 전역 변수 ===
quiz_service: Optional[object] = None
pose_service: Optional[object] = None

system_status = {
    "quiz_service_ready": False,
    "pose_service_ready": False,
    "spots_available": spots_available,
    "pose_modules_available": pose_modules_available,
    "startup_time": None,
}

# === 요청/응답 모델 ===
class ProblemGenerateRequestToAI(BaseModel):
    spotName: str = Field(..., description="스팟명", example="근정전")
    problemCnt: int = Field(..., ge=1, le=10, description="생성할 문제 개수")

class ProblemGenerateRequestFromSpotId(BaseModel):
    spotId: int = Field(..., description="스팟 ID")
    problemCnt: int = Field(..., ge=1, le=10, description="생성할 문제 개수")
    grade: Optional[int] = Field(default=5, ge=1, le=6, description="학년")

class GeneratedQuizProblem(BaseModel):
    question: str = Field(..., description="문제 텍스트")
    choices: List[str] = Field(..., description="선택지 리스트")
    correctIndex: int = Field(..., ge=0, le=2, description="정답 인덱스")
    explanation: str = Field(..., description="해설")

class ProblemGenerateResponse(BaseModel):
    success: bool = Field(default=True)
    problems: List[GeneratedQuizProblem] = Field(..., description="생성된 문제 리스트")
    generation_info: Dict = Field(default_factory=dict, description="생성 정보")

class SimplePoseResponse(BaseModel):
    success: bool = Field(..., description="포즈 조건 만족 여부")
    result: str = Field(..., description="분석 결과 메시지")

# === 설정 클래스 ===
class SimpleSettings:
    def __init__(self):
        self.YOLO_MODEL_PATH = os.path.join(BASE_DIR, "object_detect", "model", "yolov8m.pt")
        self.IMAGE_RESIZE_MAX = 1024
        self.OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
        self.GMS_API_KEY = os.getenv("GMS_API_KEY")
        self.API_KEY = os.getenv("API_KEY")

settings = SimpleSettings()

# === 인라인 서비스 구현 (간소화) ===
import openai
import numpy as np
import cv2
import re
import random

class SimpleQuizService:
    """간소화된 퀴즈 서비스"""
    
    def __init__(self, settings):
        self.settings = settings
        self.openai_client = None
        self.is_ready = False
        self.stats = {"generated": 0, "llm_success": 0, "fallback_used": 0}
        
        # 스팟 데이터베이스 구축
        self.spots_database = {}
        self.spots_by_name = {}
        self._load_spots()
        
        # OpenAI 클라이언트 초기화
        self._setup_openai()
        
    def _load_spots(self):
        """스팟 데이터 로딩"""
        if EXPANDED_EDUCATIONAL_SPOTS:
            for idx, spot in enumerate(EXPANDED_EDUCATIONAL_SPOTS):
                spot_id = idx + 1
                self.spots_database[spot_id] = spot
                self.spots_by_name[spot["세부스팟"]] = {**spot, "spot_id": spot_id}
                if spot["메인장소"] not in self.spots_by_name:
                    self.spots_by_name[spot["메인장소"]] = {**spot, "spot_id": spot_id}
            
            print(f"📊 스팟 데이터베이스 로딩: {len(self.spots_database)}개")
        else:
            print("⚠️ 스팟 데이터가 없습니다. 기본 모드로 실행")
    
    def _setup_openai(self):
        """OpenAI 클라이언트 설정"""
        api_keys = [
            ("GMS_API_KEY", "https://gms.ssafy.io/gmsapi/api.openai.com/v1"),
            ("OPENAI_API_KEY", "https://api.openai.com/v1"),
            ("API_KEY", "https://gms.ssafy.io/gmsapi/api.openai.com/v1"),
        ]
        
        for key_name, base_url in api_keys:
            api_key = getattr(self.settings, key_name)
            if api_key and len(api_key.strip()) >= 20:
                try:
                    self.openai_client = openai.OpenAI(
                        api_key=api_key.strip(), base_url=base_url
                    )
                    # 연결 테스트
                    test_response = self.openai_client.chat.completions.create(
                        model="gpt-4o-mini",
                        messages=[{"role": "user", "content": "test"}],
                        max_tokens=5,
                        timeout=10,
                    )
                    print(f"✅ {key_name} API 연결 성공")
                    break
                except Exception as e:
                    print(f"⚠️ {key_name} 연결 실패: {e}")
                    continue
        
        self.is_ready = True
    
    def find_spot_by_name(self, spot_name: str) -> Optional[Dict]:
        """스팟명으로 검색"""
        if spot_name in self.spots_by_name:
            return self.spots_by_name[spot_name]
        
        for name, spot_info in self.spots_by_name.items():
            if spot_name in name or name in spot_name:
                return spot_info
        return None
    
    def find_spot_by_id(self, spot_id: int) -> Optional[Dict]:
        """스팟 ID로 검색"""
        return self.spots_database.get(spot_id)
    
    def generate_quiz(self, spot_info: Dict, grade: int = 5) -> Dict:
        """퀴즈 생성"""
        self.stats["generated"] += 1
        
        # LLM 시도
        if self.openai_client:
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

                response = self.openai_client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[
                        {"role": "system", "content": "당신은 교육 전문가입니다."},
                        {"role": "user", "content": prompt}
                    ],
                    max_tokens=400,
                    temperature=0.3,
                    timeout=15,
                )
                
                # 간단한 파싱
                llm_text = response.choices[0].message.content.strip()
                parsed = self._parse_quiz(llm_text)
                if parsed:
                    self.stats["llm_success"] += 1
                    return parsed
                
            except Exception as e:
                logger.error(f"LLM 퀴즈 생성 실패: {e}")
        
        # 폴백
        self.stats["fallback_used"] += 1
        return self._generate_fallback(spot_info, grade)
    
    def _parse_quiz(self, text: str) -> Optional[Dict]:
        """퀴즈 파싱"""
        try:
            question_match = re.search(r"문제\s*[:：]\s*(.+?)(?=1\)|①)", text, re.DOTALL)
            if not question_match:
                return None
                
            question = question_match.group(1).strip()
            
            choices_match = re.search(r"1\)\s*([^2]+?)\s*2\)\s*([^3]+?)\s*3\)\s*([^정답]+?)(?=정답|$)", text, re.DOTALL)
            if not choices_match:
                return None
                
            choices = [choices_match.group(i).strip() for i in range(1, 4)]
            
            answer_match = re.search(r"정답\s*[:：]\s*(\d+)", text)
            correct_index = int(answer_match.group(1)) - 1 if answer_match else 0
            
            explanation_match = re.search(r"해설\s*[:：]\s*(.+?)$", text, re.DOTALL)
            explanation = explanation_match.group(1).strip() if explanation_match else "해설이 없습니다."
            
            return {
                "question": question,
                "choices": choices,
                "correctIndex": correct_index,
                "explanation": explanation,
            }
        except:
            return None
    
    def _generate_fallback(self, spot_info: Dict, grade: int) -> Dict:
        """폴백 퀴즈"""
        spot_name = spot_info.get("세부스팟", "이곳")
        location = spot_info.get("메인장소", "서울")
        
        return {
            "question": f"{spot_name}은 어디에 있나요?",
            "choices": [location, "부산", "제주도"],
            "correctIndex": 0,
            "explanation": f"{spot_name}은 {location}에 있는 중요한 곳입니다.",
        }
    
    def get_stats(self) -> Dict:
        return self.stats

class SimplePoseService:
    """간소화된 포즈 서비스"""
    
    def __init__(self, settings):
        self.settings = settings
        self.ai_model = None
        self.is_ready = False
        
        if pose_modules_available:
            try:
                self.ai_model = AI_ObjectDetector(settings.YOLO_MODEL_PATH)
                self.is_ready = True
                print("✅ 포즈 분석 모델 초기화 완료")
            except Exception as e:
                print(f"❌ 포즈 분석 모델 초기화 실패: {e}")
                self.is_ready = False
        else:
            print("⚠️ 포즈 분석 모듈 없음")
    
    async def analyze_pose(self, file: UploadFile, pose_select: str) -> Dict:
        """포즈 분석"""
        if not self.is_ready:
            return {"success": False, "result": "포즈 분석 서비스가 준비되지 않았습니다"}
        
        try:
            # 이미지 읽기
            contents = await file.read()
            np_arr = np.frombuffer(contents, np.uint8)
            image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
            
            if image is None:
                return {"success": False, "result": "이미지 디코딩 실패"}
            
            # 포즈 분석
            image, results, poses_info = self.ai_model.Load_image(image)
            
            if results is None or not poses_info:
                return {"success": False, "result": "사람을 찾을 수 없습니다"}
            
            analyzer = AI_Analyze(image, results, poses_info)
            pose_result = analyzer.print_keypoints(pose_select=pose_select)
            
            success = "Success" in str(pose_result) if isinstance(pose_result, str) else bool(pose_result)
            
            return {
                "success": success,
                "result": str(pose_result),
                "detected_people": len(poses_info)
            }
            
        except Exception as e:
            return {"success": False, "result": f"분석 오류: {str(e)}"}
    
    def get_stats(self) -> Dict:
        return {"is_ready": self.is_ready}

# === 서비스 초기화 ===
async def initialize_services():
    """서비스들 초기화"""
    global quiz_service, pose_service, system_status
    
    logger.info("🚀 서비스 초기화 시작")
    
    try:
        # 퀴즈 서비스
        quiz_service = SimpleQuizService(settings)
        system_status["quiz_service_ready"] = quiz_service.is_ready
        
        # 포즈 서비스
        pose_service = SimplePoseService(settings)
        system_status["pose_service_ready"] = pose_service.is_ready
        
        ready_count = sum([
            system_status["quiz_service_ready"],
            system_status["pose_service_ready"]
        ])
        
        logger.info(f"✅ 서비스 초기화 완료: {ready_count}/2")
        logger.info(f"   퀴즈 생성: {'✅' if system_status['quiz_service_ready'] else '❌'}")
        logger.info(f"   포즈 분석: {'✅' if system_status['pose_service_ready'] else '❌'}")
        
    except Exception as e:
        logger.error(f"❌ 서비스 초기화 실패: {e}")

# === 라이프사이클 ===
@asynccontextmanager
async def lifespan(app: FastAPI):
    system_status["startup_time"] = datetime.now().isoformat()
    await initialize_services()
    yield
    logger.info("🔄 서버 종료")

# FastAPI 앱
app = FastAPI(
    title="ARGO AI 통합 서버",
    description="퀴즈 생성 + 포즈 분석 (Import 에러 해결)",
    version="v2.1",
    lifespan=lifespan,
)

# === API 엔드포인트 ===

@app.post("/generate-problem", response_model=ProblemGenerateResponse)
async def generate_problem_by_spot_name(request: ProblemGenerateRequestToAI):
    """퀴즈 생성 (spotName 기반)"""
    if not system_status["quiz_service_ready"]:
        raise HTTPException(status_code=503, detail="퀴즈 서비스가 준비되지 않았습니다")

    try:
        spot_info = quiz_service.find_spot_by_name(request.spotName)
        if not spot_info:
            raise HTTPException(
                status_code=404, detail=f"스팟 '{request.spotName}'을 찾을 수 없습니다."
            )

        problems = []
        for i in range(request.problemCnt):
            grade = 3 + (i % 4)  # 3,4,5,6 순환
            quiz = quiz_service.generate_quiz(spot_info, grade)
            problems.append(GeneratedQuizProblem(**quiz))

        return ProblemGenerateResponse(
            success=True,
            problems=problems,
            generation_info={
                "spot_name": request.spotName,
                "generated_count": len(problems),
                "stats": quiz_service.get_stats()
            }
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ 퀴즈 생성 실패: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/generate-problem-by-id", response_model=ProblemGenerateResponse)
async def generate_problem_by_spot_id(request: ProblemGenerateRequestFromSpotId):
    """퀴즈 생성 (spotId 기반)"""
    if not system_status["quiz_service_ready"]:
        raise HTTPException(status_code=503, detail="퀴즈 서비스가 준비되지 않았습니다")

    try:
        spot_info = quiz_service.find_spot_by_id(request.spotId)
        if not spot_info:
            raise HTTPException(
                status_code=404, detail=f"스팟 ID {request.spotId}를 찾을 수 없습니다."
            )

        problems = []
        for i in range(request.problemCnt):
            quiz = quiz_service.generate_quiz(spot_info, request.grade)
            problems.append(GeneratedQuizProblem(**quiz))

        return ProblemGenerateResponse(
            success=True,
            problems=problems,
            generation_info={
                "spot_id": request.spotId,
                "grade": request.grade,
                "generated_count": len(problems),
                "stats": quiz_service.get_stats()
            }
        )

    except Exception as e:
        logger.error(f"❌ SpotID 퀴즈 생성 실패: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/pose/predict", response_model=SimplePoseResponse)
async def analyze_pose_simple(file: UploadFile = File(...), pose_select: str = Form(...)):
    """포즈 분석"""
    if not system_status["pose_service_ready"]:
        raise HTTPException(status_code=503, detail="포즈 분석 서비스가 준비되지 않았습니다")

    try:
        result = await pose_service.analyze_pose(file, pose_select)
        
        return SimplePoseResponse(
            success=result["success"],
            result=result["result"]
        )

    except Exception as e:
        logger.error(f"❌ 포즈 분석 실패: {e}")
        return SimplePoseResponse(success=False, result=f"분석 오류: {str(e)}")

@app.get("/health")
async def health_check():
    """시스템 상태"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "services": {
            "quiz_generation": system_status["quiz_service_ready"],
            "pose_analysis": system_status["pose_service_ready"],
        },
        "modules": {
            "spots_available": system_status["spots_available"],
            "pose_modules_available": system_status["pose_modules_available"],
        },
        "startup_time": system_status["startup_time"]
    }

@app.get("/")
async def root():
    """루트"""
    return {
        "title": "ARGO AI 통합 서버 (Import 에러 해결)",
        "version": "v2.1",
        "fix": "✅ sys.path 경로 문제 해결 완료",
        "endpoints": {
            "quiz_name": "POST /generate-problem",
            "quiz_id": "POST /generate-problem-by-id", 
            "pose": "POST /pose/predict",
            "health": "GET /health"
        },
        "status": system_status
    }

if __name__ == "__main__":
    print("🚀 ARGO AI 통합 서버 시작 (Import 에러 해결 완료)")
    print("🔧 수정사항:")
    print("   - sys.path 경로 문제 해결")
    print("   - 안전한 import with try/except")
    print("   - 인라인 서비스 구현으로 의존성 최소화")
    print("")
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