# run_ultra_rapid.py - 1-2일 완성 실행 스크립트
"""
🚀 Ultra Rapid RAG 실행기 - 스팟별 퀴즈 (1-2일 완성)

실행 순서:
1. 스팟별 데이터베이스 생성
2. FastAPI 서버 시작  
3. API 테스트
4. 사용 예시 출력
"""

import subprocess
import sys
import os
import time
import requests
import json
from datetime import datetime

def check_requirements():
    """필수 라이브러리 확인"""
    print("🔍 필수 라이브러리 확인 중...")
    
    required_packages = [
        "fastapi", "uvicorn", "pydantic", "requests"
    ]
    
    missing_packages = []
    
    for package in required_packages:
        try:
            __import__(package)
            print(f"   ✅ {package}")
        except ImportError:
            missing_packages.append(package)
            print(f"   ❌ {package}")
    
    if missing_packages:
        print(f"\n📦 누락된 패키지 설치:")
        print(f"pip install {' '.join(missing_packages)}")
        return False
    
    return True

def create_database():
    """스팟별 데이터베이스 생성"""
    print("\n🏗️ 스팟별 데이터베이스 생성 중...")
    
    try:
        result = subprocess.run([sys.executable, "create_spot_database.py"], 
                              capture_output=True, text=True, timeout=60)
        
        if result.returncode == 0:
            print("✅ 데이터베이스 생성 완료")
            return True
        else:
            print(f"❌ 데이터베이스 생성 실패: {result.stderr}")
            return False
            
    except Exception as e:
        print(f"❌ 데이터베이스 생성 오류: {e}")
        return False

def start_server():
    """FastAPI 서버 시작"""
    print("\n🚀 FastAPI 서버 시작 중...")
    print("📍 서버 주소: http://localhost:8000")
    print("📍 API 문서: http://localhost:8000/docs")
    
    try:
        # 백그라운드에서 서버 실행
        process = subprocess.Popen([
            sys.executable, "fastapi_main_server.py"
        ])
        
        # 서버 시작 대기
        print("⏳ 서버 시작 대기 중...")
        for i in range(10):
            try:
                response = requests.get("http://localhost:8000/health", timeout=2)
                if response.status_code == 200:
                    print("✅ 서버 시작 완료!")
                    return process
            except:
                pass
            time.sleep(1)
            print(f"   {i+1}/10 초 대기...")
        
        print("❌ 서버 시작 실패")
        process.terminate()
        return None
        
    except Exception as e:
        print(f"❌ 서버 시작 오류: {e}")
        return None

def test_api():
    """API 테스트"""
    print("\n🧪 API 테스트 실행 중...")
    
    base_url = "http://localhost:8000"
    
    # 1. 서버 상태 확인
    print("1️⃣ 서버 상태 확인...")
    try:
        response = requests.get(f"{base_url}/health")
        if response.status_code == 200:
            data = response.json()
            print(f"   ✅ 상태: {data['status']}")
            print(f"   📊 로드된 스팟: {data['loaded_spots']}개")
            print(f"   🎯 퀴즈 템플릿: {data['quiz_templates']}개")
        else:
            print(f"   ❌ 상태 확인 실패: {response.status_code}")
            return False
    except Exception as e:
        print(f"   ❌ 상태 확인 오류: {e}")
        return False
    
    # 2. 단일 퀴즈 생성 테스트
    print("\n2️⃣ 단일 퀴즈 생성 테스트...")
    try:
        quiz_request = {
            "location": "경복궁",
            "spot_name": "근정전", 
            "user_grade": 5,
            "problems_count": 1
        }
        
        response = requests.post(f"{base_url}/generate-additional-quiz", 
                               json=quiz_request)
        
        if response.status_code == 200:
            data = response.json()
            print(f"   ✅ 퀴즈 생성 성공!")
            print(f"   📝 문제: {data['question']}")
            print(f"   📋 선택지: {data['choices']}")
            print(f"   🎯 정답: {data['choices'][data['correct_index']]}")
            print(f"   💬 해설: {data['explanation']}")
        else:
            print(f"   ❌ 퀴즈 생성 실패: {response.status_code}")
            print(f"   📄 응답: {response.text}")
            return False
            
    except Exception as e:
        print(f"   ❌ 퀴즈 생성 오류: {e}")
        return False
    
    # 3. 배치 생성 테스트
    print("\n3️⃣ 배치 생성 테스트...")
    try:
        batch_request = {
            "location": "경복궁",
            "grades": [3, 5],
            "spots_count": 2,
            "problems_per_spot": 1
        }
        
        response = requests.post(f"{base_url}/batch-generate-by-location",
                               json=batch_request)
        
        if response.status_code == 200:
            data = response.json()
            batch_id = data['batch_id']
            print(f"   ✅ 배치 생성 시작!")
            print(f"   🔢 배치 ID: {batch_id}")
            print(f"   📊 예상 문제 수: {data['total_count']}개")
            
            # 배치 상태 확인
            print("   ⏳ 배치 처리 대기...")
            for i in range(5):
                time.sleep(1)
                status_response = requests.get(f"{base_url}/batch-status/{batch_id}")
                if status_response.status_code == 200:
                    status_data = status_response.json()
                    print(f"      진행률: {status_data['progress']:.1f}% ({status_data['completed']}/{status_data['total']})")
                    if status_data['status'] == 'completed':
                        print(f"   ✅ 배치 처리 완료!")
                        break
        else:
            print(f"   ❌ 배치 생성 실패: {response.status_code}")
            return False
            
    except Exception as e:
        print(f"   ❌ 배치 생성 오류: {e}")
        return False
    
    return True

def show_usage_examples():
    """사용 예시 출력"""
    print("\n" + "="*60)
    print("🎯 Ultra Rapid RAG - 사용 예시")
    print("="*60)
    
    print("\n📍 서버 정보:")
    print("   🌐 서버 주소: http://localhost:8000")
    print("   📚 API 문서: http://localhost:8000/docs")
    print("   ❤️ 헬스 체크: http://localhost:8000/health")
    
    print("\n🔥 주요 API:")
    print("1️⃣ 퀴즈 생성 (메인 API):")
    print("   POST /generate-additional-quiz")
    print("""   {
       "location": "경복궁",
       "spot_name": "경회루", 
       "user_grade": 5
   }""")
    
    print("\n2️⃣ 배치 생성 (기본 퀴즈DB용):")
    print("   POST /batch-generate-by-location")
    print("""   {
       "location": "경복궁",
       "grades": [1,2,3,4,5,6],
       "spots_count": 5,
       "problems_per_spot": 3
   }""")
    
    print("\n3️⃣ 상태 확인:")
    print("   GET /batch-status/{batch_id}")
    print("   GET /health")
    
    print("\n🎯 핵심 개선점:")
    print("   ✅ location이 아닌 spot 중심 퀴즈")
    print("   ✅ 경복궁 경회루 → 경회루 전용 문제")
    print("   ✅ 학년별(1-6) 삼지선다 전용")
    print("   ✅ 백엔드 연동용 깔끔한 4개 API")
    print("   ✅ 1-2일 완성 가능한 구조")
    
    print(f"\n⚡ 현재 지원 스팟:")
    spots_info = [
        "경복궁 > 근정전, 경회루, 향원정, 광화문",
        "창덕궁 > 인정전, 부용지", 
        "서울대공원 > 사슴사, 낙타사",
        "국립중앙박물관 > 선사고대관, 중근세관",
        "기타 > 숭례문, 불국사 다보탑"
    ]
    for spot in spots_info:
        print(f"   📍 {spot}")

def main():
    """메인 실행 함수"""
    print("🚀 Ultra Rapid RAG 실행기 시작")
    print("=" * 50)
    print("⚡ 1-2일 완성용 스팟별 퀴즈 생성 시스템")
    print("🎯 핵심: location이 아닌 spot 기준 정확한 퀴즈")
    
    # 1. 필수 라이브러리 확인
    if not check_requirements():
        print("\n❌ 필수 라이브러리가 부족합니다.")
        return
    
    # 2. 데이터베이스 생성 (없으면)
    if not os.path.exists("heritage_complete_database.json"):
        if not create_database():
            print("\n❌ 데이터베이스 생성에 실패했습니다.")
            return
    else:
        print("\n✅ 기존 데이터베이스 파일 사용")
    
    # 3. 서버 시작
    server_process = start_server()
    if not server_process:
        print("\n❌ 서버 시작에 실패했습니다.")
        return
    
    try:
        # 4. API 테스트
        if test_api():
            print("\n✅ 모든 API 테스트 성공!")
            
            # 5. 사용 예시 출력
            show_usage_examples()
            
            # 6. 서버 유지
            print(f"\n🎉 Ultra Rapid RAG 시스템 준비 완료!")
            print("📋 서버가 실행 중입니다.")
            print("⚠️ 종료하려면 Ctrl+C를 눌러주세요.")
            
            # 서버 프로세스 대기
            server_process.wait()
            
        else:
            print("\n❌ API 테스트에 실패했습니다.")
            server_process.terminate()
    
    except KeyboardInterrupt:
        print(f"\n👋 서버를 종료합니다...")
        server_process.terminate()
        server_process.wait()
        print("✅ 종료 완료")

if __name__ == "__main__":
    main()
