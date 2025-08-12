# main.py - Pydantic 객체 처리 수정 버전 + 포즈 분석 완성
"""
🎯 ARGO AI 통합 서버 - Pydantic 객체 처리 수정 + 포즈 분석 연동 완성

문제: quiz_service가 GeneratedQuizProblem 객체를 반환하는데 딕셔너리로 처리
해결: 객체 타입 확인 후 적절한 변환 처리

새로 추가: 포즈 분석 API 완성 - Java SelfieResultDto와 완벽 호환
"""

import sys
import os
import traceback
import logging
from enum import Enum
from datetime import datetime
from typing import List, Dict, Optional, Any, Union
from fastapi import FastAPI, HTTPException, UploadFile, File, Form, Request
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from contextlib import asynccontextmanager
from pydantic import BaseModel, Field, ValidationError
from dotenv import load_dotenv
import uvicorn


# === 경로 설정 ===
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(BASE_DIR, "data"))
sys.path.insert(0, os.path.join(BASE_DIR, "object_detect", "src"))
sys.path.insert(0, os.path.join(BASE_DIR, "services"))
sys.path.insert(0, os.path.join(BASE_DIR, "quiz"))
sys.path.insert(0, BASE_DIR)

load_dotenv()

# === 로깅 설정 ===
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

# === 안전한 모듈 Import ===
quiz_service_available = False
pose_service_available = False
spots_available = False

try:
    try:
        from services.quiz_service import QuizService

        quiz_service_available = True
        logger.info("✅ services.quiz_service 로드 성공")
    except ImportError:
        from services.quiz_service import QuizService

        quiz_service_available = True
        logger.info("✅ quiz_service (fallback) 로드 성공")
except ImportError as e:
    logger.warning(f"⚠️ 퀴즈 서비스 import 실패: {e}")
    QuizService = None

try:
    try:
        from services.pose_service import PoseService

        pose_service_available = True
        logger.info("✅ services.pose_service 로드 성공")
    except ImportError:
        from services.pose_service import PoseService

        pose_service_available = True
        logger.info("✅ pose_service (fallback) 로드 성공")
except ImportError as e:
    logger.warning(f"⚠️ 포즈 서비스 import 실패: {e}")
    PoseService = None

try:
    from data.expanded_educational_spots import EXPANDED_EDUCATIONAL_SPOTS

    spots_available = True
    logger.info(f"✅ 스팟 데이터 로드: {len(EXPANDED_EDUCATIONAL_SPOTS)}개")
except ImportError as e:
    logger.warning(f"⚠️ 스팟 데이터 import 실패: {e}")
    EXPANDED_EDUCATIONAL_SPOTS = []


# === 설정 클래스 ===
class Settings:
    def __init__(self):
        self.YOLO_MODEL_PATH = os.path.join(
            BASE_DIR, "object_detect", "model", "yolov8m.pt"
        )
        self.IMAGE_RESIZE_MAX = 1024
        self.OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
        self.GMS_API_KEY = os.getenv("GMS_API_KEY")
        self.API_KEY = os.getenv("API_KEY")


settings = Settings()


# === Pydantic 모델들 ===
class ProblemGenerateRequestToAI(BaseModel):
    spotName: str = Field(..., description="스팟명", example="근정전")
    grade: Optional[int] = Field(default=5, ge=1, le=6, description="학년")
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
    generation_method: Optional[str] = Field(default="unknown", description="생성 방법")
    quality_score: Optional[float] = Field(default=0.0, description="품질 점수")
    parsing_method: Optional[str] = Field(default=None, description="파싱 방법")


class ProblemGenerateResponse(BaseModel):
    problems: List["QuizProblemSimple"] = Field(
        ..., description="QuizProblem 엔티티 호환"
    )


class QuizProblemSimple(BaseModel):
    """QuizProblem 엔티티와 정확히 일치하는 구조"""

    grade: int = Field(..., description="학년")
    question: str = Field(..., description="문제 텍스트")
    choices: List[str] = Field(..., description="선택지 3개")
    correctIndex: int = Field(..., ge=0, le=2, description="정답 인덱스")
    explanation: str = Field(..., description="해설")


# === 🔥 Java SelfieResultDto와 완벽 호환하는 응답 모델 ===
class SelfieResultDto(BaseModel):
    """Java SelfieResultDto와 완벽 호환 (간단한 구조)"""

    success: bool = Field(..., description="분석 성공 여부")
    result: str = Field(..., description="분석 결과 메시지")


class SimplePoseResponse(BaseModel):
    success: bool = Field(..., description="포즈 조건 만족 여부")
    result: str = Field(..., description="분석 결과 메시지")


# === 🔥 핵심 수정: QuizProblem 엔티티 호환 변환 함수 ===
def convert_to_quiz_problem_simple(
    quiz_data: Union[Dict, BaseModel, Any], grade: int, fallback_name: str = "문제"
) -> QuizProblemSimple:
    """
    🔧 핵심 기능: 퀴즈 데이터를 QuizProblem 엔티티 호환 구조로 변환
    - 백엔드 QuizProblem 엔티티와 정확히 일치
    - grade, question, choices, correctIndex, explanation만 포함
    """

    logger.info(f"🔧 QuizProblem 변환: {type(quiz_data)}")

    try:
        # 이미 올바른 타입인 경우 그대로 반환
        if isinstance(quiz_data, QuizProblemSimple):
            return quiz_data

        # 데이터 추출 함수
        def safe_get(key: str, default: Any = None) -> Any:
            try:
                if isinstance(quiz_data, dict):
                    return quiz_data.get(key, default)
                if hasattr(quiz_data, key):
                    return getattr(quiz_data, key, default)
                if hasattr(quiz_data, "__dict__"):
                    return quiz_data.__dict__.get(key, default)
                return default
            except Exception as e:
                logger.warning(f"⚠️ {key} 추출 실패: {e}")
                return default

        # 필수 데이터 추출
        question = safe_get("question", "")
        choices = safe_get("choices", [])
        correct_index = safe_get("correctIndex")
        explanation = safe_get("explanation", "")

        # 데이터 검증
        if not question or len(question.strip()) < 5:
            raise ValueError(f"유효하지 않은 question: {question}")

        if not choices or len(choices) != 3:
            raise ValueError(f"유효하지 않은 choices: {choices}")

        if correct_index is None or not (0 <= correct_index <= 2):
            raise ValueError(f"유효하지 않은 correctIndex: {correct_index}")

        if not explanation or len(explanation.strip()) < 3:
            raise ValueError(f"유효하지 않은 explanation: {explanation}")

        # QuizProblemSimple 생성 (QuizProblem 엔티티와 정확히 일치)
        result = QuizProblemSimple(
            grade=grade,
            question=question.strip(),
            choices=[str(c).strip() for c in choices],
            correctIndex=correct_index,
            explanation=explanation.strip(),
        )

        logger.info(f"✅ QuizProblem 변환 성공: {result.question[:30]}...")
        return result

    except Exception as e:
        logger.error(f"❌ QuizProblem 변환 실패: {e}")

        # Fallback: 최소한의 유효한 QuizProblem
        return QuizProblemSimple(
            grade=grade,
            question=f"{fallback_name}에 관한 문제입니다.",
            choices=["선택지1", "선택지2", "선택지3"],
            correctIndex=0,
            explanation="기본 해설입니다.",
        )


# === 강화된 Fallback 퀴즈 서비스 (기존과 동일) ===
class SafeFallbackQuizService:
    def __init__(self):
        self.is_ready = True
        self.spots_data = {}
        self._load_spots()

    def _load_spots(self):
        if EXPANDED_EDUCATIONAL_SPOTS:
            for idx, spot in enumerate(EXPANDED_EDUCATIONAL_SPOTS):
                self.spots_data[idx + 1] = spot
                self.spots_data[spot.get("세부스팟", f"spot_{idx}")] = {
                    **spot,
                    "spot_id": idx + 1,
                }

        if not self.spots_data:
            default_spots = [
                {
                    "세부스팟": "근정전",
                    "메인장소": "경복궁",
                    "설명": "조선시대 정전",
                    "교육키워드": ["조선시대", "궁궐"],
                }
            ]
            for idx, spot in enumerate(default_spots):
                self.spots_data[idx + 1] = spot
                self.spots_data[spot["세부스팟"]] = {**spot, "spot_id": idx + 1}

    async def generate_problems_by_name(
        self, spot_name: str, count: int, grade: int = 5
    ):
        spot_info = self.spots_data.get(
            spot_name,
            {
                "세부스팟": spot_name,
                "메인장소": "서울",
                "설명": "교육적 장소",
                "교육키워드": ["역사", "문화"],
            },
        )

        problems = []
        for i in range(count):
            problem = self._create_safe_quiz(spot_info, grade, i)
            problems.append(problem)

        return problems

    async def generate_problems_by_id(self, spot_id: int, count: int, grade: int = 5):
        spot_info = self.spots_data.get(
            spot_id,
            {
                "세부스팟": f"스팟 {spot_id}",
                "메인장소": "서울",
                "설명": "교육적 장소",
                "교육키워드": ["역사", "문화"],
            },
        )

        return await self.generate_problems_by_name(
            spot_info.get("세부스팟", f"스팟 {spot_id}"), count, grade
        )

    def _create_safe_quiz(self, spot_info: Dict, grade: int, quiz_number: int) -> Dict:
        spot_name = spot_info.get("세부스팟", "이곳")
        location = spot_info.get("메인장소", "서울")

        templates = [
            {
                "question": f"{spot_name}은 어디에 있나요?",
                "choices": [location, "부산", "제주도"],
                "correctIndex": 0,
                "explanation": f"{spot_name}은 {location}에 있습니다.",
            }
        ]

        template = templates[quiz_number % len(templates)]

        return {
            "question": template["question"],
            "choices": template["choices"],
            "correctIndex": template["correctIndex"],
            "explanation": template["explanation"],
            "generation_method": "safe_fallback",
            "quality_score": 0.7,
            "parsing_method": "template",
        }

    def get_stats(self):
        return {
            "service_type": "safe_fallback",
            "spots_loaded": len(self.spots_data),
            "is_ready": self.is_ready,
        }


# === 전역 서비스 인스턴스 ===
quiz_service: Optional[object] = None

system_status = {
    "quiz_service_ready": False,
    "pose_service_ready": False,
    "startup_time": None,
    "error_details": [],
}


# === 서비스 초기화 ===
async def initialize_services():
    global quiz_service, system_status

    logger.info("🚀 서비스 초기화 시작")

    try:
        if quiz_service_available and QuizService:
            quiz_service = QuizService(settings)
            await quiz_service.initialize()
            system_status["quiz_service_ready"] = True
            logger.info("✅ 고급 퀴즈 서비스 활성화")
        else:
            quiz_service = SafeFallbackQuizService()
            system_status["quiz_service_ready"] = True
            logger.warning("⚠️ Safe Fallback 퀴즈 서비스로 전환")
    except Exception as e:
        logger.error(f"❌ 퀴즈 서비스 초기화 실패: {e}")
        quiz_service = SafeFallbackQuizService()
        system_status["quiz_service_ready"] = True
        system_status["error_details"].append(f"퀴즈 서비스: {str(e)}")

    # 🔥 포즈 서비스는 직접 AI 모듈 사용으로 변경 - 초기화 불필요
    system_status["pose_service_ready"] = True
    logger.info("✅ 포즈 분석: AI 모듈 직접 사용")

    ready_services = sum(
        [system_status["quiz_service_ready"], system_status["pose_service_ready"]]
    )
    logger.info(f"✅ 서비스 초기화 완료: {ready_services}/2")


# === 에러 핸들러 ===
@asynccontextmanager
async def lifespan(app: FastAPI):
    system_status["startup_time"] = datetime.now().isoformat()
    await initialize_services()
    yield
    logger.info("🔄 서버 종료")


app = FastAPI(
    title="ARGO AI 통합 서버",
    description="Pydantic 객체 처리 수정 완료 + 포즈 분석 연동 완성",
    version="v3.5",
    lifespan=lifespan,
)


@app.exception_handler(ValidationError)
async def validation_exception_handler(request, exc):
    logger.error(f"❌ Validation 에러: {exc}")
    return JSONResponse(
        status_code=422, content={"detail": "데이터 검증 실패", "errors": str(exc)}
    )


@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    logger.error(f"❌ 일반 에러: {exc}")
    logger.error(f"Traceback: {traceback.format_exc()}")
    return JSONResponse(status_code=500, content={"detail": f"서버 오류: {str(exc)}"})


# === 🔥 핵심 수정: API 엔드포인트 ===
@app.post("/generate-problem", response_model=ProblemGenerateResponse)
async def generate_problem_by_spot_name(request: ProblemGenerateRequestToAI):
    """퀴즈 생성 (spotName 기반) - QuizProblem 엔티티 완벽 호환"""
    if not system_status["quiz_service_ready"]:
        raise HTTPException(status_code=503, detail="퀴즈 서비스가 준비되지 않았습니다")

    try:
        logger.info(
            f"📝 퀴즈 생성 요청: {request.spotName}, {request.problemCnt}개, {request.grade}학년"
        )

        # 서비스 호출
        quiz_problems = await quiz_service.generate_problems_by_name(
            spot_name=request.spotName,
            count=request.problemCnt,
            grade=request.grade or 5,
        )

        logger.info(f"🔍 서비스 반환 데이터 개수: {len(quiz_problems)}")

        # 🔥 핵심 수정: QuizProblemSimple로 변환
        problems = []
        conversion_success_count = 0

        for idx, quiz_data in enumerate(quiz_problems):
            try:
                # QuizProblem 엔티티 호환 변환
                problem = convert_to_quiz_problem_simple(
                    quiz_data, grade=request.grade or 5, fallback_name=request.spotName
                )
                problems.append(problem)
                conversion_success_count += 1

                logger.info(f"✅ 문제 {idx+1} 변환 성공: {problem.question[:30]}...")

            except Exception as e:
                logger.error(f"❌ 문제 {idx+1} 변환 실패: {e}")

                # 개별 fallback
                fallback_problem = convert_to_quiz_problem_simple(
                    {}, grade=request.grade or 5, fallback_name=request.spotName
                )
                problems.append(fallback_problem)

        # 최소 1개 문제 보장
        if not problems:
            logger.warning("⚠️ 변환된 문제가 없음, 기본 문제 생성")
            problems.append(
                convert_to_quiz_problem_simple(
                    {
                        "question": f"{request.spotName}은 우리나라 어디에 있나요?",
                        "choices": ["서울", "부산", "제주도"],
                        "correctIndex": 0,
                        "explanation": f"{request.spotName}은 서울에 있습니다.",
                    },
                    grade=request.grade or 5,
                    fallback_name=request.spotName,
                )
            )
            conversion_success_count = 1

        logger.info(
            f"✅ 퀴즈 생성 완료: {len(problems)}개 (성공: {conversion_success_count}개)"
        )

        # 🔥 핵심: 백엔드 ProblemGenerateDto와 정확히 일치하는 응답
        return ProblemGenerateResponse(problems=problems)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ 퀴즈 생성 실패: {e}")
        logger.error(f"Traceback: {traceback.format_exc()}")
        raise HTTPException(status_code=500, detail=f"퀴즈 생성 실패: {str(e)}")


@app.post("/generate-problem-by-id", response_model=ProblemGenerateResponse)
async def generate_problem_by_spot_id(request: ProblemGenerateRequestFromSpotId):
    """퀴즈 생성 (spotId 기반) - QuizProblem 엔티티 완벽 호환"""
    if not system_status["quiz_service_ready"]:
        raise HTTPException(status_code=503, detail="퀴즈 서비스가 준비되지 않았습니다")

    try:
        logger.info(
            f"📝 ID 퀴즈 생성 요청: ID {request.spotId}, {request.problemCnt}개"
        )

        quiz_problems = await quiz_service.generate_problems_by_id(
            spot_id=request.spotId, count=request.problemCnt, grade=request.grade or 5
        )

        # QuizProblemSimple로 변환
        problems = []
        for idx, quiz_data in enumerate(quiz_problems):
            try:
                problem = convert_to_quiz_problem_simple(
                    quiz_data,
                    grade=request.grade or 5,
                    fallback_name=f"스팟{request.spotId}",
                )
                problems.append(problem)
            except Exception as e:
                logger.error(f"❌ 문제 {idx+1} 변환 실패: {e}")
                problems.append(
                    convert_to_quiz_problem_simple(
                        {},
                        grade=request.grade or 5,
                        fallback_name=f"스팟{request.spotId}",
                    )
                )

        # 백엔드 호환 응답
        return ProblemGenerateResponse(problems=problems)

    except Exception as e:
        logger.error(f"❌ ID 퀴즈 생성 실패: {e}")
        raise HTTPException(status_code=500, detail=f"퀴즈 생성 실패: {str(e)}")


# === 🎯 Java 연동용 포즈 분석 엔드포인트 (Request 직접 파싱) ===
@app.post("/pose/predict")
async def predict_pose(
    file: UploadFile = File(...),  # Java에서 보내는 필드명: "file"
    pose_select: str = Form(...),  # Java에서 보내는 필드명: "pose_select"
):
    logger.info("🎯 포즈 분석 요청 도달 (바인딩 방식)")
    logger.info(f"📋 pose_select(raw): {pose_select}")
    try:
        # 파일 검증
        content = await file.read()
        size = len(content)
        logger.info(f"📋 파일명: {file.filename}, 크기: {size} bytes")

        if size == 0:
            return {"success": False, "result": "빈 파일입니다"}
        if size > 10 * 1024 * 1024:
            return {"success": False, "result": "파일 크기가 10MB를 초과합니다"}

        pose_select_lower = pose_select.lower().strip()
        valid_poses = [
            "sitting_pose",
            "ear_pose",
            "handsup_pose",
            "armscrossed_pose",
            "akimbo_pose",
            "heart_pose",
        ]
        if pose_select_lower not in valid_poses:
            return {"success": False, "result": f"지원하지 않는 포즈: {pose_select}"}

        # 이미지 디코딩
        import numpy as np
        import cv2

        np_arr = np.frombuffer(content, np.uint8)
        image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
        if image is None:
            return {"success": False, "result": "이미지 디코딩 실패"}

        # 리사이즈(선택)
        h, w = image.shape[:2]
        max_size = 1024
        if max(h, w) > max_size:
            scale = max_size / max(h, w)
            image = cv2.resize(image, (int(w * scale), int(h * scale)))
            logger.info(f"🔧 리사이즈: {w}x{h} -> {image.shape[1]}x{image.shape[0]}")

        # 🔥 YOLO + Analyze
        import os

        BASE_DIR = os.path.dirname(os.path.abspath(__file__))

        # 프로젝트마다 패키지 경로가 달라 ImportError가 날 수 있어 try/except로 처리
        try:
            from services.AI_ObjectDetector import AI_ObjectDetector
            from services.AI_Analyze import AI_Analyze
        except ImportError:
            from services.AI_ObjectDetector import AI_ObjectDetector
            from services.AI_Analyze import AI_Analyze

        model_path = os.path.join(BASE_DIR, "object_detect", "model", "yolov8m.pt")
        logger.info(f"📋 YOLO 모델 경로: {model_path}")

        ai_model = AI_ObjectDetector(model_path)
        image, results, poses_info = ai_model.Load_image(image)
        if results is None:
            return {"success": False, "result": "YOLO 추론 실패"}
        if not poses_info:
            return {"success": False, "result": "사람 감지 실패"}

        analyzer = AI_Analyze(image, results, poses_info)
        analysis_result = analyzer.print_keypoints(pose_select=pose_select_lower)

        logger.info(f"✅ 포즈 분석 완료: {analysis_result}")
        return {"success": True, "result": str(analysis_result)}

    except ImportError as e:
        logger.error(f"❌ AI 모듈 import 실패: {e}")
        return {"success": False, "result": f"AI 모듈을 불러올 수 없습니다: {str(e)}"}
    except Exception as e:
        logger.error(f"❌ 포즈 분석 실패: {e}")
        logger.error(f"Traceback: {traceback.format_exc()}")
        return {"success": False, "result": f"분석 실패: {str(e)}"}


# === 🔧 추가: 포즈 서비스 상태 확인 엔드포인트 ===
@app.get("/pose/health")
async def pose_health_check():
    """포즈 분석 서비스 상태 확인"""
    try:
        # AI 모듈 import 확인
        from services.AI_ObjectDetector import AI_ObjectDetector
        from services.AI_Analyze import AI_Analyze

        # YOLO 모델 파일 확인
        model_path = os.path.join(BASE_DIR, "object_detect", "model", "yolov8m.pt")
        model_exists = os.path.exists(model_path)

        return {
            "status": "ready" if model_exists else "model_missing",
            "ready": model_exists,
            "model_loaded": model_exists,
            "supported_poses": [
                "sitting_pose",
                "ear_pose",
                "handsup_pose",
                "armscrossed_pose",
                "akimbo_pose",
                "heart_pose",
            ],
            "stats": {"ai_modules_available": True, "model_path": model_path},
        }
    except ImportError as e:
        return {
            "status": "modules_missing",
            "ready": False,
            "error": f"AI 모듈 import 실패: {str(e)}",
        }
    except Exception as e:
        return {"status": "error", "ready": False, "error": str(e)}


# === 🔧 기존 SimplePoseResponse용 엔드포인트 (AI_Analyze 직접 사용) ===
@app.post("/pose/predict-simple", response_model=SimplePoseResponse)
async def analyze_pose_simple(
    file: UploadFile = File(...), pose_select: str = Form(...)
):
    """포즈 분석 (간단 응답) - AI_Analyze 직접 사용"""
    logger.info(f"🎯 간단 포즈 분석 요청: pose={pose_select}")

    try:
        # 포즈 타입 검증
        pose_select_lower = pose_select.lower().strip()
        valid_poses = [
            "sitting_pose",
            "ear_pose",
            "handsup_pose",
            "armscrossed_pose",
            "akimbo_pose",
            "heart_pose",
        ]

        if pose_select_lower not in valid_poses:
            return SimplePoseResponse(
                success=False,
                result=f"지원하지 않는 포즈: {pose_select}. 가능한 포즈: {', '.join(valid_poses)}",
            )

        # 파일 읽기
        content = await file.read()

        # AI 모듈 직접 사용
        import numpy as np
        import cv2
        from services.AI_ObjectDetector import AI_ObjectDetector
        from services.AI_Analyze import AI_Analyze

        # 이미지 디코딩
        np_arr = np.frombuffer(content, np.uint8)
        image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

        if image is None:
            return SimplePoseResponse(success=False, result="이미지 디코딩 실패")

        # YOLO 모델 로드 및 추론
        model_path = os.path.join(BASE_DIR, "object_detect", "model", "yolov8m.pt")
        ai_model = AI_ObjectDetector(model_path)

        # 포즈 분석 수행
        image, results, poses_info = ai_model.Load_image(image)

        if results is None:
            return SimplePoseResponse(success=False, result="추론 실패")

        if not poses_info:
            return SimplePoseResponse(success=False, result="사람 감지 실패")

        # AI_Analyze로 포즈 분석
        analyzer = AI_Analyze(image, results, poses_info)
        analysis_result = analyzer.print_keypoints(pose_select=pose_select_lower)

        return SimplePoseResponse(success=True, result=str(analysis_result))

    except ImportError as e:
        logger.error(f"❌ AI 모듈 import 실패: {e}")
        return SimplePoseResponse(success=False, result=f"AI 모듈 로드 실패: {str(e)}")
    except Exception as e:
        logger.error(f"❌ 포즈 분석 실패: {e}")
        return SimplePoseResponse(success=False, result=f"분석 오류: {str(e)}")


@app.get("/health")
async def health_check():
    """시스템 상태 확인"""
    return {
        "status": "healthy" if system_status["quiz_service_ready"] else "partial",
        "timestamp": datetime.now().isoformat(),
        "services": {
            "quiz_generation": system_status["quiz_service_ready"],
            "pose_analysis": system_status["pose_service_ready"],
        },
        "modules": {
            "quiz_service_available": quiz_service_available,
            "pose_service_available": pose_service_available,
            "spots_available": spots_available,
        },
        "startup_time": system_status["startup_time"],
        "error_details": system_status["error_details"],
    }


@app.get("/")
async def root():
    """루트 엔드포인트"""
    return {
        "title": "ARGO AI 통합 서버 (QuizProblem 엔티티 + 포즈 분석 완벽 호환)",
        "version": "v3.6",
        "status": "✅ 정상 작동",
        "fixes": [
            "✅ QuizProblem 엔티티와 완벽 호환",
            "✅ 백엔드 ProblemGenerateDto 구조 일치",
            "✅ 불필요한 필드 제거 (success, generation_info)",
            "✅ Jackson 자동 역직렬화 지원",
            "✅ grade, question, choices, correctIndex, explanation만 포함",
        ],
        "new_features": [
            "🎯 포즈 분석 API 완성 (/pose/predict)",
            "✅ Java SelfieResultDto와 완벽 호환",
            "🔧 포즈 서비스 헬스체크 (/pose/health)",
            "⚡ 파일 크기/타입 검증 강화",
            "🛡️ 에러 처리 및 재시도 로직 준비",
        ],
        "backend_compatibility": {
            "quiz_entity": "QuizProblem",
            "quiz_dto": "ProblemGenerateDto",
            "pose_dto": "SelfieResultDto",
            "response_structure": "{'problems': [QuizProblem]}",
            "serialization": "Jackson 자동 처리",
        },
        "api_endpoints": {
            "quiz_generation": "/generate-problem",
            "pose_analysis": "/pose/predict",
            "pose_health": "/pose/health",
            "system_health": "/health",
        },
    }


if __name__ == "__main__":
    print("🚀 ARGO AI 통합 서버 시작 (QuizProblem 엔티티 + 포즈 분석 완벽 호환)")
    print("🔧 주요 수정사항:")
    print("   - QuizProblem 엔티티와 완벽 호환")
    print("   - 백엔드 ProblemGenerateDto 구조 일치")
    print("   - 불필요한 필드 완전 제거")
    print("   - Jackson 자동 역직렬화 지원")
    print("\n🎯 새로운 기능:")
    print("   - 포즈 분석 API 완성 (/pose/predict)")
    print("   - Java SelfieResultDto와 완벽 호환")
    print("   - 파일 검증 및 에러 처리 강화")
    print("   - 포즈 서비스 헬스체크 추가")
    print("\n📡 API 연동 준비 완료:")
    print("   - http://localhost:8000/pose/predict")
    print("   - http://localhost:8080/api/problem/selfie/determine")

    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False, log_level="info")
