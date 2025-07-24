# test_integration.py - 전체 파이프라인 통합 테스트
import asyncio
import aiohttp
import json
import time
import logging
from typing import Dict, List

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class ARGORAGTester:
    """ARGO RAG 파이프라인 통합 테스터"""
    
    def __init__(self, base_url: str = "http://localhost:8000"):
        self.base_url = base_url
        self.session = None
    
    async def __aenter__(self):
        self.session = aiohttp.ClientSession()
        return self
    
    async def __aexit__(self, exc_type, exc_val, exc_tb):
        if self.session:
            await self.session.close()
    
    async def test_server_health(self) -> bool:
        """서버 상태 테스트"""
        try:
            async with self.session.get(f"{self.base_url}/health") as resp:
                data = await resp.json()
                logger.info(f"서버 상태: {data}")
                return data.get("pipeline_ready", False)
        except Exception as e:
            logger.error(f"서버 연결 실패: {e}")
            return False
    
    async def test_mission_generation(self, test_cases: List[Dict]) -> List[Dict]:
        """미션 생성 테스트"""
        results = []
        
        for i, test_case in enumerate(test_cases, 1):
            logger.info(f"테스트 케이스 {i}: {test_case['location']}")
            
            start_time = time.time()
            
            try:
                async with self.session.post(
                    f"{self.base_url}/api/v1/missions/generate",
                    json=test_case
                ) as resp:
                    
                    if resp.status == 200:
                        data = await resp.json()
                        duration = time.time() - start_time
                        
                        results.append({
                            "test_case": test_case,
                            "success": True,
                            "response_time": duration,
                            "mission_id": data.get("mission_id"),
                            "mission_type": data["student_mission"].get("mission_type"),
                            "content_length": len(data["student_mission"].get("mission_content", "")),
                            "quality_score": data["metadata"].get("performance", {}).get("quality_score", 0)
                        })
                        
                        logger.info(f"✅ 성공 - {duration:.2f}초")
                        
                    else:
                        error_data = await resp.json()
                        results.append({
                            "test_case": test_case,
                            "success": False,
                            "error": error_data.get("detail", "Unknown error"),
                            "status_code": resp.status
                        })
                        
                        logger.error(f"❌ 실패 - {resp.status}: {error_data}")
                        
            except Exception as e:
                results.append({
                    "test_case": test_case,
                    "success": False,
                    "error": str(e)
                })
                
                logger.error(f"❌ 예외 발생: {e}")
            
            # API 부하 방지
            await asyncio.sleep(1)
        
        return results
    
    async def test_mission_types(self) -> Dict:
        """지원 미션 타입 테스트"""
        try:
            async with self.session.get(f"{self.base_url}/api/v1/missions/types") as resp:
                data = await resp.json()
                logger.info(f"지원 미션 타입: {data}")
                return data
        except Exception as e:
            logger.error(f"미션 타입 조회 실패: {e}")
            return {}
    
    async def test_batch_generation(self, batch_requests: List[Dict]) -> Dict:
        """배치 미션 생성 테스트"""
        try:
            start_time = time.time()
            
            async with self.session.post(
                f"{self.base_url}/api/v1/missions/batch",
                json=batch_requests
            ) as resp:
                
                data = await resp.json()
                duration = time.time() - start_time
                
                success_count = sum(1 for r in data["results"] if r["success"])
                
                logger.info(f"배치 처리 결과: {success_count}/{len(batch_requests)} 성공, {duration:.2f}초")
                
                return {
                    "total_requests": len(batch_requests),
                    "success_count": success_count,
                    "duration": duration,
                    "results": data["results"]
                }
                
        except Exception as e:
            logger.error(f"배치 처리 실패: {e}")
            return {"error": str(e)}
    
    def generate_test_report(self, results: List[Dict]) -> str:
        """테스트 결과 리포트 생성"""
        total_tests = len(results)
        successful_tests = sum(1 for r in results if r.get("success", False))
        success_rate = successful_tests / total_tests * 100 if total_tests > 0 else 0
        
        successful_results = [r for r in results if r.get("success", False)]
        avg_response_time = sum(r.get("response_time", 0) for r in successful_results) / len(successful_results) if successful_results else 0
        avg_quality = sum(r.get("quality_score", 0) for r in successful_results) / len(successful_results) if successful_results else 0
        
        report = f"""
📊 ARGO RAG 파이프라인 통합 테스트 결과
==========================================

🎯 전체 요약:
- 총 테스트: {total_tests}개
- 성공: {successful_tests}개
- 성공률: {success_rate:.1f}%
- 평균 응답시간: {avg_response_time:.2f}초
- 평균 품질점수: {avg_quality:.2f}

📋 세부 결과:
"""
        
        for i, result in enumerate(results, 1):
            if result.get("success", False):
                report += f"""
✅ 테스트 {i}: {result['test_case']['location']}
   - 미션 타입: {result.get('mission_type', 'N/A')}
   - 응답시간: {result.get('response_time', 0):.2f}초
   - 콘텐츠 길이: {result.get('content_length', 0)}자
   - 품질점수: {result.get('quality_score', 0):.2f}
"""
            else:
                report += f"""
❌ 테스트 {i}: {result['test_case']['location']}
   - 오류: {result.get('error', 'Unknown')}
   - 상태코드: {result.get('status_code', 'N/A')}
"""
        
        # 권장사항
        if success_rate < 80:
            report += "\n⚠️ 권장사항: 성공률이 낮습니다. 파이프라인 설정을 확인해주세요."
        elif avg_response_time > 10:
            report += "\n⚠️ 권장사항: 응답시간이 깁니다. 성능 최적화를 고려해주세요."
        else:
            report += "\n✅ 전체적으로 양호한 성능을 보입니다!"
        
        return report

# 테스트 케이스 정의
def get_test_cases() -> List[Dict]:
    """다양한 테스트 케이스 생성"""
    return [
        {
            "location": "경복궁",
            "grade": 5,
            "group_size": 4,
            "duration_minutes": 30,
            "mission_type": "퀴즈"
        },
        {
            "location": "서울대공원",
            "grade": 3,
            "group_size": 6,
            "duration_minutes": 45,
            "mission_type": "관찰미션"
        },
        {
            "location": "국립중앙박물관",
            "grade": 6,
            "group_size": 3,
            "duration_minutes": 60,
            "mission_type": "체험미션"
        },
        {
            "location": "불국사",
            "grade": 4,
            "group_size": 5,
            "duration_minutes": 40,
            "mission_type": "사진미션"
        },
        # 경계값 테스트
        {
            "location": "제주도",
            "grade": 3,  # 최소 학년
            "group_size": 1,  # 최소 그룹
            "duration_minutes": 15,  # 짧은 시간
            "mission_type": "퀴즈"
        },
        {
            "location": "설악산",
            "grade": 6,  # 최대 학년
            "group_size": 10,  # 최대 그룹
            "duration_minutes": 90,  # 긴 시간
            "mission_type": "체험미션"
        }
    ]

async def run_comprehensive_test():
    """종합 테스트 실행"""
    logger.info("🚀 ARGO RAG 파이프라인 종합 테스트 시작")
    
    async with ARGORAGTester() as tester:
        # 1. 서버 상태 체크
        logger.info("📡 서버 상태 확인 중...")
        server_ready = await tester.test_server_health()
        
        if not server_ready:
            logger.error("❌ 서버가 준비되지 않았습니다. 테스트를 중단합니다.")
            return
        
        # 2. 미션 타입 확인
        logger.info("🎯 지원 미션 타입 확인 중...")
        mission_types = await tester.test_mission_types()
        
        # 3. 개별 미션 생성 테스트
        logger.info("🎲 개별 미션 생성 테스트 중...")
        test_cases = get_test_cases()
        individual_results = await tester.test_mission_generation(test_cases)
        
        # 4. 배치 처리 테스트
        logger.info("📦 배치 처리 테스트 중...")
        batch_cases = test_cases[:3]  # 처음 3개만
        batch_results = await tester.test_batch_generation(batch_cases)
        
        # 5. 결과 리포트 생성
        report = tester.generate_test_report(individual_results)
        
        # 결과 출력
        print(report)
        
        # 결과 파일 저장
        with open("test_results.json", "w", encoding="utf-8") as f:
            json.dump({
                "individual_tests": individual_results,
                "batch_test": batch_results,
                "mission_types": mission_types,
                "summary": {
                    "total_tests": len(individual_results),
                    "success_count": sum(1 for r in individual_results if r.get("success", False)),
                    "avg_response_time": sum(r.get("response_time", 0) for r in individual_results if r.get("success")) / len([r for r in individual_results if r.get("success")]) if any(r.get("success") for r in individual_results) else 0
                }
            }, f, ensure_ascii=False, indent=2)
        
        logger.info("📄 테스트 결과가 test_results.json에 저장되었습니다.")

# 성능 벤치마크 테스트
async def run_performance_benchmark():
    """성능 벤치마크 테스트"""
    logger.info("⚡ 성능 벤치마크 테스트 시작")
    
    async with ARGORAGTester() as tester:
        # 동일한 요청 반복
        benchmark_request = {
            "location": "경복궁",
            "grade": 5,
            "group_size": 4,
            "duration_minutes": 30,
            "mission_type": "퀴즈"
        }
        
        times = []
        
        for i in range(10):
            start_time = time.time()
            
            try:
                async with tester.session.post(
                    f"{tester.base_url}/api/v1/missions/generate",
                    json=benchmark_request
                ) as resp:
                    
                    if resp.status == 200:
                        await resp.json()
                        duration = time.time() - start_time
                        times.append(duration)
                        logger.info(f"요청 {i+1}: {duration:.2f}초")
                    else:
                        logger.error(f"요청 {i+1} 실패: {resp.status}")
                        
            except Exception as e:
                logger.error(f"요청 {i+1} 예외: {e}")
            
            await asyncio.sleep(0.5)
        
        if times:
            avg_time = sum(times) / len(times)
            min_time = min(times)
            max_time = max(times)
            
            logger.info(f"📊 벤치마크 결과:")
            logger.info(f"   평균: {avg_time:.2f}초")
            logger.info(f"   최소: {min_time:.2f}초") 
            logger.info(f"   최대: {max_time:.2f}초")
            logger.info(f"   처리량: {len(times)/sum(times):.1f} 요청/초")

# 메인 실행 함수
async def main():
    """메인 테스트 실행"""
    import sys
    
    if len(sys.argv) > 1 and sys.argv[1] == "benchmark":
        await run_performance_benchmark()
    else:
        await run_comprehensive_test()

if __name__ == "__main__":
    asyncio.run(main())