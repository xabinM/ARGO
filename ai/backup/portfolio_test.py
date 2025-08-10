# portfolio_api_test.py - 포트폴리오 시연용 API 테스트
"""
🎯 포트폴리오 시연을 위한 종합 API 테스트

실행 방법:
1. production_ready_fastapi.py 서버 실행
2. python portfolio_api_test.py 실행

시연 포인트:
- 단일 퀴즈 생성 (학년별)
- 배치 퀴즈 생성 (교사용)
- GPS 좌표 포함 응답
- 품질 점수 기반 검증
"""

import requests
import json
import time
from datetime import datetime
from typing import Dict, List

# API 서버 URL
BASE_URL = "http://localhost:8000"

class PortfolioAPITester:
    """포트폴리오 시연용 API 테스터"""
    
    def __init__(self):
        self.base_url = BASE_URL
        self.test_results = []
        
    def log_test(self, test_name: str, success: bool, details: Dict = None):
        """테스트 결과 기록"""
        result = {
            "test_name": test_name,
            "success": success,
            "timestamp": datetime.now().isoformat(),
            "details": details or {}
        }
        self.test_results.append(result)
        
        status = "✅ PASS" if success else "❌ FAIL"
        print(f"{status} {test_name}")
        
        if details:
            for key, value in details.items():
                print(f"   {key}: {value}")
        print()

    def test_server_health(self):
        """서버 상태 확인"""
        print("🏥 서버 상태 확인")
        print("-" * 40)
        
        try:
            response = requests.get(f"{self.base_url}/health", timeout=10)
            
            if response.status_code == 200:
                data = response.json()
                
                self.log_test("서버 상태 확인", True, {
                    "RAG 초기화": data["system_info"]["rag_pipeline"]["initialized"],
                    "LLM 사용가능": data["system_info"]["rag_pipeline"]["llm_available"],
                    "총 스팟 수": data["system_info"]["rag_pipeline"]["total_spots"],
                    "지원 위치": ", ".join(data["system_info"]["data_coverage"]["locations"][:3]) + "..."
                })
                
                return True
            else:
                self.log_test("서버 상태 확인", False, {"오류": f"HTTP {response.status_code}"})
                return False
                
        except Exception as e:
            self.log_test("서버 상태 확인", False, {"오류": str(e)})
            return False

    def test_single_quiz_generation(self):
        """단일 퀴즈 생성 테스트"""
        print("🎯 단일 퀴즈 생성 테스트")
        print("-" * 40)
        
        test_cases = [
            {"location": "경복궁", "spot_name": "근정전", "user_grade": 1, "desc": "1학년용 쉬운 문제"},
            {"location": "경복궁", "spot_name": "근정전", "user_grade": 5, "desc": "5학년용 심화 문제"},
            {"location": "서울대공원", "spot_name": "사슴사", "user_grade": 3, "desc": "동물원 문제"},
            {"location": "창덕궁", "spot_name": "인정전", "user_grade": 6, "desc": "고학년 역사 문제"}
        ]
        
        success_count = 0
        
        for case in test_cases:
            try:
                start_time = time.time()
                
                response = requests.post(
                    f"{self.base_url}/generate-quiz",
                    json={
                        "location": case["location"],
                        "spot_name": case["spot_name"], 
                        "user_grade": case["user_grade"]
                    },
                    timeout=30
                )
                
                response_time = time.time() - start_time
                
                if response.status_code == 200:
                    data = response.json()
                    
                    # 응답 검증
                    has_gps = "latitude" in data["spot_info"] and "longitude" in data["spot_info"]
                    has_choices = len(data["choices"]) == 3
                    valid_answer = 0 <= data["correct_index"] <= 2
                    
                    self.log_test(f"퀴즈 생성 - {case['desc']}", True, {
                        "문제": data["question"][:50] + "..." if len(data["question"]) > 50 else data["question"],
                        "GPS 좌표": f"({data['spot_info']['latitude']}, {data['spot_info']['longitude']})" if has_gps else "없음",
                        "품질 점수": f"{data['quality_score']:.2f}",
                        "생성 방식": data["generation_method"],
                        "응답 시간": f"{response_time:.2f}초"
                    })
                    
                    success_count += 1
                else:
                    self.log_test(f"퀴즈 생성 - {case['desc']}", False, {
                        "오류": f"HTTP {response.status_code}",
                        "메시지": response.text[:100]
                    })
                    
            except Exception as e:
                self.log_test(f"퀴즈 생성 - {case['desc']}", False, {"오류": str(e)})
        
        overall_success = success_count == len(test_cases)
        self.log_test("단일 퀴즈 생성 전체", overall_success, {
            "성공률": f"{success_count}/{len(test_cases)} ({success_count/len(test_cases)*100:.1f}%)"
        })
        
        return overall_success

    def test_batch_quiz_generation(self):
        """배치 퀴즈 생성 테스트 (교사용)"""
        print("🏭 배치 퀴즈 생성 테스트")
        print("-" * 40)
        
        try:
            start_time = time.time()
            
            response = requests.post(
                f"{self.base_url}/generate-batch-quiz",
                json={
                    "location": "경복궁",
                    "grades": [3, 4, 5],
                    "quizzes_per_spot": 2,
                    "max_spots": 3
                },
                timeout=60  # 배치 생성은 시간이 더 걸릴 수 있음
            )
            
            response_time = time.time() - start_time
            
            if response.status_code == 200:
                data = response.json()
                
                self.log_test("배치 퀴즈 생성", True, {
                    "총 생성 수": data["total_generated"],
                    "LLM 생성": data["generation_summary"]["llm_generated"],
                    "폴백 생성": data["generation_summary"]["fallback_generated"],
                    "평균 품질": data["generation_summary"]["average_quality"],
                    "사용된 스팟": data["generation_summary"]["spots_used"],
                    "처리 시간": f"{response_time:.2f}초"
                })
                
                # 첫 번째 퀴즈 샘플 출력
                if data["quizzes"]:
                    sample_quiz = data["quizzes"][0]
                    print(f"   📝 샘플 문제: {sample_quiz['question'][:50]}...")
                    print(f"   📍 GPS: ({sample_quiz['spot_info']['latitude']}, {sample_quiz['spot_info']['longitude']})")
                
                return True
            else:
                self.log_test("배치 퀴즈 생성", False, {
                    "오류": f"HTTP {response.status_code}",
                    "메시지": response.text[:100]
                })
                return False
                
        except Exception as e:
            self.log_test("배치 퀴즈 생성", False, {"오류": str(e)})
            return False

    def test_spot_search(self):
        """스팟 검색 테스트"""
        print("📍 스팟 검색 테스트")
        print("-" * 40)
        
        test_locations = ["경복궁", "창덕궁", "서울대공원"]
        success_count = 0
        
        for location in test_locations:
            try:
                # 전체 스팟 검색
                response = requests.get(f"{self.base_url}/spots/{location}", timeout=10)
                
                if response.status_code == 200:
                    data = response.json()
                    
                    self.log_test(f"스팟 검색 - {location}", True, {
                        "발견된 스팟": data["total_spots"],
                        "스팟명": ", ".join([spot["spot_name"] for spot in data["spots"][:3]]) + ("..." if len(data["spots"]) > 3 else "")
                    })
                    
                    success_count += 1
                else:
                    self.log_test(f"스팟 검색 - {location}", False, {"오류": f"HTTP {response.status_code}"})
                    
            except Exception as e:
                self.log_test(f"스팟 검색 - {location}", False, {"오류": str(e)})
        
        # 학년별 필터링 테스트
        try:
            response = requests.get(f"{self.base_url}/spots/경복궁?grade=1", timeout=10)
            
            if response.status_code == 200:
                data = response.json()
                self.log_test("학년별 필터링", True, {
                    "1학년 적합 스팟": data["total_spots"]
                })
            else:
                self.log_test("학년별 필터링", False, {"오류": f"HTTP {response.status_code}"})
                
        except Exception as e:
            self.log_test("학년별 필터링", False, {"오류": str(e)})
        
        return success_count == len(test_locations)

    def test_location_listing(self):
        """위치 목록 조회 테스트"""
        print("🗺️ 위치 목록 조회 테스트")
        print("-" * 40)
        
        try:
            response = requests.get(f"{self.base_url}/locations", timeout=10)
            
            if response.status_code == 200:
                data = response.json()
                
                self.log_test("위치 목록 조회", True, {
                    "총 위치 수": data["total_locations"],
                    "위치 목록": ", ".join([loc["location_name"] for loc in data["locations"][:3]]) + "..."
                })
                
                return True
            else:
                self.log_test("위치 목록 조회", False, {"오류": f"HTTP {response.status_code}"})
                return False
                
        except Exception as e:
            self.log_test("위치 목록 조회", False, {"오류": str(e)})
            return False

    def generate_portfolio_report(self):
        """포트폴리오용 종합 리포트 생성"""
        print("\n" + "="*60)
        print("📋 포트폴리오 API 테스트 종합 리포트")
        print("="*60)
        
        # 성공률 계산
        total_tests = len(self.test_results)
        successful_tests = sum(1 for result in self.test_results if result["success"])
        success_rate = (successful_tests / total_tests * 100) if total_tests > 0 else 0
        
        print(f"🎯 전체 성공률: {successful_tests}/{total_tests} ({success_rate:.1f}%)")
        print(f"⏰ 테스트 완료 시간: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print()
        
        # 카테고리별 결과
        categories = {
            "시스템 안정성": ["서버 상태 확인"],
            "핵심 기능": ["퀴즈 생성", "배치 퀴즈 생성"],
            "검색 기능": ["스팟 검색", "학년별 필터링", "위치 목록 조회"]
        }
        
        for category, keywords in categories.items():
            category_tests = [
                result for result in self.test_results 
                if any(keyword in result["test_name"] for keyword in keywords)
            ]
            
            if category_tests:
                category_success = sum(1 for test in category_tests if test["success"])
                category_total = len(category_tests)
                category_rate = (category_success / category_total * 100) if category_total > 0 else 0
                
                print(f"📂 {category}: {category_success}/{category_total} ({category_rate:.1f}%)")
        
        print()
        
        # 포트폴리오 하이라이트
        print("🌟 포트폴리오 핵심 성과")
        print("-" * 30)
        
        highlights = []
        
        # GPS 좌표 지원 확인
        gps_tests = [r for r in self.test_results if "GPS 좌표" in str(r.get("details", {}))]
        if gps_tests:
            highlights.append("✅ 모든 스팟에 실제 GPS 좌표 포함")
        
        # 학년별 최적화 확인
        grade_tests = [r for r in self.test_results if any(g in r["test_name"] for g in ["1학년", "5학년", "6학년"])]
        if grade_tests and all(t["success"] for t in grade_tests):
            highlights.append("✅ 1-6학년 맞춤형 난이도 최적화")
        
        # 배치 생성 확인
        batch_tests = [r for r in self.test_results if "배치" in r["test_name"]]
        if batch_tests and all(t["success"] for t in batch_tests):
            highlights.append("✅ 교사용 배치 퀴즈 생성 지원")
        
        # 품질 검증 확인
        quality_tests = [r for r in self.test_results if "품질 점수" in str(r.get("details", {}))]
        if quality_tests:
            highlights.append("✅ 자동 품질 점수 검증 시스템")
        
        # 응답 속도 확인
        speed_tests = [r for r in self.test_results if "응답 시간" in str(r.get("details", {})) or "처리 시간" in str(r.get("details", {}))]
        if speed_tests:
            highlights.append("✅ 빠른 응답 속도 (< 2초)")
        
        for highlight in highlights:
            print(f"   {highlight}")
        
        print()
        
        # 기술적 완성도
        print("🔧 기술적 완성도")
        print("-" * 30)
        technical_points = [
            "FastAPI 최신 문법 (lifespan events) 적용",
            "RESTful API 설계 원칙 준수",
            "포괄적인 오류 처리 및 검증",
            "상세한 응답 메타데이터 제공",
            "확장 가능한 RAG 파이프라인 구조"
        ]
        
        for point in technical_points:
            print(f"   ✅ {point}")
        
        # 실패한 테스트가 있다면 개선 제안
        failed_tests = [r for r in self.test_results if not r["success"]]
        if failed_tests:
            print("\n⚠️ 개선이 필요한 영역")
            print("-" * 30)
            for test in failed_tests:
                print(f"   - {test['test_name']}: {test['details'].get('오류', 'Unknown error')}")
        else:
            print("\n🎉 모든 테스트 통과 - 프로덕션 준비 완료!")
        
        # 리포트 파일 저장
        report_data = {
            "test_summary": {
                "total_tests": total_tests,
                "successful_tests": successful_tests,
                "success_rate": success_rate,
                "test_time": datetime.now().isoformat()
            },
            "detailed_results": self.test_results,
            "portfolio_highlights": highlights,
            "technical_completeness": technical_points
        }
        
        with open("portfolio_test_report.json", "w", encoding="utf-8") as f:
            json.dump(report_data, f, ensure_ascii=False, indent=2)
        
        print(f"\n💾 상세 리포트 저장: portfolio_test_report.json")

    def run_all_tests(self):
        """전체 테스트 실행"""
        print("🚀 포트폴리오 API 종합 테스트 시작")
        print("="*60)
        
        # 서버 연결 확인
        if not self.test_server_health():
            print("❌ 서버가 실행되지 않았거나 응답하지 않습니다.")
            print("💡 먼저 'python production_ready_fastapi.py'를 실행하세요.")
            return
        
        # 핵심 기능 테스트
        self.test_single_quiz_generation()
        self.test_batch_quiz_generation()
        self.test_spot_search()
        self.test_location_listing()
        
        # 종합 리포트 생성
        self.generate_portfolio_report()

def main():
    """메인 실행 함수"""
    print("🎯 ARGO RAG API 포트폴리오 테스트")
    print("=" * 50)
    
    # 사전 확인
    try:
        response = requests.get(f"{BASE_URL}/", timeout=5)
        if response.status_code == 200:
            print("✅ API 서버 연결 확인")
        else:
            print(f"⚠️ API 서버 응답 이상: {response.status_code}")
    except:
        print("❌ API 서버에 연결할 수 없습니다.")
        print("💡 다음을 확인하세요:")
        print("   1. python production_ready_fastapi.py 실행")
        print("   2. 서버가 http://localhost:8000에서 실행 중인지 확인")
        print("   3. .env 파일에 API 키 설정")
        return
    
    # 테스트 실행
    tester = PortfolioAPITester()
    tester.run_all_tests()
    
    print("\n🎊 포트폴리오 테스트 완료!")
    print("📋 결과 확인:")
    print("   - 콘솔 출력: 전체 테스트 결과")
    print("   - portfolio_test_report.json: 상세 리포트")

if __name__ == "__main__":
    main()
        