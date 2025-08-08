# rag_pipeline/generator.py - 급속 버전 (간소화)
import logging
from typing import Dict
from datetime import datetime

from .llm_client import LLMClient
from config.database_schema import QuizProblemOutput

logger = logging.getLogger(__name__)

class RapidQuizGenerator:
    """급속 버전 퀴즈 생성기 - 핵심 기능만"""
    
    def __init__(self):
        self.llm_client = LLMClient()
        self.initialized = False
        
        # 학년별 간단한 프롬프트 템플릿
        self.grade_templates = {
            1: "아주 쉬운 단어로, 그림이나 색깔에 대해",
            2: "쉬운 단어로, 모양이나 크기에 대해", 
            3: "3학년이 이해할 수 있는 단어로",
            4: "4학년 수준의 단어로",
            5: "5학년이 배우는 역사나 과학 내용으로",
            6: "6학년 수준의 좀 더 어려운 내용으로"
        }
    
    def initialize(self):
        """생성기 초기화"""
        self.initialized = True
        logger.info("⚡ 급속 퀴즈 생성기 준비 완료")
    
    async def generate_quiz_for_spot(self, spot_data: Dict, grade: int) -> QuizProblemOutput:
        """스팟 기반 퀴즈 생성 (핵심 메서드)"""
        
        if not self.initialized:
            raise RuntimeError("생성기가 초기화되지 않았습니다")
        
        try:
            spot_name = spot_data.get('이름', '알 수 없는 장소')
            description = spot_data.get('설명', spot_name + ' 관련 내용')
            
            logger.info(f"🎯 퀴즈 생성: {spot_name} (학년: {grade})")
            
            # 프롬프트 생성
            prompt = self._create_quiz_prompt(spot_name, description, grade)
            
            # LLM 호출
            llm_response = await self.llm_client.generate_mission(
                prompt=prompt,
                max_tokens=400,
                temperature=0.7
            )
            
            # 응답 파싱 및 QuizProblemOutput 생성
            quiz_result = self._parse_llm_response(llm_response, spot_data, grade)
            
            logger.info(f"✅ 퀴즈 생성 완료: {spot_name}")
            return quiz_result
            
        except Exception as e:
            logger.error(f"❌ 퀴즈 생성 실패: {e}")
            return self._create_fallback_quiz(spot_data, grade)
    
    def _create_quiz_prompt(self, spot_name: str, description: str, grade: int) -> str:
        """간단한 프롬프트 생성"""
        
        grade_instruction = self.grade_templates.get(grade, "초등학생이 이해할 수 있는 수준으로")
        
        return f"""초등학교 {grade}학년을 위한 현장학습 퀴즈를 만들어주세요.

장소: {spot_name}
설명: {description}

요구사항:
1. {grade_instruction} 문제를 만들어주세요
2. 삼지선다 문제 (선택지 3개)
3. 현장에서 관찰하거나 생각할 수 있는 내용
4. 정답은 명확하게 하나만

형식:
문제: [문제 내용]
1) [선택지 1]
2) [선택지 2] 
3) [선택지 3]
정답: [1, 2, 3 중 하나]
해설: [간단한 설명]"""
    
    def _parse_llm_response(self, response: str, spot_data: Dict, grade: int) -> QuizProblemOutput:
        """LLM 응답 파싱 (간소화)"""
        
        try:
            # 기본값 설정
            question = ""
            choices = ["선택지 1", "선택지 2", "선택지 3"]
            correct_index = 0
            explanation = ""
            
            lines = response.strip().split('\n')
            
            for line in lines:
                line = line.strip()
                
                # 문제 추출
                if line.startswith('문제:'):
                    question = line.replace('문제:', '').strip()
                
                # 선택지 추출
                elif line.startswith('1)'):
                    choices[0] = line.replace('1)', '').strip()
                elif line.startswith('2)'):
                    choices[1] = line.replace('2)', '').strip()
                elif line.startswith('3)'):
                    choices[2] = line.replace('3)', '').strip()
                
                # 정답 추출
                elif line.startswith('정답:'):
                    answer_text = line.replace('정답:', '').strip()
                    try:
                        answer_num = int(answer_text)
                        if 1 <= answer_num <= 3:
                            correct_index = answer_num - 1
                    except:
                        correct_index = 0
                
                # 해설 추출
                elif line.startswith('해설:'):
                    explanation = line.replace('해설:', '').strip()
            
            # 빈 값 처리
            if not question:
                spot_name = spot_data.get('이름', '이 장소')
                question = f"{spot_name}에 대한 설명으로 옳은 것은?"
            
            if not explanation:
                explanation = f"{spot_data.get('이름', '이 장소')}에 대한 기본 정보입니다."
            
            # 품질 점수 간단 계산
            quality_score = self._calculate_simple_quality(question, choices, explanation)
            
            return QuizProblemOutput(
                problem_type="QUIZ",
                question=question,
                choices=choices,
                correct_index=correct_index,
                explanation=explanation,
                spot_name=spot_data.get('이름', ''),
                grade=grade,
                quality_score=quality_score,
                generation_time=1.0,  # 고정값
                created_at=datetime.now()
            )
            
        except Exception as e:
            logger.error(f"❌ 응답 파싱 실패: {e}")
            return self._create_fallback_quiz(spot_data, grade)
    
    def _calculate_simple_quality(self, question: str, choices: list, explanation: str) -> float:
        """간단한 품질 점수 계산"""
        
        score = 0.5  # 기본 점수
        
        # 문제 품질
        if question and len(question) > 10:
            score += 0.1
        if '?' in question or '무엇' in question:
            score += 0.1
        
        # 선택지 품질
        if len(choices) == 3:
            score += 0.1
        if all(len(choice) > 2 for choice in choices):
            score += 0.1
        
        # 해설 품질
        if explanation and len(explanation) > 5:
            score += 0.1
        
        return min(1.0, score)
    
    def _create_fallback_quiz(self, spot_data: Dict, grade: int) -> QuizProblemOutput:
        """실패 시 기본 퀴즈"""
        
        spot_name = spot_data.get('이름', '알 수 없는 장소')
        
        return QuizProblemOutput(
            problem_type="QUIZ",
            question=f"{spot_name}의 특징으로 옳은 것은?",
            choices=["역사적 의미가 있다", "많은 사람들이 방문한다", "교육적 가치가 높다"],
            correct_index=0,
            explanation=f"{spot_name}은 우리나라의 소중한 문화유산입니다.",
            spot_name=spot_name,
            grade=grade,
            quality_score=0.6,
            generation_time=0.1,
            created_at=datetime.now()
        )