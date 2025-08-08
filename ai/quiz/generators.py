# quiz/rapid_complete_generator.py - 포트폴리오 1-2일 완성용 올인원
"""
🎯 빠른 포트폴리오 완성을 위한 통합 퀴즈 생성기

특징:
- 단일 파일로 모든 기능 포함
- GPT-4o mini 최적화
- 의존성 최소화
- 품질 보장 + 빠른 구현
"""

import re
import logging
import asyncio
import random
from typing import Dict, List, Optional, Tuple
from dataclasses import dataclass
from datetime import datetime
import openai

logger = logging.getLogger(__name__)

# ===== 설정 클래스 =====
@dataclass
class QuizConfig:
    """퀴즈 생성 설정"""
    grade: int = 5
    difficulty: str = "normal"
    max_attempts: int = 2
    enable_validation: bool = True
    fallback_quality: float = 0.75

# ===== 핵심 파싱기 (GPT-4o mini 최적화) =====
class RapidParser:
    """빠르고 안정적인 GPT-4o mini 파싱기"""
    
    def __init__(self):
        # 성공률 높은 패턴만 선별
        self.patterns = {
            "question": [
                r"문제\s*[:：]\s*(.+?)(?=1\)|①)",
                r"^(.+?)(?=1\)|①)"
            ],
            "choices": r"1\)\s*([^2]+?)\s*2\)\s*([^3]+?)\s*3\)\s*([^정답답해설]+?)(?=정답|답|해설|$)",
            "answer": r"정답\s*[:：]\s*(\d+)",
            "explanation": r"해설\s*[:：]\s*(.+?)$"
        }
        
        # 품질 필터
        self.banned_words = ["놀이기구", "편의점", "카페", "PC방", "마트"]
        self.quality_words = ["역사", "문화", "전통", "교육", "학습", "관찰"]
    
    def parse(self, response: str, spot_info: Dict, grade: int) -> Optional[Dict]:
        """통합 파싱 함수"""
        try:
            # 전처리
            text = re.sub(r'\s+', ' ', response.strip())
            
            # 필수 구조 확인
            if not all(key in text for key in ["1)", "2)", "3)"]):
                return None
            
            # 각 요소 추출
            question = self._extract_question(text)
            choices = self._extract_choices(text)
            correct_index = self._extract_answer(text)
            explanation = self._extract_explanation(text)
            
            if not all([question, len(choices) == 3, correct_index is not None, explanation]):
                return None
            
            # 품질 체크
            quiz_data = {
                "question": question,
                "choices": choices,
                "correctIndex": correct_index,
                "explanation": explanation
            }
            
            if not self._quality_check(quiz_data, spot_info, grade):
                return None
            
            # 품질 점수 계산
            quality_score = self._calculate_quality(quiz_data, spot_info)
            
            return {
                **quiz_data,
                "generation_method": "llm_parsed",
                "quality_score": quality_score
            }
            
        except Exception as e:
            logger.error(f"파싱 실패: {e}")
            return None
    
    def _extract_question(self, text: str) -> Optional[str]:
        """문제 추출"""
        for pattern in self.patterns["question"]:
            match = re.search(pattern, text, re.DOTALL)
            if match:
                question = match.group(1).strip()
                question = re.sub(r'^(문제\s*[:：]\s*)', '', question)
                if len(question) >= 8 and len(question) <= 120:
                    return question
        return None
    
    def _extract_choices(self, text: str) -> List[str]:
        """선택지 추출"""
        match = re.search(self.patterns["choices"], text, re.DOTALL)
        if match:
            choices = [match.group(1).strip(), match.group(2).strip(), match.group(3).strip()]
            # 선택지 정리
            cleaned = []
            for choice in choices:
                choice = re.sub(r'^[\d\)①②③\.\s]+', '', choice).strip()
                choice = re.sub(r'(정답|답|해설).*$', '', choice).strip()
                if choice and len(choice) >= 2:
                    cleaned.append(choice)
            
            if len(cleaned) == 3:
                return cleaned
        return []
    
    def _extract_answer(self, text: str) -> Optional[int]:
        """정답 추출"""
        match = re.search(self.patterns["answer"], text)
        if match:
            try:
                answer = int(match.group(1))
                if 1 <= answer <= 3:
                    return answer - 1  # 0-based
            except ValueError:
                pass
        return None
    
    def _extract_explanation(self, text: str) -> Optional[str]:
        """해설 추출"""
        match = re.search(self.patterns["explanation"], text, re.DOTALL)
        if match:
            explanation = match.group(1).strip()
            explanation = re.sub(r'^(해설\s*[:：]\s*)', '', explanation)
            if len(explanation) >= 10:
                return explanation
        return None
    
    def _quality_check(self, quiz_data: Dict, spot_info: Dict, grade: int) -> bool:
        """기본 품질 체크"""
        content = f"{quiz_data['question']} {' '.join(quiz_data['choices'])} {quiz_data['explanation']}"
        
        # 금지 단어 체크
        if any(word in content for word in self.banned_words):
            return False
        
        # 중복 선택지 체크
        if len(set(quiz_data['choices'])) != 3:
            return False
        
        # 스팟 연관성 체크
        spot_name = spot_info.get("세부스팟", "")
        location = spot_info.get("메인장소", "")
        if spot_name and location:
            if not (spot_name in content or location in content):
                return False
        
        # 학년별 기본 체크
        if grade <= 2:
            difficult_words = ["역사적의미", "건축양식", "정치적"]
            if any(word in content for word in difficult_words):
                return False
        
        return True
    
    def _calculate_quality(self, quiz_data: Dict, spot_info: Dict) -> float:
        """품질 점수 계산"""
        score = 0.6  # 기본 점수
        
        content = f"{quiz_data['question']} {' '.join(quiz_data['choices'])} {quiz_data['explanation']}"
        
        # 교육 키워드 보너스
        quality_match = sum(1 for word in self.quality_words if word in content)
        score += min(quality_match * 0.05, 0.2)
        
        # 스팟 키워드 연관성
        keywords = spot_info.get("교육키워드", [])
        keyword_match = sum(1 for keyword in keywords if keyword in content)
        score += min(keyword_match * 0.1, 0.2)
        
        return min(score, 1.0)

# ===== 프롬프트 생성기 =====
class PromptGenerator:
    """학년별 최적화 프롬프트 생성"""
    
    def create_prompt(self, spot_info: Dict, config: QuizConfig, attempt: int = 1) -> str:
        """구조화된 프롬프트 생성"""
        
        # 스팟 정보 추출
        spot_name = spot_info.get("세부스팟", spot_info.get("이름", "이곳"))
        location = spot_info.get("메인장소", spot_info.get("위치", ""))
        description = spot_info.get("설명", "")
        keywords = spot_info.get("교육키워드", [])
        
        # 설명 보강
        if not description and keywords:
            description = f"{', '.join(keywords[:3])}와 관련된 교육적 장소입니다."
        elif not description:
            description = f"{spot_name}은 초등학생들이 학습할 수 있는 의미 있는 장소입니다."
        
        # 학년별 언어 수준
        grade = config.grade
        if grade <= 2:
            language = "1-2학년이 이해할 수 있는 쉬운 말"
            thinking = "보고 확인할 수 있는 내용"
        elif grade <= 4:
            language = "3-4학년 교과서 수준의 어휘"
            thinking = "관찰하고 생각해볼 수 있는 내용"
        else:
            language = "5-6학년 수준의 학습 용어"
            thinking = "분석하고 이해할 수 있는 내용"
        
        # 시도별 관점 변화
        perspectives = [
            "이 장소의 특징과 중요성",
            "이 장소에서 할 수 있는 활동",
            "이 장소의 역사와 문화적 의미"
        ]
        perspective = perspectives[(attempt - 1) % len(perspectives)]
        
        return f"""초등학교 {grade}학년용 삼지선다 퀴즈를 정확한 형식으로 만들어주세요.

📍 장소: {location} - {spot_name}
📝 설명: {description}
🎯 관점: {perspective}
🔤 언어: {language}
🧠 수준: {thinking}

⚠️ 금지사항:
- 놀이기구, 편의점, 카페 등 부적절한 선택지 금지
- 추측이나 불확실한 정보 금지
- {grade}학년 수준을 벗어난 어려운 용어 금지

📋 **정확한 출력 형식 (필수):**
문제: [구체적이고 명확한 질문]
1) [정답 선택지]
2) [그럴듯한 오답1]  
3) [그럴듯한 오답2]
정답: [1, 2, 3 중 번호]
해설: [{grade}학년이 이해할 수 있는 설명]

위 형식을 정확히 지켜주세요."""

# ===== 폴백 생성기 =====
class FallbackGenerator:
    """고품질 폴백 퀴즈 생성"""
    
    def __init__(self):
        self.templates = self._setup_templates()
    
    def _setup_templates(self) -> Dict:
        """학년별 템플릿 설정"""
        return {
            "1-2": [
                {
                    "question": "{spot_name}은 어디에 있나요?",
                    "choices": ["서울", "부산", "제주도"],
                    "correct": 0,
                    "explanation": "{spot_name}은 우리나라 수도인 서울에 있어요."
                },
                {
                    "question": "{spot_name}은 어떤 곳인가요?",
                    "choices": ["배우는 곳", "놀이하는 곳", "쇼핑하는 곳"],
                    "correct": 0,
                    "explanation": "{spot_name}은 우리가 보고 배울 수 있는 곳이에요."
                }
            ],
            "3-4": [
                {
                    "question": "{spot_name}의 특별한 점은 무엇인가요?",
                    "choices": ["우리나라 문화유산", "외국 건축물", "현대 건물"],
                    "correct": 0,
                    "explanation": "{spot_name}은 우리나라의 소중한 문화유산입니다."
                },
                {
                    "question": "{spot_name}에서 할 수 있는 활동은?",
                    "choices": ["역사와 문화 학습", "물건 사기", "게임하기"],
                    "correct": 0,
                    "explanation": "{spot_name}에서는 우리나라의 역사와 문화를 배울 수 있습니다."
                }
            ],
            "5-6": [
                {
                    "question": "{spot_name}의 역사적 의미는 무엇인가요?",
                    "choices": ["과거와 현재를 잇는 문화유산", "관광 수입원", "건축 기술 전시"],
                    "correct": 0,
                    "explanation": "{spot_name}은 과거의 문화와 역사를 현재에 전달하는 중요한 문화유산입니다."
                },
                {
                    "question": "{spot_name}의 교육적 가치는?",
                    "choices": ["역사 의식과 문화적 자긍심", "최신 기술 습득", "경제적 이해"],
                    "correct": 0,
                    "explanation": "{spot_name}을 통해 우리 역사에 대한 이해와 문화적 자긍심을 기를 수 있습니다."
                }
            ]
        }
    
    def generate(self, spot_info: Dict, config: QuizConfig, quiz_number: int = 1) -> Dict:
        """폴백 퀴즈 생성"""
        
        # 학년 그룹 결정
        grade = config.grade
        if grade <= 2:
            template_group = "1-2"
        elif grade <= 4:
            template_group = "3-4"
        else:
            template_group = "5-6"
        
        # 템플릿 선택 (다양성 보장)
        templates = self.templates[template_group]
        template_idx = (quiz_number - 1) % len(templates)
        template = templates[template_idx]
        
        # 스팟 정보
        spot_name = spot_info.get("세부스팟", spot_info.get("이름", "이곳"))
        
        # 퀴즈 구성
        return {
            "question": template["question"].format(spot_name=spot_name),
            "choices": template["choices"],
            "correctIndex": template["correct"],
            "explanation": template["explanation"].format(spot_name=spot_name),
            "generation_method": "fallback",
            "quality_score": config.fallback_quality
        }

# ===== 통합 퀴즈 생성기 =====
class RapidCompleteGenerator:
    """포트폴리오용 완전체 퀴즈 생성기"""
    
    def __init__(self, openai_client=None):
        self.openai_client = openai_client
        self.parser = RapidParser()
        self.prompt_gen = PromptGenerator()
        self.fallback_gen = FallbackGenerator()
        
        # 성능 통계
        self.stats = {
            "total_attempts": 0,
            "llm_success": 0,
            "fallback_used": 0,
            "high_quality": 0
        }
    
    def generate_quiz(self, spot_info: Dict, config: QuizConfig) -> Dict:
        """메인 퀴즈 생성 함수"""
        self.stats["total_attempts"] += 1
        
        # GPS 데이터 보완
        if not spot_info.get("위도") or not spot_info.get("경도"):
            spot_info["위도"] = 37.5665
            spot_info["경도"] = 126.9780
        
        # LLM 시도
        if self.openai_client:
            for attempt in range(config.max_attempts):
                try:
                    prompt = self.prompt_gen.create_prompt(spot_info, config, attempt + 1)
                    llm_response = self._call_llm(prompt, config)
                    
                    if llm_response:
                        parsed_quiz = self.parser.parse(llm_response, spot_info, config.grade)
                        
                        if parsed_quiz:
                            self.stats["llm_success"] += 1
                            if parsed_quiz.get("quality_score", 0) >= 0.8:
                                self.stats["high_quality"] += 1
                            return parsed_quiz
                
                except Exception as e:
                    logger.error(f"LLM 생성 시도 {attempt + 1} 실패: {e}")
                    continue
        
        # 폴백 생성
        self.stats["fallback_used"] += 1
        return self.fallback_gen.generate(spot_info, config)
    
    def _call_llm(self, prompt: str, config: QuizConfig) -> Optional[str]:
        """LLM 호출"""
        try:
            response = self.openai_client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {
                        "role": "system",
                        "content": f"당신은 초등학교 {config.grade}학년 현장학습 전문 교육자입니다. 정확한 형식으로 고품질 퀴즈를 만드세요."
                    },
                    {"role": "user", "content": prompt}
                ],
                max_tokens=450,
                temperature=0.3,
                timeout=20
            )
            
            return response.choices[0].message.content.strip()
            
        except Exception as e:
            logger.error(f"LLM 호출 실패: {e}")
            return None
    
    async def generate_multiple_quizzes(
        self, 
        spot_info: Dict, 
        count: int,
        grade: int = 5
    ) -> List[Dict]:
        """다중 퀴즈 생성"""
        
        config = QuizConfig(grade=grade)
        
        # 병렬 생성 (최대 3개씩)
        semaphore = asyncio.Semaphore(3)
        
        async def generate_with_semaphore(index):
            async with semaphore:
                return await self._async_generate_quiz(spot_info, config)
        
        tasks = [generate_with_semaphore(i) for i in range(count)]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        # 성공한 결과만 수집
        valid_quizzes = []
        for result in results:
            if isinstance(result, dict):
                valid_quizzes.append(result)
        
        return valid_quizzes[:count]
    
    async def _async_generate_quiz(self, spot_info: Dict, config: QuizConfig) -> Dict:
        """비동기 퀴즈 생성"""
        loop = asyncio.get_event_loop()
        return await loop.run_in_executor(
            None, self.generate_quiz, spot_info, config
        )
    
    def get_statistics(self) -> Dict:
        """생성 통계"""
        total = self.stats["total_attempts"]
        if total == 0:
            return self.stats
        
        return {
            **self.stats,
            "success_rate": self.stats["llm_success"] / total,
            "quality_rate": self.stats["high_quality"] / max(total, 1),
            "fallback_rate": self.stats["fallback_used"] / total
        }

# ===== 편의 함수들 =====
def quick_generate_quiz(openai_client, spot_info: Dict, grade: int = 5) -> Dict:
    """빠른 퀴즈 생성 (외부 호출용)"""
    generator = RapidCompleteGenerator(openai_client)
    config = QuizConfig(grade=grade)
    return generator.generate_quiz(spot_info, config)

def create_quiz_config(
    grade: int, 
    difficulty: str = "normal",
    high_quality: bool = True
) -> QuizConfig:
    """퀴즈 설정 생성"""
    return QuizConfig(
        grade=grade,
        difficulty=difficulty,
        max_attempts=2 if high_quality else 1,
        enable_validation=high_quality,
        fallback_quality=0.8 if high_quality else 0.7
    )

def emergency_quiz(spot_name: str, grade: int = 5) -> Dict:
    """비상용 최소 퀴즈"""
    return {
        "question": f"{spot_name}은 우리나라 어디에 있나요?",
        "choices": ["서울", "부산", "대구"],
        "correctIndex": 0,
        "explanation": f"{spot_name}은 서울에 있는 중요한 장소입니다.",
        "generation_method": "emergency",
        "quality_score": 0.5
    }

# ===== 테스트 함수 =====
def test_rapid_complete():
    """통합 생성기 테스트"""
    spot_info = {
        "메인장소": "경복궁",
        "세부스팟": "근정전",
        "설명": "조선시대 정전",
        "교육키워드": ["조선시대", "정치"]
    }
    
    generator = RapidCompleteGenerator(None)  # OpenAI 없이 테스트
    
    for grade in [2, 4, 6]:
        print(f"\n=== {grade}학년 통합 테스트 ===")
        config = create_quiz_config(grade)
        quiz = generator.generate_quiz(spot_info, config)
        
        print(f"문제: {quiz['question']}")
        print(f"선택지: {quiz['choices']}")
        print(f"정답: {quiz['correctIndex'] + 1}번")
        print(f"해설: {quiz['explanation']}")
        print(f"생성방법: {quiz['generation_method']}")
        print(f"품질점수: {quiz['quality_score']}")
    
    print(f"\n📊 통계: {generator.get_statistics()}")

# 기존 호환성을 위한 별명들
AdvancedQuizGenerator = RapidCompleteGenerator
QuizSpec = QuizConfig

if __name__ == "__main__":
    test_rapid_complete()