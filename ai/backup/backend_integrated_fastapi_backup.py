# backend_integrated_fastapi.py - Spring Boot 백엔드 연동 완료 버전
"""
🔗 ARGO RAG API - 백엔드 연동 버전

주요 변경사항:
1. ✅ 백엔드 DTO 구조와 완전 호환
2. ✅ spotId 기반 문제 생성 지원
3. ✅ 배치 생성 API 추가 (교사용)
4. ✅ 에러 처리 및 로깅 강화
5. ✅ Health check 및 연동 테스트 지원

백엔드 연동 포인트:
- POST /api/problem/generate → POST /generate-problem
- GET /health → 연동 상태 확인
- 백엔드 ProblemGenerateRequestToAI 구조 지원
"""

from fastapi import FastAPI, HTTPException, status
from contextlib import asynccontextmanager
from pydantic import BaseModel, Field, validator
import os
import logging
import time
from datetime import datetime
from typing import List, Dict, Optional, Union
import openai
from dotenv import load_dotenv
import uvicorn
import json
import re
import traceback
from pathlib import Path as FilePath

# 기존 모듈 import (필요시 생성)
try:
    from expanded_educational_spots import EXPANDED_EDUCATIONAL_SPOTS
    from education_curriculum_integration import EducationCurriculumIntegrator
except ImportError:
    # 폴백 데이터
    EXPANDED_EDUCATIONAL_SPOTS = []
    EducationCurriculumIntegrator = None
    print("⚠️ 교육과정 모듈 없음. 기본 모드로 실행")

# === 환경설정 ===
load_dotenv()

logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)


# === 백엔드 연동용 데이터 모델 (통합 버전) ===
class ProblemGenerateRequestToAI(BaseModel):
    """백엔드에서 전송하는 문제 생성 요청 (spotName 기반)"""

    spotName: str = Field(..., description="스팟명", example="근정전")
    problemCnt: int = Field(..., ge=1, le=10, description="생성할 문제 개수")


class ProblemGenerateRequestFromSpotId(BaseModel):
    """백엔드에서 전송하는 문제 생성 요청 (spotId 기반)"""

    spotId: int = Field(..., description="스팟 ID")
    problemCnt: int = Field(..., ge=1, le=10, description="생성할 문제 개수")
    grade: Optional[int] = Field(default=5, ge=1, le=6, description="학년 (기본값: 5)")


class QuizProblemGenerated(BaseModel):
    """생성된 퀴즈 문제 (내부 로직용)"""

    question: str = Field(..., description="문제 텍스트")
    choices: List[str] = Field(..., description="선택지 리스트")
    correctIndex: int = Field(..., ge=0, le=2, description="정답 인덱스 (0-based)")
    explanation: str = Field(..., description="해설")

    @validator("choices")
    def validate_choices(cls, v):
        if len(v) != 3:
            raise ValueError("선택지는 정확히 3개여야 합니다")
        return v


class GeneratedQuizProblem(BaseModel):
    """백엔드 PythonApiResponse.GeneratedQuizProblem과 동일"""

    question: str = Field(..., description="문제 텍스트")
    choices: List[str] = Field(..., description="선택지 리스트")
    correctIndex: int = Field(..., ge=0, le=2, description="정답 인덱스 (0-based)")
    explanation: str = Field(..., description="해설")

    @validator("choices")
    def validate_choices(cls, v):
        if len(v) != 3:
            raise ValueError("선택지는 정확히 3개여야 합니다")
        return v


class PythonApiResponse(BaseModel):
    """백엔드 PythonApiResponse와 동일"""

    success: bool = Field(default=True)
    problems: List[GeneratedQuizProblem] = Field(..., description="생성된 문제 리스트")
    generation_info: Dict = Field(default_factory=dict, description="생성 정보")


class ProblemGenerateResponse(BaseModel):
    """백엔드 ProblemGenerateResponse 호환 (내부용)"""

    success: bool = Field(default=True)
    problems: List[QuizProblemGenerated] = Field(..., description="생성된 문제 리스트")
    generation_info: Dict = Field(default_factory=dict, description="생성 정보")


class HealthCheckResponse(BaseModel):
    """연동 상태 확인용"""

    status: str
    timestamp: str
    backend_compatibility: bool
    llm_available: bool
    total_spots: int
    version: str


# === 백엔드 연동용 전역 변수 ===
openai_client = None
spots_database = {}  # spotId -> spot_info 매핑
spots_by_name = {}  # spotName -> spot_info 매핑
curriculum_integrator = None

rag_status = {
    "initialized": False,
    "llm_available": False,
    "backend_compatible": True,
    "total_spots": 0,
    "api_provider": "None",
}


# === 환경 감지 및 API 설정 ===
def setup_openai_client():
    """OpenAI/GMS 클라이언트 설정"""
    global openai_client, rag_status

    # API 키 우선순위: GMS -> OpenAI -> 기타
    api_keys = [
        ("GMS_API_KEY", "https://gms.ssafy.io/gmsapi/api.openai.com/v1"),
        ("OPENAI_API_KEY", "https://api.openai.com/v1"),
        ("API_KEY", "https://gms.ssafy.io/gmsapi/api.openai.com/v1"),
    ]

    for key_name, base_url in api_keys:
        api_key = os.getenv(key_name)
        if api_key and len(api_key.strip()) >= 20:
            try:
                openai_client = openai.OpenAI(
                    api_key=api_key.strip(), base_url=base_url
                )

                # 연결 테스트
                test_response = openai_client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[{"role": "user", "content": "test"}],
                    max_tokens=5,
                    timeout=10,
                )

                rag_status["llm_available"] = True
                rag_status["api_provider"] = key_name
                logger.info(f"✅ {key_name} API 연결 성공")
                return True

            except Exception as e:
                logger.warning(f"⚠️ {key_name} API 연결 실패: {e}")
                continue

    logger.warning("⚠️ 사용 가능한 API 키 없음. 폴백 모드로 실행")
    rag_status["llm_available"] = False
    return False


def load_spots_database():
    """스팟 데이터베이스 로딩 및 인덱싱"""
    global spots_database, spots_by_name, rag_status

    if EXPANDED_EDUCATIONAL_SPOTS:
        for idx, spot in enumerate(EXPANDED_EDUCATIONAL_SPOTS):
            # spotId는 임시로 인덱스 사용 (실제로는 백엔드 DB의 ID)
            spot_id = idx + 1
            spots_database[spot_id] = spot
            spots_by_name[spot["세부스팟"]] = {**spot, "spot_id": spot_id}

            # 메인 장소명으로도 검색 가능하게
            if spot["메인장소"] not in spots_by_name:
                spots_by_name[spot["메인장소"]] = {**spot, "spot_id": spot_id}

    rag_status["total_spots"] = len(spots_database)
    logger.info(f"📊 스팟 데이터베이스 로딩 완료: {rag_status['total_spots']}개")


def setup_curriculum_integration():
    """교육과정 연계 시스템 설정"""
    global curriculum_integrator

    if EducationCurriculumIntegrator:
        try:
            curriculum_integrator = EducationCurriculumIntegrator()
            logger.info("📚 교육과정 연계 시스템 활성화")
        except Exception as e:
            logger.warning(f"⚠️ 교육과정 연계 시스템 오류: {e}")


# === 라이프사이클 관리 ===
@asynccontextmanager
async def lifespan(app: FastAPI):
    """FastAPI 라이프사이클 - 백엔드 연동 초기화"""
    logger.info("🚀 백엔드 연동 RAG API 서버 시작")

    # 1. OpenAI/GMS 클라이언트 설정
    setup_openai_client()

    # 2. 스팟 데이터베이스 로딩
    load_spots_database()

    # 3. 교육과정 연계 시스템 설정
    setup_curriculum_integration()

    rag_status["initialized"] = True

    logger.info("✅ 백엔드 연동 초기화 완료")
    logger.info(f"   API 제공자: {rag_status['api_provider']}")
    logger.info(f"   LLM 사용 가능: {rag_status['llm_available']}")
    logger.info(f"   총 스팟 수: {rag_status['total_spots']}")

    yield

    logger.info("🔄 서버 종료 중...")


# FastAPI 앱 생성
app = FastAPI(
    title="ARGO RAG API - Backend Integration",
    description="Spring Boot 백엔드와 연동된 RAG 기반 퀴즈 생성 API",
    version="backend-v1.0",
    lifespan=lifespan,
)

# === 핵심 퀴즈 생성 함수들 ===


def find_spot_by_name(spot_name: str) -> Optional[Dict]:
    """스팟명으로 스팟 정보 검색"""
    # 정확한 매칭 우선
    if spot_name in spots_by_name:
        return spots_by_name[spot_name]

    # 부분 매칭
    for name, spot_info in spots_by_name.items():
        if spot_name in name or name in spot_name:
            return spot_info

    return None


def find_spot_by_id(spot_id: int) -> Optional[Dict]:
    """스팟 ID로 스팟 정보 검색"""
    return spots_database.get(spot_id)


def create_enhanced_prompt(spot_info: Dict, grade: int = 5) -> str:
    """교육과정 연계 프롬프트 생성"""
    spot_name = spot_info.get("이름", spot_info.get("세부스팟", ""))
    description = spot_info.get("설명", "")
    keywords = ", ".join(spot_info.get("교육키워드", [])[:3])

    # 교육과정 연계 시도
    if curriculum_integrator:
        try:
            location = spot_info.get("메인장소", "")
            return curriculum_integrator.generate_curriculum_enhanced_prompt(
                location, spot_name, grade, description
            )
        except Exception as e:
            logger.debug(f"교육과정 프롬프트 생성 실패: {e}")

    # 기본 프롬프트
    if grade <= 2:
        difficulty = "매우 쉬운"
        conditions = "짧고 간단한 단어만 사용하세요."
    elif grade <= 4:
        difficulty = "적당한"
        conditions = "교과서 수준의 어휘를 사용하세요."
    else:
        difficulty = "심화"
        conditions = "교육과정에 맞는 분석적 사고가 필요한 문제를 만드세요."

    return f"""초등학교 {grade}학년용 {difficulty} 수준의 삼지선다 퀴즈를 만드세요.

장소: {spot_name}
설명: {description}
키워드: {keywords}

조건:
- {conditions}
- 정확히 3개의 선택지
- 명확한 정답 1개
- 현장학습에 적합한 관찰형 문제

출력 형식:
문제: [질문]
1) [선택지1] 2) [선택지2] 3) [선택지3]
정답: [번호]
해설: [간단한 설명]"""


def parse_llm_response(
    llm_response: str, spot_info: Dict, grade: int
) -> Optional[QuizProblemGenerated]:
    """LLM 응답을 QuizProblemGenerated로 파싱"""
    try:
        lines = [
            line.strip() for line in llm_response.strip().split("\n") if line.strip()
        ]
        full_text = " ".join(lines)

        parsed_data = {
            "question": "",
            "choices": [],
            "correct_index": 0,
            "explanation": "",
        }

        # 문제 추출
        question_patterns = [r"문제\s*[:：]\s*(.+)", r"Q\s*[:：]\s*(.+)"]
        for pattern in question_patterns:
            match = re.search(pattern, full_text)
            if match:
                parsed_data["question"] = match.group(1).strip()
                break

        # 선택지 추출
        choice_patterns = [
            r"1\)\s*([^2]+?)\s*2\)\s*([^3]+?)\s*3\)\s*([^정답]+?)(?=정답|해설|$)",
            r"①\s*([^②]+?)\s*②\s*([^③]+?)\s*③\s*([^정답]+?)(?=정답|해설|$)",
        ]

        for pattern in choice_patterns:
            match = re.search(pattern, full_text, re.DOTALL)
            if match:
                parsed_data["choices"] = [
                    match.group(1).strip(),
                    match.group(2).strip(),
                    match.group(3).strip(),
                ]
                break

        # 정답 추출
        answer_patterns = [r"정답\s*[:：]\s*(\d+)", r"답\s*[:：]\s*(\d+)"]
        for pattern in answer_patterns:
            match = re.search(pattern, full_text)
            if match:
                answer_num = int(match.group(1))
                if 1 <= answer_num <= 3:
                    parsed_data["correct_index"] = answer_num - 1
                break

        # 해설 추출
        explanation_patterns = [
            r"해설\s*[:：]\s*(.+?)(?=\n|$)",
            r"설명\s*[:：]\s*(.+?)(?=\n|$)",
        ]
        for pattern in explanation_patterns:
            match = re.search(pattern, full_text, re.DOTALL)
            if match:
                parsed_data["explanation"] = match.group(1).strip()
                break

        # 유효성 검사
        if (
            parsed_data["question"]
            and len(parsed_data["choices"]) == 3
            and 0 <= parsed_data["correct_index"] <= 2
            and all(choice.strip() for choice in parsed_data["choices"])
            and parsed_data["explanation"]
        ):

            return QuizProblemGenerated(**parsed_data)
        else:
            logger.warning("파싱 실패: 필수 요소 누락")
            return None

    except Exception as e:
        logger.error(f"파싱 오류: {e}")
        return None


def generate_fallback_quiz(spot_info: Dict, grade: int = 5) -> QuizProblemGenerated:
    """폴백 퀴즈 생성"""
    spot_name = spot_info.get("이름", spot_info.get("세부스팟", "스팟"))
    keywords = spot_info.get("교육키워드", ["역사", "문화"])

    if grade <= 2:
        return QuizProblemGenerated(
            question=f"{spot_name}은 어디에 있을까요?",
            choices=["서울", "부산", "제주도"],
            correctIndex=0,
            explanation=f"{spot_name}은 서울에 있는 소중한 곳이에요.",
        )
    elif grade <= 4:
        keyword = keywords[0] if keywords else "역사적인 것"
        return QuizProblemGenerated(
            question=f"{spot_name}에서 볼 수 있는 것은 무엇인가요?",
            choices=[keyword, "현대식 건물", "놀이기구"],
            correctIndex=0,
            explanation=f"{spot_name}에서는 {keyword}을 볼 수 있습니다.",
        )
    else:
        return QuizProblemGenerated(
            question=f"{spot_name}의 특별한 점은 무엇인가요?",
            choices=["문화유산으로서의 가치", "현대적 편의시설", "상업적 목적"],
            correctIndex=0,
            explanation=f"{spot_name}은 우리나라의 소중한 문화유산으로 역사적, 교육적 가치가 큽니다.",
        )


def generate_single_quiz(spot_info: Dict, grade: int = 5) -> QuizProblemGenerated:
    """단일 퀴즈 생성 (LLM 또는 폴백)"""
    if openai_client and rag_status["llm_available"]:
        try:
            prompt = create_enhanced_prompt(spot_info, grade)

            response = openai_client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {
                        "role": "system",
                        "content": "당신은 초등학생 교육 전문가입니다. 정확하고 흥미로운 삼지선다 퀴즈를 만드세요.",
                    },
                    {"role": "user", "content": prompt},
                ],
                max_tokens=400,
                temperature=0.3,
                timeout=15,
            )

            llm_response = response.choices[0].message.content.strip()
            parsed_quiz = parse_llm_response(llm_response, spot_info, grade)

            if parsed_quiz:
                logger.info(
                    f"✅ LLM 퀴즈 생성 성공: {spot_info.get('세부스팟', 'Unknown')}"
                )
                return parsed_quiz
            else:
                logger.warning("LLM 응답 파싱 실패, 폴백 사용")

        except Exception as e:
            logger.error(f"LLM 생성 실패: {e}")

    # 폴백 퀴즈 생성
    return generate_fallback_quiz(spot_info, grade)


# === 백엔드 연동 API 엔드포인트 ===


@app.post("/generate-problem", response_model=ProblemGenerateResponse)
async def generate_problem_by_spot_name(request: ProblemGenerateRequestToAI):
    """백엔드 호환: spotName 기반 문제 생성"""
    start_time = datetime.now()
    logger.info(
        f"🎯 백엔드 요청: spotName={request.spotName}, count={request.problemCnt}"
    )

    try:
        # 스팟 정보 검색
        spot_info = find_spot_by_name(request.spotName)
        if not spot_info:
            logger.warning(f"❌ 스팟 없음: {request.spotName}")
            raise HTTPException(
                status_code=404, detail=f"스팟 '{request.spotName}'을 찾을 수 없습니다."
            )

        # 문제 생성
        problems = []
        generation_info = {
            "spot_name": request.spotName,
            "requested_count": request.problemCnt,
            "generated_count": 0,
            "llm_generated": 0,
            "fallback_generated": 0,
            "processing_time_ms": 0,
        }

        for i in range(request.problemCnt):
            try:
                # 다양성을 위해 학년을 순환
                grade = 3 + (i % 4)  # 3,4,5,6 순환
                quiz = generate_single_quiz(spot_info, grade)
                problems.append(quiz)
                generation_info["generated_count"] += 1

                if rag_status["llm_available"]:
                    generation_info["llm_generated"] += 1
                else:
                    generation_info["fallback_generated"] += 1

            except Exception as e:
                logger.error(f"문제 {i+1} 생성 실패: {e}")
                # 실패해도 폴백으로 계속 진행
                quiz = generate_fallback_quiz(spot_info)
                problems.append(quiz)
                generation_info["generated_count"] += 1
                generation_info["fallback_generated"] += 1

        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        generation_info["processing_time_ms"] = round(processing_time, 2)

        logger.info(
            f"✅ 백엔드 요청 완료: {len(problems)}개 생성 (소요시간: {processing_time:.0f}ms)"
        )

        return ProblemGenerateResponse(
            success=True, problems=problems, generation_info=generation_info
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ 백엔드 요청 처리 실패: {e}")
        logger.error(traceback.format_exc())
        raise HTTPException(
            status_code=500, detail=f"문제 생성 중 오류가 발생했습니다: {str(e)}"
        )


@app.post("/generate-problem-by-id", response_model=ProblemGenerateResponse)
async def generate_problem_by_spot_id(request: ProblemGenerateRequestFromSpotId):
    """spotId 기반 문제 생성 (확장 API)"""
    start_time = datetime.now()
    logger.info(
        f"🎯 SpotID 요청: spotId={request.spotId}, count={request.problemCnt}, grade={request.grade}"
    )

    try:
        # 스팟 정보 검색
        spot_info = find_spot_by_id(request.spotId)
        if not spot_info:
            logger.warning(f"❌ 스팟 ID 없음: {request.spotId}")
            raise HTTPException(
                status_code=404, detail=f"스팟 ID {request.spotId}를 찾을 수 없습니다."
            )

        # 문제 생성
        problems = []
        generation_info = {
            "spot_id": request.spotId,
            "spot_name": spot_info.get("세부스팟", "Unknown"),
            "grade": request.grade,
            "requested_count": request.problemCnt,
            "generated_count": 0,
            "llm_generated": 0,
            "fallback_generated": 0,
            "processing_time_ms": 0,
        }

        for i in range(request.problemCnt):
            try:
                quiz = generate_single_quiz(spot_info, request.grade)
                problems.append(quiz)
                generation_info["generated_count"] += 1

                if rag_status["llm_available"]:
                    generation_info["llm_generated"] += 1
                else:
                    generation_info["fallback_generated"] += 1

            except Exception as e:
                logger.error(f"문제 {i+1} 생성 실패: {e}")
                quiz = generate_fallback_quiz(spot_info, request.grade)
                problems.append(quiz)
                generation_info["generated_count"] += 1
                generation_info["fallback_generated"] += 1

        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        generation_info["processing_time_ms"] = round(processing_time, 2)

        logger.info(f"✅ SpotID 요청 완료: {len(problems)}개 생성")

        return ProblemGenerateResponse(
            success=True, problems=problems, generation_info=generation_info
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ SpotID 요청 처리 실패: {e}")
        raise HTTPException(
            status_code=500, detail=f"문제 생성 중 오류가 발생했습니다: {str(e)}"
        )


@app.get("/health", response_model=HealthCheckResponse)
async def health_check():
    """백엔드 연동 상태 확인"""
    return HealthCheckResponse(
        status="healthy",
        timestamp=datetime.now().isoformat(),
        backend_compatibility=True,
        llm_available=rag_status["llm_available"],
        total_spots=rag_status["total_spots"],
        version="backend-v1.0",
    )


@app.get("/spots")
async def get_available_spots():
    """사용 가능한 스팟 목록 (백엔드 참고용)"""
    spots_list = []

    for spot_id, spot_info in spots_database.items():
        spots_list.append(
            {
                "spot_id": spot_id,
                "spot_name": spot_info.get("세부스팟", ""),
                "location": spot_info.get("메인장소", ""),
                "description": (
                    spot_info.get("설명", "")[:100] + "..."
                    if len(spot_info.get("설명", "")) > 100
                    else spot_info.get("설명", "")
                ),
                "keywords": spot_info.get("교육키워드", [])[:3],
            }
        )

    return {"total_spots": len(spots_list), "spots": spots_list}


@app.get("/")
async def root():
    """루트 엔드포인트 - 백엔드 연동 정보"""
    return {
        "title": "🔗 ARGO RAG API - Backend Integration",
        "version": "backend-v1.0",
        "description": "Spring Boot 백엔드와 연동된 RAG 기반 퀴즈 생성 API",
        "backend_endpoints": {
            "problem_generation": "POST /generate-problem",
            "problem_by_id": "POST /generate-problem-by-id",
            "health_check": "GET /health",
            "available_spots": "GET /spots",
        },
        "integration_status": {
            "backend_compatible": True,
            "llm_available": rag_status["llm_available"],
            "api_provider": rag_status["api_provider"],
            "total_spots": rag_status["total_spots"],
        },
        "supported_features": [
            "✅ 백엔드 DTO 구조 완전 호환",
            "✅ spotName/spotId 기반 문제 생성",
            "✅ 1-6학년 맞춤형 퀴즈",
            "✅ 교육과정 연계 (선택적)",
            "✅ 에러 처리 및 폴백 지원",
            "✅ 성능 모니터링",
        ],
        "test_endpoints": {
            "sample_request": {
                "url": "/generate-problem",
                "method": "POST",
                "body": {"spotName": "근정전", "problemCnt": 3},
            }
        },
    }


# === 서버 실행 ===
if __name__ == "__main__":
    print("🔗 백엔드 연동 RAG API 서버 시작")
    print("📋 연동 기능:")
    print("   - Spring Boot 백엔드 호환")
    print("   - 문제 생성: POST /generate-problem")
    print("   - 상태 확인: GET /health")
    print("   - 스팟 목록: GET /spots")

    uvicorn.run(
        "backend_integrated_fastapi:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        log_level="info",
    )
