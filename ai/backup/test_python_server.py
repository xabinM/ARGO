#!/usr/bin/env python3
# test_python_server.py - Python FastAPI 서버 테스트 스크립트
"""
🧪 Python RAG API 서버 테스트

실행 방법:
1. backend_integrated_fastapi.py 서버 실행
2. python test_python_server.py 실행

테스트 항목:
- 서버 시작 확인
- 헬스체크 API
- 스팟 목록 조회
- 단일 문제 생성
- 다중 문제 생성
- 잘못된 요청 처리
"""

import requests
import json
import time
from datetime import datetime
from typing import Dict, List

class PythonServerTester:
    def __init__(self, base_url: str = "http://localhost:8000"):
        self.base_url = base_url
        self.session = requests.Session()
        self.test_results = []
        
    def log(self, message: str, status: str = "INFO"):
        timestamp = datetime.now().strftime("%H:%M:%S")
        status_emoji = {
            "INFO": "ℹ️",
            "SUCCESS": "✅", 
            "ERROR": "❌",
            "WARNING": "⚠️"
        }
        print(f"[{timestamp}] {status_emoji.get(status, 'ℹ️')} {message}")
        
        self.test_results.append({
            "timestamp": timestamp,
            "status": status,
            "message": message
        })
    
    def test_server_health(self) -> bool:
        """서버 헬스체크 테스트"""
        self.log("=" * 50)
        self.log("🏥 서버 헬스체크 테스트 시작")
        
        try:
            response = self.session.get(f"{self.base_url}/health", timeout=10)
            
            if response.status_code == 200:
                health_data = response.json()
                self.log(f"헬스체크 성공: {health_data.get('status', 'unknown')}")
                self.log(f"LLM 사용 가능: {health_data.get('llm_available', False)}")
                self.log(f"총 스팟 수: {health_data.get('total_spots', 0)}")
                self.log(f"백엔드 호환성: {health_data.get('backend_compatibility', False)}")
                return True
            else:
                self.log(f"헬스체크 실패: HTTP {response.status_code}", "ERROR")
                return False
                
        except requests.exceptions.ConnectionError:
            self.log("서버 연결 실패 - 서버가 실행 중인지 확인하세요", "ERROR")
            return False
        except Exception as e:
            self.log(f"헬스체크 예외: {e}", "ERROR")
            return False
    
    def test_spots_list(self) -> bool:
        """스팟 목록 조회 테스트"""
        self.log("=" * 50) 
        self.log("📍 스팟 목록 조회 테스트 시작")
        
        try:
            response = self.session.get(f"{self.base_url}/spots", timeout=10)
            
            if response.status_code == 200:
                spots_data = response.json()
                total_spots = spots_data.get("total_spots", 0)
                self.log(f"스팟 목록 조회 성공: {total_spots}개 스팟 발견")
                
                if total_spots > 0:
                    # 처음 3개 스팟 정보 출력
                    spots = spots_data.get("spots", [])
                    for i, spot in enumerate(spots[:3]):
                        self.log(f"  {i+1}. {spot.get('spot_name', 'Unknown')} "
                               f"({spot.get('location', 'Unknown')})")
                    
                    if len(spots) > 3:
                        self.log(f"  ... 외 {len(spots) - 3}개 스팟")
                        
                return True
            else:
                self.log(f"스팟 목록 조회 실패: HTTP {response.status_code}", "ERROR")
                return False
                
        except Exception as e:
            self.log(f"스팟 목록 조회 예외: {e}", "ERROR")
            return False
    
    def test_single_problem_generation(self, spot_name: str = "근정전") -> bool:
        """단일 문제 생성 테스트"""
        self.log("=" * 50)
        self.log(f"🎯 단일 문제 생성 테스트 시작: {spot_name}")
        
        try:
            request_data = {
                "spotName": spot_name,
                "problemCnt": 1
            }
            
            start_time = time.time()
            response = self.session.post(
                f"{self.base_url}/generate-problem",
                json=request_data,
                timeout=30
            )
            end_time = time.time()
            
            processing_time = (end_time - start_time) * 1000
            
            if response.status_code == 200:
                result = response.json()
                
                if result.get("success", False):
                    problems = result.get("problems", [])
                    generation_info = result.get("generation_info", {})
                    
                    self.log(f"문제 생성 성공!")
                    self.log(f"  요청 시간: {processing_time:.0f}ms")
                    self.log(f"  생성된 문제 수: {len(problems)}")
                    self.log(f"  LLM 생성: {generation_info.get('llm_generated', 0)}")
                    self.log(f"  폴백 생성: {generation_info.get('fallback_generated', 0)}")
                    
                    if problems:
                        problem = problems[0]
                        self.log(f"  문제: {problem.get('question', 'N/A')[:50]}...")
                        self.log(f"  선택지 수: {len(problem.get('choices', []))}")
                        self.log(f"  정답 인덱스: {problem.get('correctIndex', -1)}")
                        self.log(f"  해설: {problem.get('explanation', 'N/A')[:30]}...")
                        
                        # 문제 유효성 검증
                        if self._validate_problem(problem):
                            self.log("문제 유효성 검증 통과", "SUCCESS")
                            return True
                        else:
                            self.log("문제 유효성 검증 실패", "ERROR")
                            return False
                else:
                    self.log(f"문제 생성 실패: {result}", "ERROR")
                    return False
            else:
                self.log(f"문제 생성 API 실패: HTTP {response.status_code}", "ERROR")
                if response.text:
                    self.log(f"응답: {response.text[:200]}")
                return False
                
        except Exception as e:
            self.log(f"문제 생성 예외: {e}", "ERROR")
            return False
    
    def test_multiple_problems_generation(self, spot_name: str = "경회루", count: int = 3) -> bool:
        """다중 문제 생성 테스트"""
        self.log("=" * 50)
        self.log(f"🎯 다중 문제 생성 테스트 시작: {spot_name} ({count}개)")
        
        try:
            request_data = {
                "spotName": spot_name,
                "problemCnt": count
            }
            
            start_time = time.time()
            response = self.session.post(
                f"{self.base_url}/generate-problem",
                json=request_data,
                timeout=60  # 다중 생성은 시간이 더 걸림
            )
            end_time = time.time()
            
            processing_time = (end_time - start_time) * 1000
            
            if response.status_code == 200:
                result = response.json()
                
                if result.get("success", False):
                    problems = result.get("problems", [])
                    generation_info = result.get("generation_info", {})
                    
                    self.log(f"다중 문제 생성 성공!")
                    self.log(f"  요청 시간: {processing_time:.0f}ms")
                    self.log(f"  요청 문제 수: {count}")
                    self.log(f"  실제 생성 수: {len(problems)}")
                    self.log(f"  LLM 생성: {generation_info.get('llm_generated', 0)}")
                    self.log(f"  폴백 생성: {generation_info.get('fallback_generated', 0)}")
                    
                    # 모든 문제 유효성 검증
                    valid_count = 0
                    for i, problem in enumerate(problems):
                        if self._validate_problem(problem):
                            valid_count += 1
                        else:
                            self.log(f"  문제 {i+1} 유효성 검증 실패", "WARNING")
                    
                    self.log(f"  유효한 문제: {valid_count}/{len(problems)}")
                    
                    if valid_count == len(problems):
                        self.log("모든 문제 유효성 검증 통과", "SUCCESS")
                        return True
                    elif valid_count > 0:
                        self.log("일부 문제 유효성 검증 통과", "WARNING")
                        return True
                    else:
                        self.log("모든 문제 유효성 검증 실패", "ERROR")
                        return False
                else:
                    self.log(f"다중 문제 생성 실패: {result}", "ERROR")
                    return False
            else:
                self.log(f"다중 문제 생성 API 실패: HTTP {response.status_code}", "ERROR")
                return False
                
        except Exception as e:
            self.log(f"다중 문제 생성 예외: {e}", "ERROR")
            return False
    
    def test_invalid_requests(self) -> bool:
        """잘못된 요청 처리 테스트"""
        self.log("=" * 50)
        self.log("🚫 잘못된 요청 처리 테스트 시작")
        
        test_cases = [
            {
                "name": "존재하지 않는 스팟",
                "data": {"spotName": "존재하지않는스팟", "problemCnt": 1},
                "expected_status": 404
            },
            {
                "name": "잘못된 문제 개수 (0개)",
                "data": {"spotName": "근정전", "problemCnt": 0},
                "expected_status": 422
            },
            {
                "name": "너무 많은 문제 개수",
                "data": {"spotName": "근정전", "problemCnt": 20},
                "expected_status": 422
            },
            {
                "name": "누락된 필드",
                "data": {"spotName": "근정전"},
                "expected_status": 422
            }
        ]
        
        success_count = 0
        for test_case in test_cases:
            try:
                response = self.session.post(
                    f"{self.base_url}/generate-problem",
                    json=test_case["data"],
                    timeout=10
                )
                
                if response.status_code == test_case["expected_status"]:
                    self.log(f"  ✅ {test_case['name']}: 예상된 오류 응답 ({response.status_code})")
                    success_count += 1
                else:
                    self.log(f"  ❌ {test_case['name']}: 예상과 다른 응답 ({response.status_code})", "WARNING")
                    
            except Exception as e:
                self.log(f"  ❌ {test_case['name']}: 예외 발생 - {e}", "WARNING")
        
        if success_count == len(test_cases):
            self.log("모든 잘못된 요청 테스트 통과", "SUCCESS")
            return True
        else:
            self.log(f"일부 잘못된 요청 테스트 실패: {success_count}/{len(test_cases)}", "WARNING")
            return success_count > 0
    
    def _validate_problem(self, problem: Dict) -> bool:
        """문제 유효성 검증"""
        required_fields = ["question", "choices", "correctIndex", "explanation"]
        
        # 필수 필드 확인
        for field in required_fields:
            if field not in problem:
                return False
        
        # 문제 텍스트 확인
        if not problem["question"] or len(problem["question"].strip()) < 5:
            return False
        
        # 선택지 확인
        choices = problem["choices"]
        if not isinstance(choices, list) or len(choices) != 3:
            return False
        
        for choice in choices:
            if not choice or len(choice.strip()) < 1:
                return False
        
        # 정답 인덱스 확인
        correct_index = problem["correctIndex"]
        if not isinstance(correct_index, int) or correct_index < 0 or correct_index > 2:
            return False
        
        # 해설 확인
        if not problem["explanation"] or len(problem["explanation"].strip()) < 5:
            return False
        
        return True
    
    def run_all_tests(self) -> Dict:
        """모든 테스트 실행"""
        self.log("🚀 Python RAG API 서버 테스트 시작")
        self.log(f"대상 서버: {self.base_url}")
        
        test_results = {}
        
        # 1. 헬스체크
        test_results["health"] = self.test_server_health()
        if not test_results["health"]:
            self.log("헬스체크 실패로 테스트 중단", "ERROR")
            return test_results
        
        # 2. 스팟 목록
        test_results["spots"] = self.test_spots_list()
        
        # 3. 단일 문제 생성
        test_results["single_problem"] = self.test_single_problem_generation()
        
        # 4. 다중 문제 생성
        test_results["multiple_problems"] = self.test_multiple_problems_generation()
        
        # 5. 잘못된 요청 처리
        test_results["invalid_requests"] = self.test_invalid_requests()
        
        # 결과 요약
        self.log("=" * 50)
        self.log("📊 테스트 결과 요약")
        
        passed = sum(1 for result in test_results.values() if result)
        total = len(test_results)
        
        for test_name, result in test_results.items():
            status = "PASS" if result else "FAIL"
            emoji = "✅" if result else "❌"
            self.log(f"  {emoji} {test_name}: {status}")
        
        self.log(f"전체 결과: {passed}/{total} 테스트 통과")
        
        if passed == total:
            self.log("🎉 모든 테스트 통과! 백엔드 연동 준비 완료", "SUCCESS")
        elif passed > 0:
            self.log("⚠️ 일부 테스트 실패. 문제 확인 후 백엔드 연동 권장", "WARNING")
        else:
            self.log("❌ 모든 테스트 실패. 서버 문제 해결 필요", "ERROR")
        
        return test_results

def main():
    """메인 실행 함수"""
    print("🧪 Python RAG API 서버 테스트 도구")
    print("=" * 50)
    
    # 서버 URL 확인
    server_url = input("서버 URL (기본값: http://localhost:8000): ").strip()
    if not server_url:
        server_url = "http://localhost:8000"
    
    # 테스터 생성 및 실행
    tester = PythonServerTester(server_url)
    results = tester.run_all_tests()
    
    # 상세 로그 저장
    log_filename = f"python_api_test_{datetime.now().strftime('%Y%m%d_%H%M%S')}.log"
    with open(log_filename, 'w', encoding='utf-8') as f:
        for result in tester.test_results:
            f.write(f"[{result['timestamp']}] {result['status']}: {result['message']}\n")
    
    print(f"\n📝 상세 로그가 {log_filename}에 저장되었습니다.")
    
    # 다음 단계 안내
    passed = sum(1 for result in results.values() if result)
    total = len(results)
    
    if passed == total:
        print("\n🎯 다음 단계:")
        print("1. Spring Boot ProblemService에 PythonApiClient 추가")
        print("2. 백엔드 연동 테스트 실행")
        print("3. 통합 테스트 수행")
    else:
        print("\n🔧 해결해야 할 문제:")
        for test_name, result in results.items():
            if not result:
                print(f"- {test_name} 테스트 실패 원인 분석 및 수정")

if __name__ == "__main__":
    main()