# tests/test_fastapi_server.py - 업데이트된 FastAPI 서버 테스트
import asyncio
import aiohttp
import time
import json
from typing import Dict, List

class FastAPIServerTester:
    """FastAPI 서버 테스트"""
    
    def __init__(self, base_url: str = "http://localhost:8000"):
        self.base_url = base_url
        self.session = None
    
    async def __aenter__(self):
        self.session = aiohttp.ClientSession()
        return self
    
    async def __aexit__(self, exc_type, exc_val, exc_tb):
        if self.session:
            await self.session.close()
    
    async def test_server_connection(self) -> bool:
        """서버 연결 테스트"""
        try:
            async with self.session.get(f"{self.base_url}/") as resp:
                if resp.status == 200:
                    data = await resp.json()
                    print(f"✅ 서버 연결 성공: {data.get('service', 'Unknown')}")
                    return True
                else:
                    print(f"❌ 서버 응답 오류: {resp.status}")
                    return False
        except Exception as e:
            print(f"❌ 서버 연결 실패: {e}")
            return False
    
    async def test_health_check(self) -> Dict:
        """서버 상태 확인"""
        try:
            async with self.session.get(f"{self.base_url}/health") as resp:
                data = await resp.json()
                
                print(f"📊 서버 상태:")
                print(f"   상태: {data.get('status', 'unknown')}")
                print(f"   파이프라인 준비: {data.get('pipeline_ready', False)}")
                print(f"   데이터 파일: {data.get('data_file', 'N/A')}")
                print(f"   메시지: {data.get('message', '')}")
                
                return data
        except Exception as e:
            print(f"❌ 상태 확인 실패: {e}")
            return {"pipeline_ready": False, "error": str(e)}
    
    async def test_mission_types(self) -> bool:
        """미션 타입 API 테스트"""
        try:
            async with self.session.get(f"{self.base_url}/mission-types") as resp:
                if resp.status == 200:
                    data = await resp.json()
                    print(f"✅ 지원 미션 타입:")
                    for mission_type in data.get('supported_types', []):
                        print(f"   - {mission_type.get('id', '')}: {mission_type.get('name', '')}")
                    return True
                else:
                    print(f"❌ 미션 타입 조회 실패: {resp.status}")
                    return False
        except Exception as e:
            print(f"❌ 미션 타입 테스트 실패: {e}")
            return False
    
    async def test_mission_generation_api(self, test_cases: List[Dict]) -> List[Dict]:
        """미션 생성 API 테스트"""
        results = []
        
        print(f"🧪 미션 생성 API 테스트 ({len(test_cases)}개 케이스)")
        
        for i, test_case in enumerate(test_cases, 1):
            print(f"\n테스트 {i}: {test_case['location']} - {test_case.get('mission_type', '자동')}")
            
            start_time = time.time()
            
            try:
                async with self.session.post(
                    f"{self.base_url}/generate-mission",
                    json=test_case
                ) as resp:
                    
                    duration = time.time() - start_time
                    
                    if resp.status == 200:
                        data = await resp.json()
                        
                        # 응답 데이터 검증
                        if data.get("success") and "data" in data:
                            mission_data = data["data"]
                            student_mission = mission_data.get("student_mission", {})
                            
                            results.append({
                                "test_case": test_case,
                                "success": True,
                                "response_time": duration,
                                "mission_id": student_mission.get("mission_id", "N/A"),
                                "mission_type": student_mission.get("mission_type", "N/A"),
                                "content_length": len(student_mission.get("mission_content", "")),
                                "quality_score": mission_data.get("metadata", {}).get("performance", {}).get("quality_score", 0)
                            })
                            
                            print(f"   ✅ 성공 ({duration:.2f}초)")
                            print(f"   📱 미션 ID: {student_mission.get('mission_id', 'N/A')}")
                            print(f"   🎯 타입: {student_mission.get('mission_type', 'N/A')}")
                            print(f"   📏 콘텐츠 길이: {len(student_mission.get('mission_content', ''))}자")
                            
                        else:
                            results.append({
                                "test_case": test_case,
                                "success": False,
                                "error": "응답 데이터 구조 오류",
                                "response_data": data
                            })
                            print(f"   ❌ 응답 구조 오류")
                        
                    else:
                        error_data = await resp.json()
                        results.append({
                            "test_case": test_case,
                            "success": False,
                            "error": error_data.get("detail", "Unknown error"),
                            "status_code": resp.status
                        })
                        print(f"   ❌ HTTP 오류 {resp.status}: {error_data.get('detail', 'Unknown')}")
                        
            except Exception as e:
                results.append({
                    "test_case": test_case,
                    "success": False,
                    "error": str(e)
                })
                print(f"   ❌ 예외 발생: {e}")
            
            # API 부하 방지
            await asyncio.sleep(0.5)
        
        return results
    
    async def test_performance_metrics(self) -> bool:
        """성능 메트릭 API 테스트"""
        try:
            async with self.session.get(f"{self.base_url}/performance") as resp:
                if resp.status == 200:
                    data = await resp.json()
                    print(f"✅ 성능 메트릭 조회 성공")
                    
                    # 주요 메트릭 출력
                    if isinstance(data, dict):
                        for key, value in data.items():
                            if isinstance(value, (int, float)):
                                print(f"   {key}: {value}")
                    
                    return True
                else:
                    print(f"❌ 성능 메트릭 조회 실패: {resp.status}")
                    return False
        except Exception as e:
            print(f"❌ 성능 메트릭 테스트 실패: {e}")
            return False

async def test_fastapi_server():
    """FastAPI 서버 종합 테스트"""
    print("🌐 FastAPI 서버 종합 테스트 시작")
    print("="*60)
    
    test_cases = [
        {"location": "경복궁", "grade": 5, "group_size": 4, "duration_minutes": 30, "mission_type": "퀴즈"},
        {"location": "서울대공원", "grade": 4, "group_size": 6, "duration_minutes": 45, "mission_type": "관찰미션"},
        {"location": "국립과천과학관", "grade": 6, "group_size": 3, "duration_minutes": 60, "mission_type": "체험미션"},
        # 자동 미션 타입 결정 테스트
        {"location": "불국사", "grade": 5, "group_size": 4, "duration_minutes": 40}  # mission_type 생략
    ]
    
    async with FastAPIServerTester() as tester:
        try:
            # 1. 서버 연결 확인
            print("1️⃣ 서버 연결 확인")
            connected = await tester.test_server_connection()
            if not connected:
                print("❌ 서버가 실행되지 않음")
                print("💡 해결방법: 다른 터미널에서 'python api_test_server.py' 실행")
                return False
            
            # 2. 서버 상태 확인
            print("\n2️⃣ 서버 상태 확인")
            health_data = await tester.test_health_check()
            if not health_data.get("pipeline_ready", False):
                print("❌ 파이프라인이 준비되지 않음")
                print("💡 서버 로그를 확인하여 초기화 오류를 확인하세요")
                return False
            
            # 3. 미션 타입 API 테스트
            print("\n3️⃣ 미션 타입 API 테스트")
            mission_types_ok = await tester.test_mission_types()
            
            # 4. 미션 생성 API 테스트
            print("\n4️⃣ 미션 생성 API 테스트")
            results = await tester.test_mission_generation_api(test_cases)
            
            # 5. 성능 메트릭 테스트
            print("\n5️⃣ 성능 메트릭 테스트")
            performance_ok = await tester.test_performance_metrics()
            
            # 6. 결과 분석
            print(f"\n📊 테스트 결과 분석")
            success_count = sum(1 for r in results if r.get("success", False))
            total_tests = len(results)
            
            if success_count > 0:
                successful_results = [r for r in results if r.get("success", False)]
                avg_time = sum(r.get("response_time", 0) for r in successful_results) / len(successful_results)
                avg_quality = sum(r.get("quality_score", 0) for r in successful_results) / len(successful_results)
                
                print(f"   성공률: {success_count}/{total_tests} ({success_count/total_tests*100:.1f}%)")
                print(f"   평균 응답시간: {avg_time:.2f}초")
                print(f"   평균 품질점수: {avg_quality:.2f}")
            else:
                print(f"   성공률: 0/{total_tests} (0%)")
            
            # 7. 최종 판정
            all_tests_passed = (
                connected and 
                health_data.get("pipeline_ready", False) and
                mission_types_ok and
                success_count > 0 and
                performance_ok
            )
            
            if all_tests_passed:
                print(f"\n🎉 FastAPI 서버 테스트 성공!")
                print(f"✅ 안드로이드 앱 연동 준비 완료")
                print(f"📋 API 엔드포인트:")
                print(f"   POST /generate-mission - 미션 생성")
                print(f"   GET  /mission-types   - 미션 타입 목록")
                print(f"   GET  /health         - 서버 상태")
                print(f"   GET  /docs           - API 문서")
                return True
            else:
                print(f"\n⚠️ 일부 테스트 실패")
                print(f"📋 실패한 항목들을 확인하여 문제를 해결하세요")
                return False
                
        except Exception as e:
            print(f"❌ 테스트 실행 중 예외 발생: {e}")
            import traceback
            traceback.print_exc()
            return False

if __name__ == "__main__":
    asyncio.run(test_fastapi_server())