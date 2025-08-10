# backend_integrated_fastapi.py
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
import logging
import time
from datetime import datetime
from typing import List, Dict, Optional, Union, List
import openai
from dotenv import load_dotenv
import uvicorn
import json
import re
import random
import traceback
from pathlib import Path as FilePath


import sys
import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
OBJECT_DETECT_SRC_PATH = os.path.join(BASE_DIR, "object_detect", "src")
sys.path.insert(0, OBJECT_DETECT_SRC_PATH)

# 포즈 인식 라우터 임포트
from controller.predict import router as pose_router


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


app.include_router(predict_module.router, prefix="/pose", tags=["Pose Detection"])


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


def create_enhanced_prompt(spot_info: Dict, grade: int, quiz_number: int = 1) -> str:
    """품질 개선된 교육과정 연계 프롬프트 생성"""
    location = spot_info.get("메인장소", "")
    spot_name = spot_info.get("세부스팟", "")
    description = spot_info.get("설명", "")
    keywords = spot_info.get("교육키워드", [])

    # 학년별 어휘 수준 조정
    if grade <= 2:
        vocab_level = "1-2학년이 이해할 수 있는 쉬운 단어"
        difficulty_desc = "매우 간단하고 직관적인"
        question_types = ["이름 맞히기", "위치 찾기", "색깔이나 모양"]
    elif grade <= 4:
        vocab_level = "3-4학년 교과서 수준의 어휘"
        difficulty_desc = "관찰과 경험에 기반한"
        question_types = ["특징 파악", "용도나 역할", "역사적 의미"]
    else:
        vocab_level = "5-6학년이 이해할 수 있는 학술적 어휘"
        difficulty_desc = "분석적 사고가 필요한"
        question_types = ["역사적 배경", "문화적 의미", "교육적 가치"]

    # 다양성을 위한 관점 선택
    perspectives = [
        "건축적 특징과 구조",
        "역사적 배경과 의미",
        "문화적 가치와 중요성",
        "현장에서 관찰할 수 있는 요소",
        "교육적 의미와 교훈",
    ]

    selected_perspective = perspectives[(quiz_number - 1) % len(perspectives)]

    # 키워드 기반 힌트 생성
    keyword_hints = ""
    if keywords:
        main_keywords = keywords[:3]
        keyword_hints = f"핵심 개념: {', '.join(main_keywords)}"

    prompt = f"""당신은 초등학교 현장학습 전문 교육자입니다. {grade}학년 학생들이 현장에서 직접 관찰하며 학습할 수 있는 {difficulty_desc} 삼지선다 퀴즈를 만들어주세요.

📍 장소 정보:
- 위치: {location}
- 세부 장소: {spot_name}  
- 설명: {description}
- {keyword_hints}

🎯 이번 문제 관점: {selected_perspective}

📝 출제 조건:
1. {vocab_level}로만 구성
2. 현장에서 실제로 볼 수 있거나 체험할 수 있는 내용
3. 정답은 명확하고 의심의 여지가 없어야 함
4. 오답 선택지는 그럴듯하지만 틀린 내용
5. 해설은 왜 정답인지 교육적으로 설명

❌ 피해야 할 표현:
- "~를 본다", "~를 볼 수 있다" (추상적)
- 어색한 조사 사용 ("조선시대을" 등)
- 너무 뻔한 오답 ("놀이기구", "현대식 건물" 등)

✅ 권장 표현:
- "~의 특징은", "~에서 중요한 것은", "~의 역할은"
- 구체적이고 관찰 가능한 요소들
- 교육적 가치가 있는 선택지들

📋 반드시 아래 형식으로만 답변:

문제: [구체적이고 자연스러운 한국어 질문]
1) [정답 - 관찰 가능하고 정확한 내용]
2) [오답1 - 그럴듯하지만 틀린 내용] 
3) [오답2 - 그럴듯하지만 틀린 내용]
정답: [1, 2, 3 중 번호]
해설: [정답인 이유를 {grade}학년이 이해할 수 있게 명확하게 설명. 교육적 의미 포함]

지금 {quiz_number}번째 문제를 만들고 있으니, 이전과는 다른 관점에서 출제해주세요."""

    return prompt


def parse_llm_response(
    llm_response: str, spot_info: Dict, grade: int
) -> Optional[Dict]:
    """개선된 LLM 응답 파싱 (품질 검증 강화)"""
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

        # 1. 문제 추출 (다양한 패턴 지원)
        question_patterns = [
            r"문제\s*[:：]\s*(.+?)(?=1\)|①|답|정답|해설)",
            r"Q\s*[:：]\s*(.+?)(?=1\)|①|답|정답|해설)",
            r"질문\s*[:：]\s*(.+?)(?=1\)|①|답|정답|해설)",
        ]

        for pattern in question_patterns:
            match = re.search(pattern, full_text, re.DOTALL)
            if match:
                question = match.group(1).strip()
                # 불필요한 부분 제거
                question = re.sub(r"\s+", " ", question)  # 연속 공백 제거
                if (
                    question.endswith("?")
                    or question.endswith("까요")
                    or question.endswith("니까")
                ):
                    parsed_data["question"] = question
                    break

        # 2. 선택지 추출 (강화된 패턴)
        choice_patterns = [
            r"1\)\s*([^2]+?)\s*2\)\s*([^3]+?)\s*3\)\s*([^정답답해설]+?)(?=정답|답|해설|$)",
            r"①\s*([^②]+?)\s*②\s*([^③]+?)\s*③\s*([^정답답해설]+?)(?=정답|답|해설|$)",
            r"1\.\s*([^2]+?)\s*2\.\s*([^3]+?)\s*3\.\s*([^정답답해설]+?)(?=정답|답|해설|$)",
        ]

        for pattern in choice_patterns:
            match = re.search(pattern, full_text, re.DOTALL)
            if match:
                choices = [
                    match.group(1).strip(),
                    match.group(2).strip(),
                    match.group(3).strip(),
                ]
                # 선택지 정리
                cleaned_choices = []
                for choice in choices:
                    choice = re.sub(r"\s+", " ", choice).strip()
                    # 숫자나 기호로 시작하는 부분 제거
                    choice = re.sub(r"^[\d\)①②③\.]+\s*", "", choice)
                    if choice:
                        cleaned_choices.append(choice)

                if len(cleaned_choices) == 3:
                    parsed_data["choices"] = cleaned_choices
                    break

        # 3. 정답 추출 (다양한 형식 지원)
        answer_patterns = [
            r"정답\s*[:：]\s*(\d+)",
            r"답\s*[:：]\s*(\d+)",
            r"정답은?\s*(\d+)",
            r"Answer\s*[:：]\s*(\d+)",
        ]

        for pattern in answer_patterns:
            match = re.search(pattern, full_text)
            if match:
                answer_num = int(match.group(1))
                if 1 <= answer_num <= 3:
                    parsed_data["correct_index"] = answer_num - 1
                    break

        # 4. 해설 추출
        explanation_patterns = [
            r"해설\s*[:：]\s*(.+?)(?=\n\n|\n[A-Z]|\n\d+\.|$)",
            r"설명\s*[:：]\s*(.+?)(?=\n\n|\n[A-Z]|\n\d+\.|$)",
            r"풀이\s*[:：]\s*(.+?)(?=\n\n|\n[A-Z]|\n\d+\.|$)",
        ]

        for pattern in explanation_patterns:
            match = re.search(pattern, full_text, re.DOTALL)
            if match:
                explanation = match.group(1).strip()
                explanation = re.sub(r"\s+", " ", explanation)
                parsed_data["explanation"] = explanation
                break

        # 5. 품질 검증
        if not validate_quiz_quality(parsed_data, spot_info, grade):
            return None

        return parsed_data

    except Exception as e:
        logger.error(f"파싱 오류: {e}")
        return None


def validate_quiz_quality(quiz_data: Dict, spot_info: Dict, grade: int) -> bool:
    """생성된 퀴즈 품질 검증"""

    # 1. 기본 구조 검증
    if not quiz_data.get("question") or len(quiz_data.get("question", "")) < 10:
        return False

    if len(quiz_data.get("choices", [])) != 3:
        return False

    if not (0 <= quiz_data.get("correct_index", -1) <= 2):
        return False

    if not quiz_data.get("explanation") or len(quiz_data.get("explanation", "")) < 15:
        return False

    # 2. 내용 품질 검증
    question = quiz_data["question"]
    choices = quiz_data["choices"]
    explanation = quiz_data["explanation"]

    # 문법 오류 체크 (간단한 패턴)
    grammar_issues = [
        r"\w+을\s+볼\s+수\s+있습니다",  # "조선시대을 볼 수 있습니다"
        r"\w+을\s+볼\s+수\s+있어요",
        r"\w+을\s+합니다",
        r"\w+을\s+해요",
    ]

    for pattern in grammar_issues:
        if re.search(pattern, explanation):
            return False

    # 3. 선택지 다양성 검증
    low_quality_choices = [
        "놀이기구",
        "현대식 건물",
        "쇼핑몰",
        "아파트",
        "편의점",
        "카페",
        "PC방",
    ]

    for choice in choices:
        if any(bad_choice in choice for bad_choice in low_quality_choices):
            return False

    # 4. 교육적 가치 검증
    spot_keywords = spot_info.get("교육키워드", [])
    has_educational_connection = False

    full_content = question + " " + " ".join(choices) + " " + explanation

    for keyword in spot_keywords:
        if keyword in full_content:
            has_educational_connection = True
            break

    # 스팟 이름이나 위치가 언급되어야 함
    spot_name = spot_info.get("세부스팟", "")
    location = spot_info.get("메인장소", "")

    if not (spot_name in full_content or location in full_content):
        return False

    return True


def generate_fallback_quiz(spot_info: Dict, grade: int, quiz_number: int = 1) -> Dict:
    """품질 개선된 폴백 퀴즈 생성"""
    spot_name = spot_info.get("이름", spot_info.get("세부스팟", ""))
    location = spot_info.get("메인장소", "")
    keywords = spot_info.get("교육키워드", ["역사", "문화"])
    description = spot_info.get("설명", "")

    # 다양성을 위한 문제 유형 순환
    question_types = [
        "location",  # 위치/장소
        "feature",  # 특징/구조
        "purpose",  # 용도/역할
        "historical",  # 역사적 의미
        "cultural",  # 문화적 가치
    ]

    question_type = question_types[(quiz_number - 1) % len(question_types)]

    if grade <= 2:
        return generate_elementary_fallback(spot_info, question_type, grade)
    elif grade <= 4:
        return generate_intermediate_fallback(spot_info, question_type, grade)
    else:
        return generate_advanced_fallback(spot_info, question_type, grade)


def generate_elementary_fallback(
    spot_info: Dict, question_type: str, grade: int
) -> Dict:
    """1-2학년용 폴백 퀴즈"""
    spot_name = spot_info.get("세부스팟", "")
    location = spot_info.get("메인장소", "")

    fallback_templates = {
        "location": {
            "question": f"{spot_name}은 어느 도시에 있나요?",
            "choices": ["서울", "부산", "대구"],
            "correct_index": 0,
            "explanation": f"{spot_name}은 우리나라 수도인 서울에 있어요.",
        },
        "feature": {
            "question": f"{spot_name}은 어떤 건물인가요?",
            "choices": ["옛날 궁궐 건물", "새로 지은 건물", "외국 건물"],
            "correct_index": 0,
            "explanation": f"{spot_name}은 조상들이 살았던 옛날 궁궐 건물이에요.",
        },
        "purpose": {
            "question": f"{spot_name}에서는 누가 생활했나요?",
            "choices": ["임금님과 궁궐 사람들", "일반 시민들", "외국 사람들"],
            "correct_index": 0,
            "explanation": f"{spot_name}에서는 조선시대 임금님과 궁궐 사람들이 생활했어요.",
        },
    }

    return fallback_templates.get(question_type, fallback_templates["location"])


def generate_intermediate_fallback(
    spot_info: Dict, question_type: str, grade: int
) -> Dict:
    """3-4학년용 폴백 퀴즈"""
    spot_name = spot_info.get("세부스팟", "")
    location = spot_info.get("메인장소", "")
    keywords = spot_info.get("교육키워드", [])

    main_keyword = keywords[0] if keywords else "역사적 특징"

    fallback_templates = {
        "location": {
            "question": f"{location}에서 {spot_name}의 위치는 어디인가요?",
            "choices": ["궁궐 중심부", "궁궐 입구", "궁궐 밖"],
            "correct_index": 0,
            "explanation": f"{spot_name}은 {location}의 중심부에 위치한 중요한 건물입니다.",
        },
        "feature": {
            "question": f"{spot_name}의 건축적 특징으로 옳은 것은 무엇인가요?",
            "choices": [
                f"전통 {main_keyword}을 보여주는 구조",
                "서양식 건축 양식",
                "현대적 디자인",
            ],
            "correct_index": 0,
            "explanation": f"{spot_name}은 우리나라 전통 {main_keyword}을 잘 보여주는 건축물입니다.",
        },
        "purpose": {
            "question": f"{spot_name}의 주요 용도는 무엇이었나요?",
            "choices": ["국가 중요 행사 장소", "일반인 거주지", "상업 활동 공간"],
            "correct_index": 0,
            "explanation": f"{spot_name}은 조선시대 국가의 중요한 행사가 열리던 장소였습니다.",
        },
    }

    return fallback_templates.get(question_type, fallback_templates["feature"])


def generate_advanced_fallback(spot_info: Dict, question_type: str, grade: int) -> Dict:
    """5-6학년용 폴백 퀴즈"""
    spot_name = spot_info.get("세부스팟", "")
    location = spot_info.get("메인장소", "")
    keywords = spot_info.get("교육키워드", [])
    description = spot_info.get("설명", "")

    # 설명에서 키워드 추출
    key_elements = []
    if "정전" in description:
        key_elements.append("정치적 권위의 상징")
    if "임금" in description:
        key_elements.append("왕권을 나타내는 공간")
    if "신하" in description:
        key_elements.append("조정 정치의 중심")

    main_element = key_elements[0] if key_elements else "문화유산으로서의 가치"

    fallback_templates = {
        "historical": {
            "question": f"{spot_name}이 조선시대에 가졌던 역사적 의미는 무엇인가요?",
            "choices": [main_element, "종교적 의식 공간", "경제 활동 중심지"],
            "correct_index": 0,
            "explanation": f"{spot_name}은 조선시대 {main_element}으로서 중요한 역할을 했습니다.",
        },
        "cultural": {
            "question": f"{spot_name}이 현재 우리에게 주는 문화적 가치는 무엇인가요?",
            "choices": [
                "조선시대 문화와 역사 학습",
                "현대적 건축 기법 연구",
                "외국 문화 체험",
            ],
            "correct_index": 0,
            "explanation": f"{spot_name}을 통해 우리는 조선시대의 문화와 역사를 배우고 이해할 수 있습니다.",
        },
        "purpose": {
            "question": f"{spot_name}에서 이루어졌던 주요 활동은 무엇인가요?",
            "choices": [
                "국정 운영과 정치 활동",
                "종교적 의식과 제사",
                "상업적 거래와 교역",
            ],
            "correct_index": 0,
            "explanation": f"{spot_name}에서는 조선시대 국정 운영과 관련된 중요한 정치 활동들이 이루어졌습니다.",
        },
    }

    return fallback_templates.get(question_type, fallback_templates["historical"])


# 추가: 중복 방지를 위한 퀴즈 히스토리 관리
quiz_generation_history = {}


def generate_single_quiz(spot_info: Dict, grade: int, quiz_number: int = 1) -> Dict:
    """품질 개선된 단일 퀴즈 생성"""
    global quiz_generation_history

    spot_id = spot_info.get("세부스팟", "") + str(grade)

    # 이전 생성 이력 확인 (중복 방지)
    if spot_id not in quiz_generation_history:
        quiz_generation_history[spot_id] = []

    generation_method = "fallback"
    max_attempts = 3  # LLM 생성 재시도 횟수

    if openai_client:
        for attempt in range(max_attempts):
            try:
                prompt = create_enhanced_prompt(spot_info, grade, quiz_number + attempt)

                # 다양성을 위한 temperature 조정
                temperature = 0.4 + (quiz_number * 0.1) + (attempt * 0.05)
                temperature = min(temperature, 0.8)

                response = openai_client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[
                        {
                            "role": "system",
                            "content": f"당신은 초등학교 {grade}학년 현장학습 전문 교육자입니다. 매번 새롭고 창의적인 관점에서 교육적 가치가 높은 퀴즈를 만드세요. 같은 장소라도 다양한 각도에서 접근하세요.",
                        },
                        {"role": "user", "content": prompt},
                    ],
                    max_tokens=500,
                    temperature=temperature,
                    timeout=20,
                )

                llm_response = response.choices[0].message.content.strip()
                parsed_quiz = parse_llm_response(llm_response, spot_info, grade)

                if parsed_quiz:
                    # 중복 검사
                    question_text = parsed_quiz["question"]
                    if question_text not in quiz_generation_history[spot_id]:
                        quiz_generation_history[spot_id].append(question_text)
                        generation_method = "llm"
                        if "correct_index" in parsed_quiz:
                            parsed_quiz["correctIndex"] = parsed_quiz.pop(
                                "correct_index"
                            )
                        return {**parsed_quiz, "generation_method": generation_method}
                    else:
                        logger.info(
                            f"중복 문제 감지, 재시도 {attempt + 1}/{max_attempts}"
                        )
                        continue
                else:
                    logger.warning(
                        f"LLM 품질 검증 실패, 재시도 {attempt + 1}/{max_attempts}"
                    )

            except Exception as e:
                logger.error(f"LLM 생성 실패 (시도 {attempt + 1}/{max_attempts}): {e}")

    # 폴백 퀴즈 생성 (updated)
    fallback_quiz = generate_fallback_quiz(spot_info, grade, quiz_number)
    # correctIndex로 키 이름 변경
    if "correct_index" in fallback_quiz:
        fallback_quiz["correctIndex"] = fallback_quiz.pop("correct_index")
    return {**fallback_quiz, "generation_method": "fallback"}


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
