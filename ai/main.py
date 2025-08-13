# main.py - ARGO AI 통합 서버 (퀴즈 생성 + 포즈 분석)

import sys
import os
import traceback
import logging
from enum import Enum
from datetime import datetime
from typing import List, Dict, Optional, Any, Union
from fastapi import FastAPI, HTTPException, UploadFile, File, Form, Request, Depends
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from starlette.datastructures import FormData
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
spots_available = False

try:
    from services.quiz_service import QuizService

    quiz_service_available = True
    logger.info("✅ services.quiz_service 로드 성공")
except ImportError as e:
    logger.warning(f"⚠️ 퀴즈 서비스 import 실패: {e}")
    QuizService = None

try:
    from data.expanded_educational_spots import EXPANDED_EDUCATIONAL_SPOTS

    spots_available = True
    logger.info(f"✅ 스팟 데이터 로드 성공: {len(EXPANDED_EDUCATIONAL_SPOTS)}개")
except ImportError as e:
    logger.warning(f"⚠️ 스팟 데이터 import 실패: {e}")
    EXPANDED_EDUCATIONAL_SPOTS = []
    spots_available = False


# === 설정 클래스 ===
class Settings:
    """애플리케이션 설정"""

    OPENAI_API_KEY: str = os.getenv("OPENAI_API_KEY", "")
    GMS_API_KEY: str = os.getenv("GMS_API_KEY", "")
    IMAGE_RESIZE_MAX: int = 1024
    MAX_QUIZ_COUNT: int = 10


settings = Settings()


# === Request/Response 모델 ===
class ProblemGenerateRequest(BaseModel):
    spotName: str = Field(..., description="스팟명")
    grade: int = Field(..., ge=1, le=6, description="학년")
    problemCnt: int = Field(..., ge=1, le=10, description="문제 개수")


class QuizProblemSimple(BaseModel):
    question: str = Field(..., description="문제")
    choice1: str = Field(..., description="선택지 1")
    choice2: str = Field(..., description="선택지 2")
    choice3: str = Field(..., description="선택지 3")
    answer: int = Field(..., ge=1, le=3, description="정답")
    explanation: str = Field(..., description="해설")
    grade: int = Field(..., ge=1, le=6, description="학년")
    spotName: str = Field(..., description="스팟명")


class ProblemGenerateResponse(BaseModel):
    problems: List[QuizProblemSimple] = Field(..., description="생성된 문제 리스트")


class SimplePoseResponse(BaseModel):
    success: bool = Field(..., description="성공 여부")
    result: str = Field(..., description="결과 메시지")


# === 시스템 상태 ===
system_status = {
    "quiz_service_ready": False,
    "pose_service_ready": False,
    "startup_time": None,
    "error_details": [],
}

# === 서비스 초기화 ===
quiz_service: Optional[QuizService] = None


async def initialize_services():
    """서비스 초기화"""
    global quiz_service

    logger.info("🔄 서비스 초기화 시작")

    # 퀴즈 서비스 초기화
    if quiz_service_available and QuizService:
        try:
            quiz_service = QuizService(settings)
            await quiz_service.initialize()
            system_status["quiz_service_ready"] = True
            logger.info("✅ 퀴즈 서비스 초기화 완료")
        except Exception as e:
            logger.error(f"❌ 퀴즈 서비스 초기화 실패: {e}")
            system_status["error_details"].append(f"퀴즈 서비스: {str(e)}")

    # 포즈 서비스는 직접 AI 모듈 사용
    system_status["pose_service_ready"] = True
    logger.info("✅ 포즈 분석: AI 모듈 직접 사용")

    ready_services = sum(
        [system_status["quiz_service_ready"], system_status["pose_service_ready"]]
    )
    logger.info(f"✅ 서비스 초기화 완료: {ready_services}/2")


# === FastAPI 앱 생성 ===
@asynccontextmanager
async def lifespan(app: FastAPI):
    system_status["startup_time"] = datetime.now().isoformat()
    await initialize_services()
    yield
    logger.info("🔄 서버 종료")


app = FastAPI(
    title="ARGO AI 통합 서버",
    description="퀴즈 생성 + 포즈 분석 완전 통합",
    version="v4.0",
    lifespan=lifespan,
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# === 에러 핸들러 ===
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


# === 헬퍼 함수들 ===
def convert_to_quiz_problem_simple(
    quiz_data: Union[Dict, Any], grade: int, fallback_name: str
) -> QuizProblemSimple:
    """퀴즈 데이터를 QuizProblemSimple로 변환"""

    if hasattr(quiz_data, "choices"):
        # Pydantic 객체인 경우
        choices = quiz_data.choices
        return QuizProblemSimple(
            question=quiz_data.question,
            choice1=choices[0] if len(choices) > 0 else "선택지 1",
            choice2=choices[1] if len(choices) > 1 else "선택지 2",
            choice3=choices[2] if len(choices) > 2 else "선택지 3",
            answer=quiz_data.correctIndex + 1,  # 0-based -> 1-based
            explanation=quiz_data.explanation,
            grade=grade,
            spotName=fallback_name,
        )
    elif isinstance(quiz_data, dict):
        # 딕셔너리인 경우
        choices = quiz_data.get("choices", ["선택지 1", "선택지 2", "선택지 3"])
        return QuizProblemSimple(
            question=quiz_data.get("question", f"{fallback_name}에 대한 문제입니다."),
            choice1=choices[0] if len(choices) > 0 else "선택지 1",
            choice2=choices[1] if len(choices) > 1 else "선택지 2",
            choice3=choices[2] if len(choices) > 2 else "선택지 3",
            answer=quiz_data.get("correctIndex", 0) + 1,
            explanation=quiz_data.get(
                "explanation", f"{fallback_name}에 대한 설명입니다."
            ),
            grade=grade,
            spotName=fallback_name,
        )
    else:
        # 빈 데이터인 경우 기본값
        return QuizProblemSimple(
            question=f"{fallback_name}은 우리나라 어디에 있나요?",
            choice1="서울",
            choice2="부산",
            choice3="제주도",
            answer=1,
            explanation=f"{fallback_name}은 서울에 있습니다.",
            grade=grade,
            spotName=fallback_name,
        )


# === 퀴즈 생성 엔드포인트 ===
@app.post("/generate-problem", response_model=ProblemGenerateResponse)
async def generate_problem(request: ProblemGenerateRequest):
    """퀴즈 문제 생성"""

    logger.info(
        f"📝 퀴즈 생성 요청: {request.spotName}, {request.grade}학년, {request.problemCnt}개"
    )

    try:
        generated_problems = []

        if quiz_service and system_status["quiz_service_ready"]:
            # 퀴즈 서비스 사용
            try:
                problems = await quiz_service.generate_problems_by_name(
                    spot_name=request.spotName,
                    count=request.problemCnt,
                    grade=request.grade,
                )

                for problem in problems:
                    quiz_simple = convert_to_quiz_problem_simple(
                        problem, request.grade, request.spotName
                    )
                    generated_problems.append(quiz_simple)

                logger.info(f"✅ 퀴즈 서비스로 {len(generated_problems)}개 생성 완료")

            except Exception as e:
                logger.error(f"❌ 퀴즈 서비스 실패: {e}")
                raise HTTPException(status_code=500, detail=f"퀴즈 생성 실패: {str(e)}")
        else:
            # Fallback: 기본 문제 생성
            logger.warning("⚠️ 퀴즈 서비스 비활성화 - Fallback 사용")

            for i in range(request.problemCnt):
                fallback_quiz = QuizProblemSimple(
                    question=f"{request.spotName}에 관한 문제 {i+1}번입니다.",
                    choice1="정답",
                    choice2="오답 1",
                    choice3="오답 2",
                    answer=1,
                    explanation=f"{request.spotName}에 대한 설명입니다.",
                    grade=request.grade,
                    spotName=request.spotName,
                )
                generated_problems.append(fallback_quiz)

            logger.info(f"🔄 Fallback으로 {len(generated_problems)}개 생성 완료")

        return ProblemGenerateResponse(problems=generated_problems)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ 전체 퀴즈 생성 실패: {e}")
        logger.error(f"Traceback: {traceback.format_exc()}")
        raise HTTPException(status_code=500, detail=f"퀴즈 생성 중 오류 발생: {str(e)}")


# === 🔥 포즈 분석 엔드포인트 (Stream consumed 에러 완전 해결) ===


@app.post("/pose/full")
async def pose_predict_full(file: UploadFile = File(...), pose_select: str = Form(...)):
    """🎯 포즈 분석 - 정상 파라미터 방식으로 복원"""

    logger.info(f"🎯 /pose/full 요청 받음")
    logger.info(f"📍 file: {file.filename if file else 'None'}")
    logger.info(f"📍 pose_select: {pose_select}")

    try:
        if not file or not file.filename:
            return JSONResponse(
                status_code=400,
                content={"success": False, "result": "파일이 필요합니다"},
            )

        # 파일 내용 읽기
        file_content = await file.read()
        logger.info(f"📍 파일 크기: {len(file_content)} bytes")

        # 🔥 실제 AI_Analyze.py 연결
        try:
            import numpy as np
            import cv2
            from service.AI_ObjectDetector import AI_ObjectDetector
            from service.AI_Analyze import AI_Analyze

            # 이미지 디코딩
            np_arr = np.frombuffer(file_content, np.uint8)
            image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

            if image is None:
                return JSONResponse(
                    status_code=400,
                    content={"success": False, "result": "이미지 디코딩 실패"},
                )

            logger.info(f"📷 이미지 크기: {image.shape}")

            # AI 모델 로드 및 추론
            logger.info("🤖 AI 모델 로드 중...")
            ai_model = AI_ObjectDetector("model/yolov8m.pt")

            logger.info("🔍 포즈 감지 중...")
            processed_image, results, poses_info = ai_model.Load_image(image)

            if results is None:
                logger.error("❌ AI 추론 실패")
                return JSONResponse(
                    status_code=500,
                    content={"success": False, "result": "AI 추론 실패"},
                )

            if not poses_info:
                logger.warning("⚠️ 사람 감지 실패")
                return JSONResponse(
                    status_code=404,
                    content={
                        "success": False,
                        "result": "사람 감지 실패 / 포즈 탐색 실패",
                    },
                )

            logger.info(f"✅ 포즈 감지 성공: {len(poses_info)}명")

            # AI_Analyze로 포즈 분석
            logger.info(f"🎯 포즈 분석 시작: {pose_select}")
            processor = AI_Analyze(processed_image, results, poses_info)
            ai_result = processor.print_keypoints(pose_select=pose_select)

            logger.info(f"📊 AI 분석 결과: {ai_result}")

            # 🔥 AI 결과를 JSON 형태로 변환
            if isinstance(ai_result, str):
                # 문자열 응답인 경우 JSON 객체로 래핑
                if "성공" in ai_result or "올바른" in ai_result:
                    return {"success": True, "result": ai_result}
                else:
                    return {"success": False, "result": ai_result}
            elif isinstance(ai_result, dict):
                # 이미 딕셔너리인 경우 그대로 반환
                return ai_result
            else:
                # 기타 타입인 경우 문자열로 변환
                return {"success": True, "result": str(ai_result)}

        except ImportError as e:
            logger.error(f"❌ AI 모듈 import 실패: {e}")
            return JSONResponse(
                status_code=503,
                content={
                    "success": False,
                    "result": f"AI 분석 모듈을 사용할 수 없습니다: {str(e)}",
                },
            )

        except Exception as e:
            logger.error(f"❌ AI 분석 실패: {e}")
            logger.error(f"Traceback: {traceback.format_exc()}")
            return JSONResponse(
                status_code=500,
                content={"success": False, "result": f"포즈 분석 실패: {str(e)}"},
            )

    except Exception as e:
        logger.error(f"❌ 포즈 분석 실패: {e}")
        return JSONResponse(
            status_code=500,
            content={"success": False, "result": f"분석 실패: {str(e)}"},
        )


@app.post("/pose/debug")
async def pose_debug_fixed(request: Request):
    """🔍 디버깅 - Stream consumed 에러 완전 해결"""

    logger.info("🔍 /pose/debug 요청 받음 (수정된 버전)")

    try:
        # 헤더 정보 수집
        headers_info = dict(request.headers)
        content_type = request.headers.get("content-type", "")
        content_length = request.headers.get("content-length", "0")

        logger.info(f"📍 Content-Type: {content_type}")
        logger.info(f"📍 Content-Length: {content_length}")

        debug_result = {
            "success": True,
            "method": "header_only_analysis",
            "headers": headers_info,
            "content_type": content_type,
            "content_length": content_length,
            "is_multipart": "multipart/form-data" in content_type.lower(),
            "timestamp": datetime.now().isoformat(),
        }

        # 🔥 Stream을 건드리지 않고 헤더만으로 분석
        if "multipart/form-data" in content_type.lower():
            boundary = None
            if "boundary=" in content_type:
                boundary = content_type.split("boundary=")[1].split(";")[0]

            debug_result.update(
                {
                    "detected_boundary": boundary,
                    "analysis": "멀티파트 요청 감지됨 - 정상적인 파일 업로드 형식",
                }
            )
        else:
            debug_result.update(
                {"analysis": "멀티파트가 아닌 요청 - Content-Type 확인 필요"}
            )

        logger.info(f"📋 디버그 결과: {debug_result}")

        return debug_result

    except Exception as e:
        logger.error(f"❌ 디버깅 실패: {e}")
        return {
            "success": False,
            "error": str(e),
            "timestamp": datetime.now().isoformat(),
        }


@app.post("/pose/test")
async def pose_test_simple():
    """📡 간단한 연결 테스트"""
    return {
        "status": "연결 성공",
        "service": "ARGO AI Pose Analysis",
        "timestamp": datetime.now().isoformat(),
        "ai_modules_available": "object_detect.src" in sys.modules,
    }


@app.get("/pose/test")
async def pose_test_get():
    """📡 GET 방식 연결 테스트"""
    return {
        "status": "GET 연결 성공",
        "service": "ARGO AI Pose Analysis",
        "timestamp": datetime.now().isoformat(),
    }


# === 헬스체크 엔드포인트 ===
@app.get("/health")
async def health_check():
    """헬스체크"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "services": system_status,
        "version": "v4.0",
    }


# === 시스템 정보 엔드포인트 ===
@app.get("/")
async def root():
    """루트 엔드포인트"""
    return {
        "service": "ARGO AI 통합 서버",
        "version": "v4.0",
        "status": system_status,
        "endpoints": {
            "quiz_generation": "/generate-problem",
            "pose_analysis": "/pose/full",
            "pose_test": "/pose/test",
            "health": "/health",
        },
    }


if __name__ == "__main__":
    print("🚀 ARGO AI 통합 서버 시작")
    print("📡 엔드포인트:")
    print("   - /generate-problem : 퀴즈 생성")
    print("   - /pose/full : 포즈 분석")
    print("   - /health : 헬스체크")

    # 🔥 uvicorn 설정 변경 - 멀티파트 처리 강화
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        loop="asyncio",  # 명시적 이벤트 루프
        http="httptools",  # HTTP 파서 명시
        limit_max_requests=1000,
        timeout_keep_alive=5,
    )
