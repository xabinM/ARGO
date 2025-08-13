# quiz/generators.py - Import 문제 해결 완전판
"""
🎯 ARGO 통합 퀴즈 생성기 - 다양성 보장 시스템

핵심 기능:
1. 문제 중복 방지 (의미적 유사도 검사)
2. 7가지 문제 유형 강제 순환
3. 학년별 맞춤 최적화
4. 안정적인 파싱 + 폴백 시스템
"""

import re
import logging
import asyncio
import random
from typing import Dict, List, Optional, Tuple, Set
from dataclasses import dataclass
from datetime import datetime
from difflib import SequenceMatcher
import openai

logger = logging.getLogger(__name__)


# ===== 설정 클래스 =====
@dataclass
class QuizConfig:
    """퀴즈 생성 설정"""

    grade: int = 5
    difficulty: str = "normal"
    max_attempts: int = 3
    enable_validation: bool = True
    fallback_quality: float = 0.75
    quality_threshold: float = 0.7


# ===== 강력한 파싱기 (독립 구현) =====
class RobustParser:
    """강력한 파싱 시스템"""

    def __init__(self):
        self.question_patterns = [
            r"문제\s*[:：\-]\s*(.+?)(?=\n|1\)|①)",
            r"Q\s*[:：\-]?\s*(.+?)(?=\n|1\)|①)",
            r"^(.+?)(?=\n\s*1\)|①)",
            r"(.+\?)\s*(?=1\)|①)",
        ]

        self.choices_patterns = [
            r"1\)\s*([^2\n]+?)\s*2\)\s*([^3\n]+?)\s*3\)\s*([^정답\n]+?)(?=정답|답|해설|$)",
            r"①\s*([^②\n]+?)\s*②\s*([^③\n]+?)\s*③\s*([^정답\n]+?)(?=정답|답|해설|$)",
        ]

        self.answer_patterns = [
            r"정답\s*[:：\-]\s*(\d+)",
            r"답\s*[:：\-]\s*(\d+)",
        ]

        self.explanation_patterns = [
            r"해설\s*[:：\-]\s*(.+?)(?=\n\s*$|$)",
            r"설명\s*[:：\-]\s*(.+?)(?=\n\s*$|$)",
        ]

    def parse(self, response: str, spot_info: Dict, grade: int) -> Optional[Dict]:
        """통합 파싱 함수"""
        try:
            text = re.sub(r"\s+", " ", response.strip())

            if not all(key in text for key in ["1)", "2)", "3)"]):
                return None

            question = self._extract_question(text)
            choices = self._extract_choices(text)
            correct_index = self._extract_answer(text)
            explanation = self._extract_explanation(text)

            if not all(
                [question, len(choices) == 3, correct_index is not None, explanation]
            ):
                return None

            quiz_data = {
                "question": question,
                "choices": choices,
                "correctIndex": correct_index,
                "explanation": explanation,
            }

            if not self._quality_check(quiz_data, spot_info, grade):
                return None

            quality_score = self._calculate_quality(quiz_data, spot_info)

            return {
                **quiz_data,
                "generation_method": "llm_parsed",
                "quality_score": quality_score,
            }

        except Exception as e:
            logger.error(f"파싱 실패: {e}")
            return None

    def _extract_question(self, text: str) -> Optional[str]:
        for pattern in self.question_patterns:
            match = re.search(pattern, text, re.DOTALL)
            if match:
                question = match.group(1).strip()
                question = re.sub(r"^(문제|Q)\s*[:：\-]?\s*", "", question)
                if 8 <= len(question) <= 150:
                    return question
        return None

    def _extract_choices(self, text: str) -> List[str]:
        for pattern in self.choices_patterns:
            match = re.search(pattern, text, re.DOTALL)
            if match:
                choices = [match.group(i).strip() for i in range(1, 4)]
                cleaned = []
                for choice in choices:
                    choice = re.sub(r"^[\d\)①②③\.\s]+", "", choice).strip()
                    choice = re.sub(r"(정답|답|해설).*$", "", choice).strip()
                    if choice and len(choice) >= 2:
                        cleaned.append(choice)

                if len(cleaned) == 3 and len(set(cleaned)) == 3:
                    return cleaned
        return []

    def _extract_answer(self, text: str) -> Optional[int]:
        for pattern in self.answer_patterns:
            match = re.search(pattern, text)
            if match:
                try:
                    answer = int(match.group(1))
                    if 1 <= answer <= 3:
                        return answer - 1
                except ValueError:
                    continue
        return None

    def _extract_explanation(self, text: str) -> Optional[str]:
        for pattern in self.explanation_patterns:
            match = re.search(pattern, text, re.DOTALL)
            if match:
                explanation = match.group(1).strip()
                if len(explanation) >= 10:
                    return explanation
        return None

    def _quality_check(self, quiz_data: Dict, spot_info: Dict, grade: int) -> bool:
        content = f"{quiz_data['question']} {' '.join(quiz_data['choices'])} {quiz_data['explanation']}"

        banned_words = ["놀이기구", "편의점", "카페", "PC방"]
        if any(word in content for word in banned_words):
            return False

        spot_name = spot_info.get("세부스팟", "")
        location = spot_info.get("메인장소", "")
        if spot_name and location:
            if not (spot_name in content or location in content):
                return False

        return True

    def _calculate_quality(self, quiz_data: Dict, spot_info: Dict) -> float:
        score = 0.7
        content = f"{quiz_data['question']} {' '.join(quiz_data['choices'])} {quiz_data['explanation']}"

        keywords = spot_info.get("교육키워드", [])
        if any(keyword in content for keyword in keywords):
            score += 0.2

        return min(score, 1.0)


# ===== 문제 다양성 관리자 =====
class QuizDiversityManager:
    """문제 다양성 보장 시스템"""

    def __init__(self):
        self.generated_questions = []
        self.used_perspectives = []
        self.used_keywords = set()

        # 문제 유형 정의
        self.question_types = [
            {
                "type": "관찰형",
                "focus": "직접 보이는 특징",
                "templates": [
                    "{spot}을 보면 가장 눈에 띄는 특징은 무엇인가요?",
                    "{spot}의 외관에서 특별한 점은 무엇인가요?",
                ],
            },
            {
                "type": "기능형",
                "focus": "용도와 역할",
                "templates": [
                    "{spot}은 어떤 목적으로 만들어졌나요?",
                    "{spot}에서 주로 어떤 일이 이루어졌나요?",
                ],
            },
            {
                "type": "역사형",
                "focus": "시대적 배경",
                "templates": [
                    "{spot}은 언제 만들어졌나요?",
                    "{spot}과 관련된 역사적 사건은?",
                ],
            },
            {
                "type": "문화형",
                "focus": "문화적 의미",
                "templates": [
                    "{spot}이 우리 문화에서 갖는 의미는?",
                    "{spot}에 담긴 조상들의 지혜는?",
                ],
            },
            {
                "type": "체험형",
                "focus": "활동과 경험",
                "templates": [
                    "{spot}에서 할 수 있는 활동은?",
                    "{spot}에서 느낄 수 있는 것은?",
                ],
            },
            {
                "type": "비교형",
                "focus": "다른 것과의 차이",
                "templates": [
                    "{spot}과 다른 건물의 차이점은?",
                    "{spot}만의 독특한 특징은?",
                ],
            },
            {
                "type": "교육형",
                "focus": "배울 수 있는 것",
                "templates": [
                    "{spot}에서 배울 수 있는 것은?",
                    "{spot}이 주는 교훈은?",
                ],
            },
        ]

    def check_similarity(self, new_question: str, threshold: float = 0.7) -> bool:
        """기존 문제와의 유사도 검사"""
        if not self.generated_questions:
            return False

        for existing_question in self.generated_questions:
            similarity = SequenceMatcher(None, new_question, existing_question).ratio()
            if similarity > threshold:
                logger.warning(f"높은 유사도 감지: {similarity:.2f}")
                return True

        return False

    def get_next_question_type(self, grade: int, quiz_number: int) -> Dict:
        """다음 문제 유형 결정 (강제 순환)"""
        if grade <= 2:
            available_types = ["관찰형", "체험형"]
        elif grade <= 4:
            available_types = ["관찰형", "기능형", "체험형", "비교형"]
        else:
            available_types = [q["type"] for q in self.question_types]

        unused_types = [
            qt
            for qt in self.question_types
            if qt["type"] in available_types
            and qt["type"] not in self.used_perspectives
        ]

        if not unused_types:
            self.used_perspectives = []
            unused_types = [
                qt for qt in self.question_types if qt["type"] in available_types
            ]

        selected_type = unused_types[quiz_number % len(unused_types)]
        self.used_perspectives.append(selected_type["type"])

        return selected_type

    def generate_diverse_keywords(
        self, spot_info: Dict, question_type: Dict
    ) -> List[str]:
        """다양한 키워드 생성"""
        base_keywords = spot_info.get("교육키워드", [])

        type_keywords = {
            "관찰형": ["외관", "모양", "구조", "특징"],
            "기능형": ["용도", "역할", "목적", "기능"],
            "역사형": ["시대", "역사", "과거", "전통"],
            "문화형": ["문화", "의미", "가치", "정신"],
            "체험형": ["활동", "경험", "느낌", "체험"],
            "비교형": ["차이", "특별함", "독특함", "비교"],
            "교육형": ["교훈", "배움", "지식", "학습"],
        }

        additional_keywords = type_keywords.get(question_type["type"], [])
        all_keywords = base_keywords + additional_keywords
        unused_keywords = [k for k in all_keywords if k not in self.used_keywords]

        if not unused_keywords:
            self.used_keywords.clear()
            unused_keywords = all_keywords

        selected = unused_keywords[:3]
        self.used_keywords.update(selected)

        return selected

    def register_generated_question(self, question: str):
        """생성된 문제 등록"""
        self.generated_questions.append(question)
        if len(self.generated_questions) > 10:
            self.generated_questions = self.generated_questions[-10:]

    def reset_for_new_spot(self):
        """새로운 스팟을 위한 리셋"""
        self.generated_questions = []
        self.used_perspectives = []
        self.used_keywords.clear()


# ===== 다양성 보장 프롬프트 생성기 =====
class DiversityEnhancedPromptGenerator:
    """문제 다양성을 보장하는 프롬프트 생성기"""

    def __init__(self):
        self.diversity_manager = QuizDiversityManager()

    def create_diverse_prompt(
        self, spot_info: Dict, grade: int, quiz_number: int
    ) -> str:
        """다양성이 보장된 프롬프트 생성"""

        spot_name = spot_info.get("세부스팟", "이곳")
        location = spot_info.get("메인장소", "")
        description = spot_info.get("설명", "")

        question_type = self.diversity_manager.get_next_question_type(
            grade, quiz_number
        )
        diverse_keywords = self.diversity_manager.generate_diverse_keywords(
            spot_info, question_type
        )

        if grade <= 2:
            vocab_level = "유치원~2학년 수준의 쉬운 말"
            sentence_style = "짧고 간단한 문장"
        elif grade <= 4:
            vocab_level = "3-4학년 교과서 어휘"
            sentence_style = "명확하고 이해하기 쉬운 문장"
        else:
            vocab_level = "5-6학년 사회과 용어"
            sentence_style = "분석적 사고가 필요한 문장"

        previous_questions = "\n".join(
            [f"- {q}" for q in self.diversity_manager.generated_questions[-3:]]
        )

        duplicate_prevention = f"""
    🚫 **중복 방지 (매우 중요!):**
    다음과 유사한 문제는 절대 만들지 마세요:
    {previous_questions if previous_questions else "- (아직 이전 문제 없음)"}

    반드시 완전히 다른 관점에서 문제를 만드세요!
    """

        # 🔥 객관적 사실 기반 문제 강제
        objective_requirements = f"""
    📏 **객관적 사실 기반 필수 조건:**
    - 감정이나 기분을 묻는 문제 절대 금지 ("어떤 기분", "어떻게 느꼈을까" 등)
    - 개인적 의견을 묻는 문제 금지 ("어떻게 생각하나요", "당신이라면" 등)
    - 반드시 역사적 사실, 건축적 특징, 구체적 정보만 질문
    - 정답이 명확하고 검증 가능한 객관적 사실만 다룰 것

    ✅ **좋은 예시:**
    - "{spot_name}은 언제 지어졌나요?"
    - "{spot_name}의 주요 기능은 무엇이었나요?"
    - "{spot_name}에서 볼 수 있는 건축 특징은?"

    ❌ **절대 금지 예시:**
    - "어떤 기분이 들었을까요?"
    - "어떻게 생각하나요?"
    - "당신이라면 어떻게 했을까요?"
    """

        specific_instruction = f"""
    🎯 **이번 문제 필수 조건:**
    - 문제 유형: {question_type['type']} ({question_type['focus']})
    - 핵심 키워드: {', '.join(diverse_keywords)}
    - 관점: {question_type['focus']}에 초점을 맞춘 **객관적 사실** 문제

    ✅ **반드시 지킬 것:**
    - {vocab_level}만 사용
    - {sentence_style}으로 구성  
    - 현장에서 직접 확인 가능한 구체적 사실
    - 정답이 명확하고 객관적인 역사적/건축적 정보
    - 교과서에 나올 법한 교육적 내용
    """

        return f"""당신은 초등학교 현장학습 전문 교육자입니다. {grade}학년용 삼지선다 퀴즈를 만드세요.

    📍 **장소 정보:**
    - 위치: {location}
    - 장소: {spot_name}  
    - 설명: {description}

    {duplicate_prevention}

    {objective_requirements}

    {specific_instruction}

    📋 **정확한 출력 형식:**
    문제: [객관적 사실 질문]
    1) [구체적 사실 선택지1]
    2) [구체적 사실 선택지2] 
    3) [구체적 사실 선택지3]
    정답: [1, 2, 3 중 숫자만]
    해설: [역사적/건축적 사실 근거로 한 문장 설명]

    위 조건을 모두 지켜서 {question_type['type']} 관점의 **객관적이고 교육적인** 문제를 만들어주세요."""

    def register_question(self, question: str):
        """생성된 문제 등록"""
        self.diversity_manager.register_generated_question(question)

    def reset_for_new_spot(self):
        """새로운 스팟용 리셋"""
        self.diversity_manager.reset_for_new_spot()


# ===== 중복 검증 파서 =====
class DuplicateAwareParser:
    """중복 검증이 포함된 파서"""

    def __init__(self, diversity_manager: QuizDiversityManager):
        self.diversity_manager = diversity_manager
        self.base_parser = RobustParser()

    def parse_with_diversity_check(
        self, response: str, spot_info: Dict, grade: int
    ) -> Optional[Dict]:
        """다양성 검증이 포함된 파싱"""

        parsed_result = self.base_parser.parse(response, spot_info, grade)

        if not parsed_result:
            return None

        question = parsed_result.get("question", "")

        if self.diversity_manager.check_similarity(question, threshold=0.6):
            logger.warning(f"중복 문제 감지, 거부: {question}")
            return None

        self.diversity_manager.register_generated_question(question)

        return {
            **parsed_result,
            "diversity_verified": True,
            "parsing_method": "diversity_checked",
        }


# ===== 다양성 보장 생성기 =====
class DiversityEnhancedGenerator:
    """문제 다양성을 보장하는 퀴즈 생성기"""

    def __init__(self, openai_client=None):
        self.openai_client = openai_client
        self.prompt_gen = DiversityEnhancedPromptGenerator()
        self.parser = DuplicateAwareParser(self.prompt_gen.diversity_manager)

        # 통계
        self.stats = {
            "total_attempts": 0,
            "diversity_rejections": 0,
            "unique_generated": 0,
            "similarity_scores": [],
        }

    def generate_quiz(self, spot_info: Dict, grade: int, quiz_number: int = 1) -> Dict:
        """다양성 보장 퀴즈 생성"""
        self.stats["total_attempts"] += 1

        # 첫 번째 문제면 리셋
        if quiz_number == 1:
            self.prompt_gen.reset_for_new_spot()

        # 최대 시도 횟수
        max_attempts = 5

        for attempt in range(max_attempts):
            try:
                prompt = self.prompt_gen.create_diverse_prompt(
                    spot_info, grade, quiz_number + attempt
                )

                llm_response = self._call_llm(prompt, grade)

                if llm_response:
                    parsed_quiz = self.parser.parse_with_diversity_check(
                        llm_response, spot_info, grade
                    )

                    if parsed_quiz:
                        self.stats["unique_generated"] += 1
                        return parsed_quiz
                    else:
                        self.stats["diversity_rejections"] += 1
                        logger.info(
                            f"다양성 검증 실패, 재시도 {attempt + 1}/{max_attempts}"
                        )

            except Exception as e:
                logger.error(f"생성 시도 {attempt + 1} 실패: {e}")
                continue

        # 모든 시도 실패시 강제 다양성 폴백
        return self._generate_forced_diverse_fallback(spot_info, grade, quiz_number)

    def _call_llm(self, prompt: str, grade: int) -> Optional[str]:
        """LLM 호출"""
        if not self.openai_client:
            return None

        try:
            response = self.openai_client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {
                        "role": "system",
                        "content": f"""당신은 초등학교 {grade}학년 현장학습 전문 교육자입니다.

중요: 
1. 이전 문제와 완전히 다른 관점에서 문제를 만드세요
2. 중복되는 내용은 절대 안됩니다
3. 각 문제는 독특하고 다양해야 합니다""",
                    },
                    {"role": "user", "content": prompt},
                ],
                max_tokens=500,
                temperature=0.8,
                timeout=25,
            )
            return response.choices[0].message.content.strip()
        except Exception as e:
            logger.error(f"LLM 호출 실패: {e}")
            return None

    def _generate_forced_diverse_fallback(
        self, spot_info: Dict, grade: int, quiz_number: int
    ) -> Dict:
        """강제 다양성 폴백"""
        spot_name = spot_info.get("세부스팟", "이곳")

        # 문제 번호별로 완전히 다른 유형의 폴백
        fallback_templates = [
            {  # 1번 문제
                "question": f"{spot_name}은 어디에 있나요?",
                "choices": ["서울", "부산", "제주도"],
                "correct": 0,
                "explanation": f"{spot_name}은 서울에 있습니다.",
            },
            {  # 2번 문제
                "question": f"{spot_name}에서 볼 수 있는 것은 무엇인가요?",
                "choices": ["전통 건축물", "현대 건물", "놀이기구"],
                "correct": 0,
                "explanation": f"{spot_name}에서는 전통 건축물을 볼 수 있습니다.",
            },
            {  # 3번 문제
                "question": f"{spot_name}을 보존해야 하는 이유는 무엇인가요?",
                "choices": ["문화유산이므로", "돈이 되므로", "크기가 크므로"],
                "correct": 0,
                "explanation": f"{spot_name}은 우리나라의 소중한 문화유산입니다.",
            },
        ]

        template = fallback_templates[(quiz_number - 1) % len(fallback_templates)]

        return {
            "question": template["question"],
            "choices": template["choices"],
            "correctIndex": template["correct"],
            "explanation": template["explanation"],
            "generation_method": "diversity_fallback",
            "quality_score": 0.6,
            "diversity_verified": True,
        }

    async def generate_multiple_quizzes(
        self, spot_info: Dict, count: int, grade: int = 5
    ) -> List[Dict]:
        """다중 퀴즈 생성 (다양성 보장)"""
        results = []

        for i in range(count):
            quiz = self.generate_quiz(spot_info, grade, i + 1)
            results.append(quiz)

            # 약간의 딜레이로 안정성 확보
            await asyncio.sleep(0.2)

        return results

    def get_diversity_stats(self) -> Dict:
        """다양성 통계"""
        total = self.stats["total_attempts"]
        if total == 0:
            return self.stats

        return {
            **self.stats,
            "diversity_success_rate": f"{(self.stats['unique_generated'] / total * 100):.1f}%",
            "rejection_rate": f"{(self.stats['diversity_rejections'] / total * 100):.1f}%",
        }


# ===== 편의 함수들 =====
def quick_generate_quiz(openai_client, spot_info: Dict, grade: int = 5) -> Dict:
    """빠른 퀴즈 생성 (외부 호출용)"""
    generator = DiversityEnhancedGenerator(openai_client)
    config = QuizConfig(grade=grade)
    return generator.generate_quiz(spot_info, grade)


def create_quiz_config(
    grade: int, difficulty: str = "normal", high_quality: bool = True
) -> QuizConfig:
    """퀴즈 설정 생성"""
    return QuizConfig(
        grade=grade,
        difficulty=difficulty,
        max_attempts=3 if high_quality else 2,
        enable_validation=high_quality,
        fallback_quality=0.8 if high_quality else 0.7,
    )


# ===== 기존 호환성 유지 =====
RapidCompleteGenerator = DiversityEnhancedGenerator
AntiFallbackGenerator = DiversityEnhancedGenerator
QuizGenerator = DiversityEnhancedGenerator
RapidQuizGenerator = DiversityEnhancedGenerator


# ===== 테스트 함수 =====
def test_diversity_enhanced():
    """다양성 보장 테스트"""
    spot_info = {
        "메인장소": "경복궁",
        "세부스팟": "근정전",
        "설명": "조선시대 정전",
        "교육키워드": ["조선시대", "정치", "왕"],
    }

    generator = DiversityEnhancedGenerator(None)  # OpenAI 없이 테스트

    print("🧪 다양성 보장 테스트")
    for i in range(3):
        quiz = generator.generate_quiz(spot_info, 5, i + 1)
        print(f"\n문제 {i+1}: {quiz['question']}")
        print(f"선택지: {quiz['choices']}")
        print(f"정답: {quiz['correctIndex'] + 1}번")
        print(f"해설: {quiz['explanation']}")
        print(f"유형: {quiz.get('generation_method', 'unknown')}")

    print(f"\n📊 다양성 통계: {generator.get_diversity_stats()}")


# __all__ 정의
__all__ = [
    "DiversityEnhancedGenerator",
    "RapidCompleteGenerator",
    "QuizConfig",
    "QuizDiversityManager",
    "quick_generate_quiz",
    "create_quiz_config",
]

if __name__ == "__main__":
    test_diversity_enhanced()
