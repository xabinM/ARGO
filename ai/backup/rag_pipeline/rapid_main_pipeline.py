# rag_pipeline/main_pipeline.py - 급속 버전 (간소화)
import json
import logging
from typing import Dict, List, Optional
from datetime import datetime

from .retriever import RapidRAGRetriever
from .generator import RapidQuizGenerator  
from .llm_client import LLMClient
from config.database_schema import QuizProblemOutput

logger = logging.getLogger(__name__)

class RapidARGOPipeline:
    """급속 버전 ARGO 파이프라인 (1-2일 완성용)"""
    
    def __init__(self):
        self.retriever = RapidRAGRetriever()
        self.generator = RapidQuizGenerator()
        self.llm_client = LLMClient()
        self.initialized = False
        
        logger.info("🚀 급속 ARGO 파이프라인 초기화")
    
    async def initialize(self, data_file: str):
        """파이프라인 초기화 (간소화)"""
        
        try:
            logger.info(f"⚡ 급속 파이프라인 초기화: {data_file}")
            
            # 1. 데이터 로드
            with open(data_file, 'r', encoding='utf-8') as f:
                self.data = json.load(f)
            
            self.spots = self.data.get('스팟', [])
            logger.info(f"📊 로드된 스팟: {len(self.spots)}개")
            
            # 2. 검색기 초기화 (간단 버전)
            await self.retriever.initialize(self.spots)
            
            # 3. 생성기 초기화
            self.generator.initialize()
            
            self.initialized = True
            logger.info("✅ 급속 파이프라인 준비 완료")
            
        except Exception as e:
            logger.error(f"❌ 초기화 실패: {e}")
            raise
    
    async def generate_quiz(self, request: Dict) -> QuizProblemOutput:
        """퀴즈 생성 (핵심 기능만)"""
        
        if not self.initialized:
            raise RuntimeError("파이프라인이 초기화되지 않았습니다")
        
        try:
            location = request.get('location', '')
            spot_name = request.get('spot_name', '')
            grade = request.get('user_grade', 5)
            
            logger.info(f"🎯 퀴즈 생성: {location} - {spot_name} (학년: {grade})")
            
            # 1. 관련 스팟 검색 (간소화)
            relevant_spots = self.retriever.search_by_location_and_name(
                location, spot_name, max_results=3
            )
            
            if not relevant_spots:
                logger.warning(f"⚠️ 관련 스팟 없음: {location} - {spot_name}")
                return self._create_fallback_quiz(request)
            
            # 2. 최적 스팟 선택
            best_spot = relevant_spots[0]
            
            # 3. 퀴즈 생성
            quiz_result = await self.generator.generate_quiz_for_spot(
                spot_data=best_spot,
                grade=grade
            )
            
            logger.info(f"✅ 퀴즈 생성 완료: 품질 {quiz_result.quality_score:.2f}")
            return quiz_result
            
        except Exception as e:
            logger.error(f"❌ 퀴즈 생성 실패: {e}")
            return self._create_fallback_quiz(request)
    
    async def generate_batch_quizzes(self, location: str, grades: List[int], 
                                   max_spots: int = 5) -> List[QuizProblemOutput]:
        """배치 퀴즈 생성 (간소화)"""
        
        try:
            logger.info(f"🏭 배치 생성: {location} - 학년 {grades}")
            
            # 1. 지역 스팟 검색
            location_spots = self.retriever.search_by_location(location, max_spots)
            
            if not location_spots:
                logger.warning(f"⚠️ {location} 스팟 없음")
                return []
            
            # 2. 학년별 퀴즈 생성
            all_quizzes = []
            
            for spot in location_spots[:max_spots]:
                for grade in grades:
                    try:
                        quiz = await self.generator.generate_quiz_for_spot(
                            spot_data=spot,
                            grade=grade
                        )
                        
                        if quiz and quiz.quality_score > 0.5:
                            all_quizzes.append(quiz)
                            
                    except Exception as e:
                        logger.warning(f"⚠️ 스팟 {spot.get('이름', 'Unknown')} 학년 {grade} 실패: {e}")
                        continue
            
            logger.info(f"✅ 배치 생성 완료: {len(all_quizzes)}개")
            return all_quizzes
            
        except Exception as e:
            logger.error(f"❌ 배치 생성 실패: {e}")
            return []
    
    def _create_fallback_quiz(self, request: Dict) -> QuizProblemOutput:
        """실패 시 기본 퀴즈"""
        
        location = request.get('location', '알 수 없는 장소')
        grade = request.get('user_grade', 5)
        
        return QuizProblemOutput(
            problem_type="QUIZ",
            question=f"{location}에 대한 설명으로 옳은 것은?",
            choices=["역사적 의미가 있다", "문화재로 지정되어 있다", "교육적 가치가 높다"],
            correct_index=0,
            explanation=f"{location}은 우리나라의 소중한 문화유산입니다.",
            grade=grade,
            spot_name=location,
            quality_score=0.6,
            generation_time=0.1,
            created_at=datetime.now()
        )
    
    def get_health_status(self) -> Dict:
        """상태 확인 (간소화)"""
        return {
            "initialized": self.initialized,
            "total_spots": len(self.spots) if self.initialized else 0,
            "status": "ready" if self.initialized else "not_initialized",
            "timestamp": datetime.now().isoformat()
        }