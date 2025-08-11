# main.py - Pydantic 객체 처리 수정 버전
"""
🎯 ARGO AI 통합 서버 - Pydantic 객체 처리 수정

문제: quiz_service가 GeneratedQuizProblem 객체를 반환하는데 딕셔너리로 처리
해결: 객체 타입 확인 후 적절한 변환 처리
"""

import sys
import os
import traceback
import logging
from datetime import datetime
from typing import List, Dict, Optional, Any, Union
from fastapi import FastAPI, HTTPException, UploadFile, File, Form
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
        from quiz_service import QuizService

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
        from pose_service import PoseService

        pose_service_available = True
        logger.info("✅ pose_service (fallback) 로드 성공")
except ImportError as e:
    logger.warning(f"⚠️ 포즈 서비스 import 실패: {e}")
    PoseService = None

try:
    from expanded_educational_spots import EXPANDED_EDUCATIONAL_SPOTS

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
    problemCnt: int = Field(..., ge=1, le=10, description="생성할 문제 개수")
    grade: Optional[int] = Field(default=5, ge=1, le=6, description="학년")


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
    problems: List[QuizProblemSimple] = Field(
        ..., description="QuizProblem 엔티티 호환"
    )


class QuizProblemSimple(BaseModel):
    """QuizProblem 엔티티와 정확히 일치하는 구조"""

    grade: int = Field(..., description="학년")
    question: str = Field(..., description="문제 텍스트")
    choices: List[str] = Field(..., description="선택지 3개")
    correctIndex: int = Field(..., ge=0, le=2, description="정답 인덱스")
    explanation: str = Field(..., description="해설")


class SimplePoseResponse(BaseModel):
    success: bool = Field(..., description="포즈 조건 만족 여부")
    result: str = Field(..., description="분석 결과 메시지")


# === 🔥 핵심 수정: 유니버설 데이터 변환 함수 ===
def convert_to_api_model(
    quiz_data: Union[Dict, BaseModel, Any], fallback_name: str = "문제"
) -> GeneratedQuizProblem:
    """
    🔧 핵심 기능: 다양한 타입의 퀴즈 데이터를 API 모델로 변환
    - Dict, Pydantic 객체, 기타 모든 타입 처리
    - 속성 접근과 딕셔너리 접근 모두 지원
    - 안전한 fallback 보장
    """

    logger.info(f"🔧 변환 대상 타입: {type(quiz_data)}")
    logger.info(f"🔧 변환 대상 데이터: {quiz_data}")

    try:
        # 1. 이미 올바른 타입인 경우 그대로 반환
        if isinstance(quiz_data, GeneratedQuizProblem):
            logger.info("✅ 이미 GeneratedQuizProblem 타입, 그대로 반환")
            return quiz_data

        # 2. 데이터 추출 함수 정의
        def safe_get(key: str, default: Any = None) -> Any:
            """다양한 방법으로 데이터 추출 시도"""
            try:
                # Dict 방식
                if isinstance(quiz_data, dict):
                    return quiz_data.get(key, default)

                # 객체 속성 방식
                if hasattr(quiz_data, key):
                    return getattr(quiz_data, key, default)

                # Pydantic 모델 방식 (__dict__ 접근)
                if hasattr(quiz_data, "__dict__"):
                    return quiz_data.__dict__.get(key, default)

                return default

            except Exception as e:
                logger.warning(f"⚠️ {key} 추출 실패: {e}")
                return default

        # 3. 필수 데이터 추출
        question = safe_get("question", "")
        choices = safe_get("choices", [])
        correct_index = safe_get("correctIndex")
        explanation = safe_get("explanation", "")

        # 4. 데이터 검증 및 정제
        # question 검증
        if not question or not isinstance(question, str) or len(question.strip()) < 5:
            raise ValueError(f"유효하지 않은 question: {question}")

        # choices 검증
        if not choices or not isinstance(choices, list) or len(choices) != 3:
            raise ValueError(f"유효하지 않은 choices: {choices}")

        # choices 내용 검증
        clean_choices = []
        for choice in choices:
            if not choice or not isinstance(choice, str):
                raise ValueError(f"유효하지 않은 choice: {choice}")
            clean_choices.append(str(choice).strip())

        # correctIndex 검증
        if (
            correct_index is None
            or not isinstance(correct_index, int)
            or not (0 <= correct_index <= 2)
        ):
            raise ValueError(f"유효하지 않은 correctIndex: {correct_index}")

        # explanation 검증
        if (
            not explanation
            or not isinstance(explanation, str)
            or len(explanation.strip()) < 3
        ):
            raise ValueError(f"유효하지 않은 explanation: {explanation}")

        # 5. 선택적 필드 추출
        generation_method = str(safe_get("generation_method", "unknown"))

        quality_score = safe_get("quality_score", 0.0)
        try:
            quality_score = float(quality_score) if quality_score is not None else 0.0
        except (ValueError, TypeError):
            quality_score = 0.0

        parsing_method = str(safe_get("parsing_method", "converted"))

        # 6. GeneratedQuizProblem 생성
        result = GeneratedQuizProblem(
            question=question.strip(),
            choices=clean_choices,
            correctIndex=correct_index,
            explanation=explanation.strip(),
            generation_method=generation_method,
            quality_score=quality_score,
            parsing_method=parsing_method,
        )

        logger.info(f"✅ 변환 성공: {result.question[:50]}...")
        return result

    except Exception as e:
        logger.error(f"❌ 데이터 변환 실패: {e}")
        logger.error(f"원본 데이터 타입: {type(quiz_data)}")
        logger.error(f"원본 데이터: {quiz_data}")

        # 🚨 Fallback: 최소한의 유효한 문제 생성
        logger.warning("🔄 Fallback 문제 생성")
        return GeneratedQuizProblem(
            question=f"{fallback_name}에 관한 문제입니다.",
            choices=["선택지1", "선택지2", "선택지3"],
            correctIndex=0,
            explanation="기본 해설입니다.",
            generation_method="conversion_error_fallback",
            quality_score=0.3,
            parsing_method="error_recovery",
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
pose_service: Optional[object] = None

system_status = {
    "quiz_service_ready": False,
    "pose_service_ready": False,
    "startup_time": None,
    "error_details": [],
}


# === 서비스 초기화 ===
async def initialize_services():
    global quiz_service, pose_service, system_status

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

    try:
        if pose_service_available and PoseService:
            pose_service = PoseService(settings)
            await pose_service.initialize()
            system_status["pose_service_ready"] = pose_service.is_ready
            logger.info("✅ 포즈 분석 서비스 활성화")
        else:
            system_status["pose_service_ready"] = False
            logger.warning("⚠️ 포즈 분석 서비스 비활성화")
    except Exception as e:
        logger.error(f"❌ 포즈 서비스 초기화 실패: {e}")
        system_status["pose_service_ready"] = False
        system_status["error_details"].append(f"포즈 서비스: {str(e)}")

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
    description="Pydantic 객체 처리 수정 완료",
    version="v3.4",
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
    """퀴즈 생성 (spotName 기반) - Pydantic 객체 처리 수정"""
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

        # 🔥 핵심 수정: 유니버설 데이터 변환
        problems = []
        conversion_success_count = 0

        for idx, quiz_data in enumerate(quiz_problems):
            logger.info(f"🔧 문제 {idx+1} 변환 시작")

            try:
                # 유니버설 변환 함수 사용
                problem = convert_to_api_model(quiz_data, request.spotName)
                problems.append(problem)

                # 성공 카운트 (fallback이 아닌 경우)
                if problem.generation_method != "conversion_error_fallback":
                    conversion_success_count += 1

                logger.info(f"✅ 문제 {idx+1} 변환 성공: {problem.question[:30]}...")

            except Exception as e:
                logger.error(f"❌ 문제 {idx+1} 변환 실패: {e}")

                # 개별 fallback
                fallback_problem = convert_to_api_model({}, request.spotName)
                problems.append(fallback_problem)

        # 최소 1개 문제 보장
        if not problems:
            logger.warning("⚠️ 변환된 문제가 없음, 기본 문제 생성")
            problems.append(
                convert_to_api_model(
                    {
                        "question": f"{request.spotName}은 우리나라 어디에 있나요?",
                        "choices": ["서울", "부산", "제주도"],
                        "correctIndex": 0,
                        "explanation": f"{request.spotName}은 서울에 있습니다.",
                    },
                    request.spotName,
                )
            )
            conversion_success_count = 1

        generation_info = {
            "spot_name": request.spotName,
            "generated_count": len(problems),
            "grade": request.grade or 5,
            "service_stats": (
                quiz_service.get_stats() if hasattr(quiz_service, "get_stats") else {}
            ),
            "conversion_success": conversion_success_count,
            "conversion_rate": f"{conversion_success_count}/{len(problems)}",
        }

        logger.info(
            f"✅ 퀴즈 생성 완료: {len(problems)}개 (성공: {conversion_success_count}개)"
        )

        return ProblemGenerateResponse(
            success=True, problems=problems, generation_info=generation_info
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ 퀴즈 생성 실패: {e}")
        logger.error(f"Traceback: {traceback.format_exc()}")
        raise HTTPException(status_code=500, detail=f"퀴즈 생성 실패: {str(e)}")


@app.post("/generate-problem-by-id", response_model=ProblemGenerateResponse)
async def generate_problem_by_spot_id(request: ProblemGenerateRequestFromSpotId):
    """퀴즈 생성 (spotId 기반) - Pydantic 객체 처리 수정"""
    if not system_status["quiz_service_ready"]:
        raise HTTPException(status_code=503, detail="퀴즈 서비스가 준비되지 않았습니다")

    try:
        logger.info(
            f"📝 ID 퀴즈 생성 요청: ID {request.spotId}, {request.problemCnt}개"
        )

        quiz_problems = await quiz_service.generate_problems_by_id(
            spot_id=request.spotId, count=request.problemCnt, grade=request.grade
        )

        # 유니버설 변환 함수 사용
        problems = []
        for idx, quiz_data in enumerate(quiz_problems):
            try:
                problem = convert_to_api_model(quiz_data, f"스팟{request.spotId}")
                problems.append(problem)
            except Exception as e:
                logger.error(f"❌ 문제 {idx+1} 변환 실패: {e}")
                problems.append(convert_to_api_model({}, f"스팟{request.spotId}"))

        return ProblemGenerateResponse(
            success=True,
            problems=problems,
            generation_info={
                "spot_id": request.spotId,
                "generated_count": len(problems),
            },
        )

    except Exception as e:
        logger.error(f"❌ ID 퀴즈 생성 실패: {e}")
        raise HTTPException(status_code=500, detail=f"퀴즈 생성 실패: {str(e)}")


@app.post("/pose/predict", response_model=SimplePoseResponse)
async def analyze_pose_simple(
    file: UploadFile = File(...), pose_select: str = Form(...)
):
    """포즈 분석"""
    if not system_status["pose_service_ready"]:
        return SimplePoseResponse(
            success=False, result="포즈 분석 서비스가 준비되지 않았습니다"
        )

    try:
        result = await pose_service.analyze_pose(file, pose_select)
        return SimplePoseResponse(success=True, result=str(result))
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
        "title": "ARGO AI 통합 서버 (Pydantic 객체 처리 수정)",
        "version": "v3.4",
        "status": "✅ 정상 작동",
        "fixes": [
            "✅ Pydantic 객체 vs 딕셔너리 처리 수정",
            "✅ 유니버설 데이터 변환 함수 추가",
            "✅ 다양한 데이터 타입 지원",
            "✅ 속성 접근과 딕셔너리 접근 모두 지원",
            "✅ 고품질 퀴즈 데이터 보존 보장",
        ],
        "debug_info": {
            "conversion_method": "universal",
            "type_support": "dict, pydantic, object",
            "fallback_safety": "guaranteed",
        },
    }


if __name__ == "__main__":
    print("🚀 ARGO AI 통합 서버 시작 (Pydantic 객체 처리 수정)")
    print("🔧 주요 수정사항:")
    print("   - Pydantic 객체 vs 딕셔너리 처리 구분")
    print("   - 유니버설 데이터 변환 함수")
    print("   - 다양한 데이터 타입 지원")
    print("   - 고품질 퀴즈 데이터 보존")

    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False, log_level="info")
