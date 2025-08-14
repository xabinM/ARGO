# services/quiz_service.py - Anti-Fallback Generator 사용 버전
"""
🎯 강화된 퀴즈 생성 서비스 (Anti-Fallback 버전)

주요 기능:
- Anti-Fallback Generator 사용
- 문법 교정 자동 적용
- Fallback 원인 분석
- 품질 모니터링
"""

import asyncio
import logging
import sys
import os
from typing import List, Dict, Optional
import openai
from datetime import datetime
from pydantic import BaseModel

# 경로 추가
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, BASE_DIR)

# 플래그 변수들 먼저 정의
quiz_module_available = False
anti_fallback_available = False
generators_available = False
spots_data_available = False

# 퀴즈 생성기 import
QuizGenerator = None
QuizConfig = None

try:
    from quiz.generators import DiversityEnhancedGenerator as QuizGenerator, QuizConfig

    quiz_module_available = True
    anti_fallback_available = True  # 🔥 이 변수가 빠져있었음
    generators_available = True
    print("✅ DiversityEnhancedGenerator 로드 성공")
except ImportError as e:
    print(f"⚠️ DiversityEnhancedGenerator import 실패: {e}")
    QuizGenerator = None
    QuizConfig = None
    quiz_module_available = False
    anti_fallback_available = False
    generators_available = False

# 스팟 데이터 import (경로 수정)
try:
    from data.expanded_educational_spots import EXPANDED_EDUCATIONAL_SPOTS

    spots_data_available = True
    logging.info(f"✅ 스팟 데이터 로드 성공: {len(EXPANDED_EDUCATIONAL_SPOTS)}개")
except ImportError as e:
    logging.warning(f"스팟 데이터 import 실패: {e}")
    EXPANDED_EDUCATIONAL_SPOTS = []
    spots_data_available = False

logger = logging.getLogger(__name__)


class GeneratedQuizProblem(BaseModel):
    """생성된 퀴즈 문제"""

    question: str
    choices: List[str]
    correctIndex: int
    explanation: str
    generation_method: str = "anti_fallback"
    quality_score: float = 0.0
    parsing_method: Optional[str] = None


class QuizService:
    """강화된 퀴즈 생성 서비스 (Anti-Fallback)"""

    def __init__(self, settings):
        self.settings = settings
        self.quiz_generator = None
        self.is_ready = False
        self.is_llm_available = False

        # OpenAI 클라이언트 설정
        self.openai_client = self._setup_openai_client()

        # 성능 통계 (확장)
        self.stats = {
            "total_generated": 0,
            "anti_fallback_success": 0,
            "grammar_corrected": 0,
            "quality_improved": 0,
            "fallback_used": 0,
            "average_quality": 0.0,
            "parsing_success_rate": 0.0,
        }

    def _setup_openai_client(self):
        """OpenAI 클라이언트 설정"""
        api_keys = [
            ("GMS_API_KEY", "https://gms.ssafy.io/gmsapi/api.openai.com/v1"),
            ("OPENAI_API_KEY", "https://api.openai.com/v1"),
        ]

        for key_name, base_url in api_keys:
            api_key = getattr(self.settings, key_name, None) or os.getenv(key_name)
            if api_key and len(api_key.strip()) >= 20:
                try:
                    client = openai.OpenAI(api_key=api_key.strip(), base_url=base_url)
                    # 연결 테스트
                    client.chat.completions.create(
                        model="gpt-4o-mini",
                        messages=[{"role": "user", "content": "test"}],
                        max_tokens=5,
                        timeout=10,
                    )
                    self.is_llm_available = True
                    logger.info(f"✅ {key_name} API 연결 성공")
                    return client
                except Exception as e:
                    logger.warning(f"⚠️ {key_name} 연결 실패: {e}")
                    continue

        logger.warning("⚠️ 사용 가능한 API 키 없음")
        return None

    async def initialize(self):
        """서비스 초기화"""
        logger.info("🎯 강화된 퀴즈 서비스 초기화 중...")

        if not anti_fallback_available:
            logger.error("❌ Anti-Fallback Generator를 사용할 수 없습니다")
            self.is_ready = False
            return

        if not spots_data_available:
            logger.warning("⚠️ 스팟 데이터가 없습니다. 기본 모드로 실행")

        # 강화된 퀴즈 생성기 초기화
        try:
            self.quiz_generator = QuizGenerator(self.openai_client)
            self.is_ready = True

            generator_type = "Anti-Fallback" if anti_fallback_available else "Basic"
            logger.info(
                f"✅ {generator_type} 퀴즈 서비스 준비 완료 (LLM: {'활성' if self.is_llm_available else '비활성'})"
            )

        except Exception as e:
            logger.error(f"❌ 퀴즈 생성기 초기화 실패: {e}")
            self.is_ready = False

    async def generate_single_quiz(
        self, spot_info: Dict, grade: int = 5, difficulty: str = "normal"
    ) -> GeneratedQuizProblem:
        """단일 퀴즈 생성 (강화된 버전)"""

        if not self.is_ready:
            raise RuntimeError("퀴즈 서비스가 준비되지 않았습니다")

        # GPS 데이터 보완
        if not spot_info.get("위도") or not spot_info.get("경도"):
            spot_info["위도"] = 37.5665
            spot_info["경도"] = 126.9780

        # 🔥 올바른 파라미터로 호출 (config 제거!)
        quiz_data = self.quiz_generator.generate_quiz(spot_info, grade, 1)

        if quiz_data:
            # 통계 업데이트
            self.stats["total_generated"] += 1

            # Anti-Fallback 성공 여부 확인
            if quiz_data.get("generation_method") in ["llm_parsed", "llm_recovered"]:
                self.stats["anti_fallback_success"] += 1

                # 문법 교정 적용된 경우 카운트
                if quiz_data.get("parsing_method") in ["standard", "recovered"]:
                    self.stats["grammar_corrected"] += 1
            else:
                self.stats["fallback_used"] += 1

            # 품질 점수 누적
            quality = quiz_data.get("quality_score", 0.0)
            if quality >= 0.8:
                self.stats["quality_improved"] += 1

            self.stats["average_quality"] = (
                self.stats["average_quality"] * (self.stats["total_generated"] - 1)
                + quality
            ) / self.stats["total_generated"]

            # 파싱 성공률 계산
            if hasattr(self.quiz_generator, "get_fallback_analysis"):
                analysis = self.quiz_generator.get_fallback_analysis()
                if "LLM_성공률" in analysis:
                    success_rate_str = analysis["LLM_성공률"].replace("%", "")
                    self.stats["parsing_success_rate"] = float(success_rate_str)

            return GeneratedQuizProblem(**quiz_data)

        # 최후 비상 폴백
        logger.error("❌ 모든 퀴즈 생성 방법 실패")
        return self._create_emergency_fallback(spot_info, grade)

    async def generate_multiple_quizzes(
        self, spot_info: Dict, count: int, grade: int = 5, difficulty: str = "normal"
    ) -> List[GeneratedQuizProblem]:
        """다중 퀴즈 생성 (Anti-Fallback 버전)"""

        if count <= 0 or count > 10:
            raise ValueError("퀴즈 개수는 1-10개 사이여야 합니다")

        # Anti-Fallback Generator의 다중 생성 사용
        if hasattr(self.quiz_generator, "generate_multiple_quizzes"):
            quiz_list = await self.quiz_generator.generate_multiple_quizzes(
                spot_info, count, grade
            )

            problems = []
            for quiz_data in quiz_list:
                # 통계 업데이트
                self._update_stats(quiz_data)
                problems.append(GeneratedQuizProblem(**quiz_data))

            return problems
        else:
            # 순차 생성 (기존 방식)
            problems = []
            for i in range(count):
                problem = await self.generate_single_quiz(spot_info, grade, difficulty)
                problems.append(problem)

            return problems

    def _update_stats(self, quiz_data: Dict):
        """통계 업데이트 헬퍼"""
        self.stats["total_generated"] += 1

        if quiz_data.get("generation_method") in ["llm_parsed", "llm_recovered"]:
            self.stats["anti_fallback_success"] += 1
        else:
            self.stats["fallback_used"] += 1

        quality = quiz_data.get("quality_score", 0.0)
        if quality >= 0.8:
            self.stats["quality_improved"] += 1

        self.stats["average_quality"] = (
            self.stats["average_quality"] * (self.stats["total_generated"] - 1)
            + quality
        ) / self.stats["total_generated"]

    async def generate_problems_by_name(
        self, spot_name: str, count: int, grade: int = 5
    ) -> List[GeneratedQuizProblem]:
        """스팟명으로 문제 생성 (강화된 버전)"""

        if not self.is_ready:
            raise RuntimeError("퀴즈 서비스가 준비되지 않았습니다")

        # 스팟 정보 찾기
        spot_info = self._find_spot_by_name(spot_name)
        if not spot_info:
            raise ValueError(f"스팟 '{spot_name}'을 찾을 수 없습니다")

        # 다중 퀴즈 생성
        return await self.generate_multiple_quizzes(spot_info, count, grade)

    async def generate_problems_by_id(
        self, spot_id: int, count: int, grade: int = 5
    ) -> List[GeneratedQuizProblem]:
        """스팟 ID로 문제 생성 (강화된 버전)"""

        if not self.is_ready:
            raise RuntimeError("퀴즈 서비스가 준비되지 않았습니다")

        # 스팟 정보 찾기
        spot_info = self._find_spot_by_id(spot_id)
        if not spot_info:
            raise ValueError(f"스팟 ID {spot_id}를 찾을 수 없습니다")

        # 다중 퀴즈 생성
        return await self.generate_multiple_quizzes(spot_info, count, grade)

    def _find_spot_by_name(self, spot_name: str) -> Optional[Dict]:
        """스팟명으로 스팟 정보 검색"""
        if not spots_data_available:
            return self._create_default_spot(spot_name)

        for idx, spot in enumerate(EXPANDED_EDUCATIONAL_SPOTS):
            if (
                spot.get("세부스팟") == spot_name
                or spot.get("이름") == spot_name
                or spot_name in spot.get("세부스팟", "")
                or spot_name in spot.get("메인장소", "")
            ):
                return {**spot, "spot_id": idx + 1}

        return self._create_default_spot(spot_name)

    def _find_spot_by_id(self, spot_id: int) -> Optional[Dict]:
        """스팟 ID로 스팟 정보 검색"""
        if not spots_data_available:
            return self._create_default_spot(f"스팟 {spot_id}")

        if 1 <= spot_id <= len(EXPANDED_EDUCATIONAL_SPOTS):
            spot = EXPANDED_EDUCATIONAL_SPOTS[spot_id - 1]
            return {**spot, "spot_id": spot_id}

        return self._create_default_spot(f"스팟 {spot_id}")

    def _create_default_spot(self, spot_name: str) -> Dict:
        """기본 스팟 정보 생성"""
        return {
            "이름": spot_name,
            "메인장소": "서울",
            "세부스팟": spot_name,
            "설명": f"{spot_name}은 교육적 가치가 있는 장소입니다.",
            "위치": "서울특별시",
            "위도": 37.5665,
            "경도": 126.9780,
            "교육키워드": ["역사", "문화", "학습"],
            "학년적합도": [1, 2, 3, 4, 5, 6],
            "spot_id": 1,
        }

    def _create_emergency_fallback(
        self, spot_info: Dict, grade: int
    ) -> GeneratedQuizProblem:
        """비상 폴백 퀴즈 - 🔥 실제 정보 기반"""
        spot_name = spot_info.get("세부스팟", spot_info.get("이름", "이곳"))
        location = spot_info.get("메인장소", spot_info.get("위치", "서울"))
        description = spot_info.get("설명", "역사적 장소")
        keywords = spot_info.get("교육키워드", ["역사", "문화"])
        
        # 🔥 학년별 맞춤 응급 질문
        if grade <= 2:
            question = f"{spot_name}은 어떤 곳인가요?"
            choices = ["옛날 사람들이 살던 곳", "놀이터", "마트"]
            explanation = f"{spot_name}은 옛날 사람들이 살던 소중한 곳입니다."
        elif grade <= 4:
            primary_keyword = keywords[0] if keywords else "문화재"
            question = f"{spot_name}에서 가장 중요한 것은 무엇인가요?"
            choices = [f"{primary_keyword}의 가치", "현대적 편의시설", "상업적 이익"]
            explanation = f"{spot_name}은 {primary_keyword}의 가치가 매우 높은 곳입니다."
        else:
            question = f"{spot_name}이 현재까지 보존되는 이유는 무엇인가요?"
            choices = [
                "역사적·문화적 가치가 높기 때문에",
                "관광 수입을 위해서",
                "땅값이 비싸서"
            ]
            explanation = f"{spot_name}은 우리나라의 역사와 문화를 보여주는 소중한 유산으로 후손들에게 전해져야 할 가치가 있습니다."

        return GeneratedQuizProblem(
            question=question,
            choices=choices,
            correctIndex=0,
            explanation=explanation,
            generation_method="smart_emergency_fallback",  # 🔥 새로운 타입
            quality_score=0.8,  # 🔥 품질 향상
        )

    def get_fallback_analysis(self) -> Dict:
        """Fallback 분석 결과 (Anti-Fallback Generator 기능)"""
        if hasattr(self.quiz_generator, "get_fallback_analysis"):
            return self.quiz_generator.get_fallback_analysis()
        else:
            return {
                "message": "Anti-Fallback 분석 기능을 사용할 수 없습니다",
                "basic_stats": self.stats,
            }

    async def cleanup(self):
        """서비스 정리"""
        logger.info("🔄 강화된 퀴즈 서비스 정리 중...")

        # 통계 출력
        if self.stats["total_generated"] > 0:
            logger.info(f"📊 퀴즈 생성 통계:")
            logger.info(f"   총 생성: {self.stats['total_generated']}개")
            logger.info(
                f"   Anti-Fallback 성공: {self.stats['anti_fallback_success']}개"
            )
            logger.info(f"   문법 교정: {self.stats['grammar_corrected']}개")
            logger.info(f"   고품질 (0.8+): {self.stats['quality_improved']}개")
            logger.info(f"   Fallback 사용: {self.stats['fallback_used']}개")
            logger.info(f"   평균 품질: {self.stats['average_quality']:.2f}")
            logger.info(f"   파싱 성공률: {self.stats['parsing_success_rate']:.1f}%")

        # Generator 정리
        if hasattr(self.quiz_generator, "reset_statistics"):
            self.quiz_generator.reset_statistics()

    def get_stats(self) -> Dict:
        """서비스 통계 반환 (확장된 버전)"""
        basic_stats = {
            **self.stats,
            "is_ready": self.is_ready,
            "llm_available": self.is_llm_available,
            "spots_available": spots_data_available,
            "total_spots": (
                len(EXPANDED_EDUCATIONAL_SPOTS) if spots_data_available else 0
            ),
            "anti_fallback_enabled": anti_fallback_available,
        }

        # Anti-Fallback 분석 추가
        if hasattr(self.quiz_generator, "get_fallback_analysis"):
            basic_stats["fallback_analysis"] = (
                self.quiz_generator.get_fallback_analysis()
            )

        return basic_stats
