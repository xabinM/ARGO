# rag_pipeline/main_pipeline.py - 업데이트된 ARGO 메인 파이프라인
import logging
import asyncio
import time
from typing import Dict
from datetime import datetime

# 업데이트된 모듈들 import
try:
    from .llm_client import LLMClient
    from .generator import SmartMissionGenerator  # 업데이트된 생성기
    from .cache_manager import CacheManager
    from .retriever import ARGORAGRetriever
    from .formatter import ARGOOutputFormatter
    from .models import MissionOutput
except ImportError:
    # 절대 import FALLBACK 
    import sys
    import os
    sys.path.append(os.path.dirname(__file__))
    
    from llm_client import LLMClient
    from generator import SmartMissionGenerator  # 업데이트된 생성기
    from cache_manager import CacheManager
    from retriever import ARGORAGRetriever
    from formatter import ARGOOutputFormatter
    from models import MissionOutput

logger = logging.getLogger(__name__)

class ARGOPipeline:
    """업데이트된 ARGO 프로젝트 메인 RAG 파이프라인"""
    
    def __init__(self):
        # 핵심 컴포넌트들
        self.retriever = ARGORAGRetriever()
        self.formatter = ARGOOutputFormatter()
        self.initialized = False
        
        # 업데이트된 LLM 기반 컴포넌트들
        self.llm_client = None
        self.smart_generator = None  # 업데이트된 생성기
        self.cache_manager = CacheManager()
        
        # 성능 메트릭
        self.performance_metrics = {
            "total_requests": 0,
            "cache_hits": 0,
            "llm_calls": 0,
            "average_response_time": 0.0,
            "response_times": [],
            "auto_type_decisions": 0,  # 자동 미션 타입 결정 횟수
            "average_quality_score": 0.0
        }
    
    async def initialize(self, heritage_data_file: str):
        """파이프라인 초기화 (업데이트된 스마트 생성기 포함)"""
        logger.info("🚀 업데이트된 ARGO RAG 파이프라인 초기화")
        
        try:
            # 1. LLM 클라이언트 초기화
            logger.info("🤖 LLM 클라이언트 초기화 중...")
            self.llm_client = LLMClient()
            self.smart_generator = SmartMissionGenerator()  # 업데이트된 생성기
            logger.info("✅ 업데이트된 스마트 생성기 초기화 완료")
            
            # 2. RAG 검색기 초기화 
            logger.info("🔍 RAG 검색기 초기화 중...")
            success = await self.retriever.initialize(heritage_data_file)
            if not success:
                raise Exception("RAG 검색기 초기화 실패")
            logger.info("✅ RAG 검색기 초기화 완료")
            
            # 3. 캐시 정리
            self.cache_manager.clear_expired_cache()
            
            self.initialized = True
            logger.info("✅ 업데이트된 ARGO 파이프라인 준비 완료")
            
        except Exception as e:
            logger.error(f"파이프라인 초기화 실패: {e}")
            raise Exception(f"파이프라인 초기화 실패: {e}")
    
    async def generate_argo_mission(self, request: Dict) -> MissionOutput:
        """업데이트된 스마트 미션 생성 메인 메서드"""
        if not self.initialized:
            raise Exception("파이프라인이 초기화되지 않았습니다")
        
        start_time = time.time()
        self.performance_metrics["total_requests"] += 1
        
        try:
            mission_id = f"argo_{int(datetime.now().timestamp())}"
            logger.info(f"🎯 업데이트된 스마트 미션 생성 시작: {mission_id}")
            
            # 요청 파라미터 추출
            location = request.get('location', '')
            grade = request.get('grade', 5)
            group_size = request.get('group_size', 4)
            duration_minutes = request.get('duration_minutes', 30)
            mission_type = request.get('mission_type', None)  # None이면 자동 결정
            
            # 자동 결정 메트릭 업데이트
            if not mission_type:
                self.performance_metrics["auto_type_decisions"] += 1
            
            # 1. 캐시 확인 (LLM 호출 최적화)
            cache_key = f"{location}_{mission_type or 'auto'}_{grade}_{group_size}_{duration_minutes}"
            cached_mission = self.cache_manager.get_cached_mission(
                location, mission_type or "auto", grade, group_size, duration_minutes
            )
            
            if cached_mission:
                self.performance_metrics["cache_hits"] += 1
                logger.info(f"✅ 캐시에서 미션 반환: {mission_id}")
                return self._create_mission_output_from_cache(cached_mission, request, mission_id)
            
            # 2. 관련 스팟 검색 (기존 RAG 기능)
            relevant_spots = self.retriever.search_relevant_spots(location, grade, max_results=1)
            
            if not relevant_spots:
                return self._create_fallback_mission(request, mission_id)
            
            # 3. 최적 스팟 선택
            best_spot = relevant_spots[0]['spot']
            
            # 4. 업데이트된 스마트 미션 생성
            logger.info(f"🤖 업데이트된 스마트 생성기로 미션 생성 중...")
            self.performance_metrics["llm_calls"] += 1
            
            mission_data = await self.smart_generator.generate_smart_mission(
                spot_info=best_spot,
                mission_type=mission_type,
                grade=grade,
                group_size=group_size,
                duration=duration_minutes
            )
            
            # 5. 생성 결과 캐싱
            self.cache_manager.cache_mission(
                location, mission_data['mission_type'], grade, group_size, duration_minutes, mission_data
            )
            
            # 6. ARGO 포맷으로 출력 생성
            result = self.formatter.format_for_argo(mission_data, best_spot, request)
            
            # 7. 성능 메트릭 업데이트
            response_time = time.time() - start_time
            self.performance_metrics["response_times"].append(response_time)
            self.performance_metrics["average_response_time"] = sum(self.performance_metrics["response_times"]) / len(self.performance_metrics["response_times"])
            
            # 품질 점수 평균 업데이트
            quality_scores = [mission_data['quality_score']]
            if hasattr(self, '_quality_scores'):
                self._quality_scores.append(mission_data['quality_score'])
            else:
                self._quality_scores = quality_scores
            self.performance_metrics["average_quality_score"] = sum(self._quality_scores) / len(self._quality_scores)
            
            logger.info(f"✅ 업데이트된 스마트 미션 생성 완료: {mission_id} ({response_time:.2f}초)")
            logger.info(f"🎯 미션 타입: {mission_data['mission_type']}, 품질점수: {mission_data['quality_score']:.2f}")
            
            # 자동 결정 정보 로깅
            if mission_data.get('auto_determination_reasoning'):
                logger.info(f"🔍 자동 결정 근거: {mission_data['auto_determination_reasoning']}")
            
            return result
            
        except Exception as e:
            logger.error(f"업데이트된 스마트 미션 생성 실패: {e}")
            return self._create_error_mission(str(e), request.get('location', ''), mission_id)
    
    def get_performance_metrics(self) -> Dict:
        """업데이트된 성능 메트릭 반환"""
        cache_stats = self.cache_manager.get_cache_stats()
        
        return {
            "total_requests": self.performance_metrics["total_requests"],
            "cache_hits": self.performance_metrics["cache_hits"],
            "cache_hit_rate": self.performance_metrics["cache_hits"] / max(1, self.performance_metrics["total_requests"]),
            "llm_calls": self.performance_metrics["llm_calls"],
            "average_response_time": self.performance_metrics["average_response_time"],
            "auto_type_decisions": self.performance_metrics["auto_type_decisions"],  # 새로 추가
            "auto_decision_rate": self.performance_metrics["auto_type_decisions"] / max(1, self.performance_metrics["total_requests"]),  # 새로 추가
            "average_quality_score": self.performance_metrics["average_quality_score"],  # 새로 추가
            "cache_stats": cache_stats,
            "smart_generator_initialized": self.smart_generator is not None,  # 업데이트
            "retriever_initialized": self.retriever is not None and hasattr(self.retriever, 'processed_spots')
        }
    
    async def get_mission_type_analysis(self, location: str, grade: int = 5, group_size: int = 4, duration: int = 30) -> Dict:
        """새로운 기능: 미션 타입 분석 결과 반환"""
        if not self.initialized:
            return {"error": "파이프라인이 초기화되지 않았습니다"}
        
        # 관련 스팟 검색
        relevant_spots = self.retriever.search_relevant_spots(location, grade, max_results=1)
        if not relevant_spots:
            return {"error": "관련 스팟을 찾을 수 없습니다"}
        
        best_spot = relevant_spots[0]['spot']
        
        # 미션 타입 점수 분석
        scores = self.smart_generator.calculate_mission_type_scores(best_spot, grade, group_size, duration)
        
        return {
            "spot_name": best_spot.get('name', ''),
            "location_characteristics": self.smart_generator.analyze_location_characteristics(best_spot),
            "mission_type_scores": [
                {
                    "type": score.mission_type,
                    "score": round(score.score, 3),
                    "reasoning": score.reasoning
                }
                for score in scores
            ],
            "recommended_type": scores[0].mission_type if scores else "퀴즈"
        }
    
    # 기존 메서드들 유지...
    def _create_mission_output_from_cache(self, cached_data: Dict, request: Dict, mission_id: str) -> MissionOutput:
        """캐시된 데이터로 미션 출력 생성"""
        cached_data = cached_data.copy()
        cached_data['mission_id'] = mission_id
        
        spot_info = {
            'name': request.get('location', ''),
            'location': request.get('location', ''),
            'gps': [37.5665, 126.9780],
            'safety_score': 0.8,
            'education_link': '',
            'category': '캐시됨'
        }
        
        return self.formatter.format_for_argo(cached_data, spot_info, request)
    
    def _create_fallback_mission(self, request: Dict, mission_id: str) -> MissionOutput:
        """관련 정보 없을 때 기본 미션"""
        location = request.get('location', '현재 위치')
        
        fallback_mission = {
            'content': f"""🎯 미션 제목: {location} 자유 탐험

👀 둘러보기: 주변을 자세히 관찰해보세요
📝 발견하기: 흥미로운 점 3가지를 찾아보세요  
🤝 나누기: 팀원들과 발견한 것을 공유해보세요

⏰ 탐험 시간: {request.get('duration_minutes', 30)}분

⚠️ 안전수칙:
- 선생님과 함께 활동하세요
- 정해진 구역에서만 활동하세요""",
            'mission_type': '관찰미션',
            'grade': request.get('grade', 5),
            'quality_score': 0.6,
            'safety_included': True,
            'ai_generated': False,
            'fallback_used': True
        }
        
        fallback_spot = {
            'name': location,
            'location': location,
            'gps': [37.5665, 126.9780],
            'safety_score': 0.8,
            'education_link': '',
            'category': '일반'
        }
        
        return self.formatter.format_for_argo(fallback_mission, fallback_spot, request)
    
    def _create_error_mission(self, error_msg: str, location: str, mission_id: str) -> MissionOutput:
        """오류 발생 시 기본 응답"""
        return MissionOutput(
            teacher_version={
                "error": error_msg,
                "mission_id": mission_id,
                "fallback_available": True,
                "suggestion": "LLM API 연결을 확인하거나 다른 장소를 시도해보세요"
            },
            student_version={
                "message": "미션을 준비하고 있어요. 잠시만 기다려주세요! 🎯",
                "location": location,
                "mission_id": mission_id,
                "status": "loading"
            },
            metadata={
                "error": error_msg,
                "mission_id": mission_id,
                "timestamp": datetime.now().isoformat(),
                "status": "error"
            }
        )

# 사용 예시 (업데이트된 테스트)
async def test_upgraded_pipeline():
    """업데이트된 파이프라인 테스트"""
    
    # 1. 파이프라인 초기화
    pipeline = ARGOPipeline()
    await pipeline.initialize("heritage_complete_database.json")
    
    # 2. LLM 연결 테스트
    llm_connected = await pipeline.test_llm_connection()
    print(f"🤖 LLM 연결 상태: {'✅ 성공' if llm_connected else '❌ 실패'}")
    
    # 3. 미션 타입 분석 테스트 (새로운 기능)
    analysis = await pipeline.get_mission_type_analysis("경복궁", grade=5, group_size=4, duration=30)
    print(f"📊 미션 타입 분석: {analysis}")
    
    # 4. 자동 미션 생성 테스트
    test_request = {
        "location": "경복궁",
        "grade": 5,
        "group_size": 4,
        "duration_minutes": 30,
        # mission_type 생략 → 자동 결정
    }
    
    result = await pipeline.generate_argo_mission(test_request)
    
    # 5. 결과 출력
    print("📱 **학생용 출력**:")
    print(f"미션 ID: {result.student_version['mission_id']}")
    print(f"장소: {result.student_version['spot_name']}")
    print(f"타입: {result.student_version['mission_type']}")
    
    print("\n🎯 **교사용 출력**:")
    print(f"품질 점수: {result.teacher_version['mission_overview']['quality_score']:.2f}")
    
    # 6. 업데이트된 성능 메트릭
    metrics = pipeline.get_performance_metrics()
    print(f"\n📊 **업데이트된 성능 메트릭**:")
    print(f"  자동 타입 결정률: {metrics['auto_decision_rate']:.1%}")
    print(f"  평균 품질 점수: {metrics['average_quality_score']:.2f}")
    print(f"  캐시 적중률: {metrics['cache_hit_rate']:.1%}")

if __name__ == "__main__":
    asyncio.run(test_upgraded_pipeline())