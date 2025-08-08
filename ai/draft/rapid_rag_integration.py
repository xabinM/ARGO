# rapid_rag_integration.py - 30분 RAG 통합
import asyncio
import json
import logging
from pathlib import Path

# 기존 파이프라인 import
from rag_pipeline import ARGOPipeline, ARGOAPIHandler

class RapidRAGIntegrator:
    """급속 RAG 통합기"""
    
    def __init__(self):
        self.pipeline = ARGOPipeline()
        self.api_handler = ARGOAPIHandler()
        
        logging.basicConfig(level=logging.INFO)
        self.logger = logging.getLogger(__name__)
    
    async def rapid_integration_test(self, data_file: str = "heritage_rapid_database.json"):
        """급속 통합 테스트 (30분 완성)"""
        
        self.logger.info("⚡ 급속 RAG 통합 테스트 시작")
        
        # 1단계: 데이터 파일 확인
        if not Path(data_file).exists():
            raise FileNotFoundError(f"❌ 데이터 파일 없음: {data_file}")
        
        # 2단계: 파이프라인 초기화
        self.logger.info("🔧 RAG 파이프라인 초기화...")
        await self.pipeline.initialize(data_file)
        
        # 3단계: API 핸들러 초기화
        self.logger.info("🔧 API 핸들러 초기화...")
        await self.api_handler.initialize_api(data_file)
        
        # 4단계: 핵심 기능 테스트
        await self._run_core_tests()
        
        # 5단계: 성능 확인
        await self._performance_check()
        
        self.logger.info("✅ 급속 RAG 통합 완료!")
        return True
    
    async def _run_core_tests(self):
        """핵심 기능 테스트"""
        
        # 테스트 케이스 (서울 집중)
        test_cases = [
            {
                "location": "경복궁",
                "spot_name": "근정전",
                "user_grade": 5,
                "problems_count": 1
            },
            {
                "location": "창덕궁", 
                "spot_name": "인정전",
                "user_grade": 3,
                "problems_count": 1
            },
            {
                "location": "국립중앙박물관",
                "spot_name": "고구려실",
                "user_grade": 4,
                "problems_count": 1
            }
        ]
        
        success_count = 0
        
        for i, request in enumerate(test_cases):
            try:
                self.logger.info(f"🧪 테스트 {i+1}: {request['location']}")
                
                # RAG 파이프라인 테스트
                result = await self.pipeline.generate_quiz(request)
                
                if result and hasattr(result, 'question'):
                    self.logger.info(f"   ✅ 문제: {result.question[:50]}...")
                    self.logger.info(f"   ✅ 선택지: {len(result.choices)}개")
                    success_count += 1
                else:
                    self.logger.warning(f"   ⚠️ 결과 없음")
                
            except Exception as e:
                self.logger.error(f"   ❌ 실패: {e}")
        
        success_rate = success_count / len(test_cases) * 100
        self.logger.info(f"📊 테스트 성공률: {success_rate:.1f}% ({success_count}/{len(test_cases)})")
        
        if success_rate < 60:
            raise Exception("❌ 테스트 성공률 부족 - 데이터 품질 확인 필요")
    
    async def _performance_check(self):
        """성능 확인"""
        
        import time
        
        # 단일 퀴즈 생성 속도 측정
        start_time = time.time()
        
        request = {
            "location": "경복궁",
            "spot_name": "근정전", 
            "user_grade": 5,
            "problems_count": 1
        }
        
        result = await self.pipeline.generate_quiz(request)
        
        end_time = time.time()
        response_time = end_time - start_time
        
        self.logger.info(f"⏱️ 응답시간: {response_time:.2f}초")
        
        if response_time > 10:
            self.logger.warning("⚠️ 응답시간 느림 - 최적화 필요")
        else:
            self.logger.info("✅ 응답시간 양호")
    
    async def generate_demo_quizzes(self):
        """데모용 퀴즈 생성"""
        
        self.logger.info("🎯 데모용 퀴즈 생성 중...")
        
        demo_requests = [
            ("경복궁", "근정전", 5),
            ("창덕궁", "인정전", 4), 
            ("국립중앙박물관", "고구려실", 6),
            ("남산타워", "전망대", 3),
            ("서울대공원", "동물원", 2)
        ]
        
        demo_results = []
        
        for location, spot, grade in demo_requests:
            try:
                request = {
                    "location": location,
                    "spot_name": spot,
                    "user_grade": grade,
                    "problems_count": 1
                }
                
                result = await self.pipeline.generate_quiz(request)
                
                if result:
                    demo_results.append({
                        "location": location,
                        "spot_name": spot,
                        "grade": grade,
                        "question": result.question,
                        "choices": result.choices,
                        "correct_answer": result.choices[result.correct_index],
                        "quality_score": result.quality_score
                    })
                    
            except Exception as e:
                self.logger.error(f"❌ {location} 퀴즈 생성 실패: {e}")
        
        # 데모 결과 저장
        with open("demo_quiz_results.json", "w", encoding="utf-8") as f:
            json.dump(demo_results, f, ensure_ascii=False, indent=2)
        
        self.logger.info(f"📁 데모 퀴즈 저장: demo_quiz_results.json ({len(demo_results)}개)")

async def main():
    """메인 함수"""
    print("⚡ 급속 RAG 통합 시작!")
    print("=" * 40)
    
    integrator = RapidRAGIntegrator()
    
    try:
        # 1. 통합 테스트
        await integrator.rapid_integration_test()
        
        # 2. 데모 퀴즈 생성
        await integrator.generate_demo_quizzes()
        
        print("\n🎉 급속 RAG 통합 성공!")
        print("📋 다음 단계:")
        print("   1. FastAPI 서버 구축")
        print("   2. API 엔드포인트 테스트") 
        print("   3. 포트폴리오 문서화")
        
    except Exception as e:
        print(f"\n❌ 통합 실패: {e}")
        print("🔧 문제 해결 후 재시도 필요")

if __name__ == "__main__":
    asyncio.run(main())