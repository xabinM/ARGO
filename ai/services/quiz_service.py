# services/quiz_service.py - 간소화된 퀴즈 생성 서비스
"""
🎯 Rapid 퀴즈 생성 서비스 (1-2일 완성용)

주요 기능:
- GPT 기반 고품질 퀴즈 생성
- 학년별 맞춤 최적화  
- 파싱 및 품질 검증
- 간단한 캐싱
"""

import asyncio
import logging
from typing import List, Dict, Optional
import openai
from datetime import datetime

from quiz.rapid_complete_generator import RapidQuizGenerator
from pydantic import BaseModel

logger = logging.getLogger(__name__)

class GeneratedQuizProblem(BaseModel):
    """생성된 퀴즈 문제"""
    question: str
    choices: List[str]
    correctIndex: int
    explanation: str
    generation_method: str = "gpt_rapid"
    quality_score: float = 0.0

class QuizService:
    """간소화된 퀴즈 생성 서비스"""
    
    def __init__(self, settings):
        self.settings = settings
        self.quiz_generator = None
        self.is_ready = False
        self.is_llm_available = False
        
        # 성능 통계
        self.stats = {
            "total_generated": 0,
            "gpt_success": 0,
            "fallback_used": 0,
            "average_quality": 0.0
        }
    
    async def initialize(self):
        """서비스 초기화"""
        logger.info("🎯 Rapid 퀴즈 서비스 초기화 중...")
        
        # 간소화된 생성기 초기화
        self.quiz_generator = RapidQuizGenerator(self.settings)
        await self.quiz_generator.initialize()
        
        self.is_ready = True
        self.is_llm_available = self.quiz_generator.is_llm_ready
        
        logger.info(f"✅ Rapid 퀴즈 서비스 준비 완료 (LLM: {'활성' if self.is_llm_available else '비활성'})")
    
    async def generate_single_quiz(
        self, 
        spot_info: Dict, 
        grade: int = 5, 
        difficulty: str = "normal"
    ) -> GeneratedQuizProblem:
        """단일 퀴즈 생성"""
        
        if not self.is_ready:
            raise RuntimeError("퀴즈 서비스가 준비되지 않았습니다")
        
        # RapidQuizGenerator로 퀴즈 생성
        quiz_data = await self.quiz_generator.generate_quiz(spot_info, grade, difficulty)
        
        if quiz_data:
            # 통계 업데이트
            self.stats["total_generated"] += 1
            if quiz_data.get("generation_method") == "gpt_success":
                self.stats["gpt_success"] += 1
            else:
                self.stats["fallback_used"] += 1
            
            # 품질 점수 누적
            quality = quiz_data.get("quality_score", 0.0)
            self.stats["average_quality"] = (
                (self.stats["average_quality"] * (self.stats["total_generated"] - 1) + quality)
                / self.stats["total_generated"]
            )
            
            return GeneratedQuizProblem(**quiz_data)
        
        # 최후 비상 폴백
        logger.error("❌ 모든 퀴즈 생성 방법 실패")
        return self._create_emergency_fallback(spot_info, grade)
    
    async def generate_multiple_quizzes(
        self,
        spot_info: Dict,
        count: int,
        grade: int = 5,
        difficulty: str = "normal"
    ) -> List[GeneratedQuizProblem]:
        """다중 퀴즈 생성 (병렬 처리)"""
        
        if count <= 0 or count > 10:
            raise ValueError("퀴즈 개수는 1-10개 사이여야 합니다")
        
        # 동시성 제한 (최대 3개씩)
        semaphore = asyncio.Semaphore(3)
        
        async def generate_with_semaphore():
            async with semaphore:
                return await self.generate_single_quiz(spot_info, grade, difficulty)
        
        # 병렬 생성
        tasks = [generate_with_semaphore() for _ in range(count)]
        problems = await asyncio.gather(*tasks)
        
        return problems
    
    def _create_emergency_fallback(self, spot_info: Dict, grade: int) -> GeneratedQuizProblem:
        """비상 폴백 퀴즈"""
        spot_name = spot_info.get("세부스팟", spot_info.get("이름", "이곳"))
        location = spot_info.get("메인장소", spot_info.get("위치", "서울"))
        
        return GeneratedQuizProblem(
            question=f"{spot_name}은 우리나라 어디에 있나요?",
            choices=["서울", "부산", "대구"],
            correctIndex=0,
            explanation=f"{spot_name}은 {location}에 있는 소중한 장소입니다.",
            generation_method="emergency_fallback",
            quality_score=0.5
        )
    
    async def cleanup(self):
        """서비스 정리"""
        logger.info("🔄 퀴즈 서비스 정리 중...")
        
        # 통계 출력
        if self.stats["total_generated"] > 0:
            logger.info(f"📊 퀴즈 생성 통계:")
            logger.info(f"   총 생성: {self.stats['total_generated']}개")
            logger.info(f"   GPT 성공: {self.stats['gpt_success']}개") 
            logger.info(f"   폴백 사용: {self.stats['fallback_used']}개")
            logger.info(f"   평균 품질: {self.stats['average_quality']:.2f}")
        
        if self.quiz_generator:
            await self.quiz_generator.cleanup()
    
    def get_stats(self) -> Dict:
        """서비스 통계 반환"""
        return self.stats.copy()