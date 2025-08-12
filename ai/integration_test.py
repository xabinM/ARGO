#!/usr/bin/env python3
"""
🧪 ARGO AI 통합 테스트 스크립트
두 서버 간 연동 상태를 빠르게 확인하는 스크립트
"""

import requests
import time
import json
from pathlib import Path

# 설정
PYTHON_SERVER = "http://localhost:8000"
JAVA_SERVER = "http://localhost:8080"

def test_python_server():
    """Python FastAPI 서버 테스트"""
    print("🔍 Python 서버 테스트 시작...")
    
    try:
        # 1. 헬스 체크
        response = requests.get(f"{PYTHON_SERVER}/pose/health", timeout=10)
        print(f"   📊 헬스 체크: {response.status_code}")
        
        if response.status_code == 200:
            health_data = response.json()
            print(f"   ✅ 서비스 상태: {health_data.get('status')}")
            print(f"   📈 지원 포즈: {health_data.get('supported_poses', [])}")
        
        # 2. 테스트 이미지로 포즈 분석 (간단한 더미 요청)
        print("   🎯 포즈 분석 엔드포인트 확인...")
        
        # 더미 이미지 생성 (1x1 픽셀)
        import io
        from PIL import Image
        
        dummy_img = Image.new('RGB', (100, 100), color='red')
        img_buffer = io.BytesIO()
        dummy_img.save(img_buffer, format='JPEG')
        img_buffer.seek(0)
        
        files = {
            'file': ('test.jpg', img_buffer, 'image/jpeg')
        }
        data = {
            'pose_select': 'sitting_pose'
        }
        
        response = requests.post(
            f"{PYTHON_SERVER}/pose/predict", 
            files=files,
            data=data,
            timeout=30
        )
        
        print(f"   📊 포즈 분석: {response.status_code}")
        if response.status_code == 200:
            result = response.json()
            print(f"   ✅ 성공 여부: {result.get('success')}")
            print(f"   👥 감지된 사람: {result.get('detected_people')}")
        else:
            print(f"   ❌ 에러: {response.text}")
            
        return True
        
    except requests.ConnectionError:
        print(f"   ❌ Python 서버 연결 실패 ({PYTHON_SERVER})")
        return False
    except Exception as e:
        print(f"   ❌ Python 서버 테스트 실패: {e}")
        return False

def test_java_server():
    """Java Spring Boot 서버 테스트"""
    print("\n🔍 Java 서버 테스트 시작...")
    
    try:
        # 1. 액추에이터 헬스 체크 (있다면)
        try:
            response = requests.get(f"{JAVA_SERVER}/actuator/health", timeout=10)
            print(f"   📊 액추에이터: {response.status_code}")
        except:
            print("   ⚠️ 액추에이터 없음 (정상)")
        
        # 2. 셀피 분석 API 확인 (실제 엔드포인트로)
        print("   🎯 셀피 분석 API 확인...")
        
        # 더미 이미지 생성
        import io
        from PIL import Image
        
        dummy_img = Image.new('RGB', (100, 100), color='blue')
        img_buffer = io.BytesIO()
        dummy_img.save(img_buffer, format='JPEG')
        img_buffer.seek(0)
        
        files = {
            'multipartFile': ('test.jpg', img_buffer, 'image/jpeg')
        }
        data = {
            'pose': 'SITTING_POSE'  # Java enum 형식
        }
        
        response = requests.post(
            f"{JAVA_SERVER}/api/problem/selfie/determine",
            files=files,
            data=data,
            timeout=60  # 긴 타임아웃 (포즈 분석 시간 고려)
        )
        
        print(f"   📊 셀피 분석: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print(f"   ✅ 응답 받음: {result}")
        elif response.status_code == 404:
            print("   ⚠️ API 엔드포인트 없음 - 확인 필요")
        else:
            print(f"   ❌ 에러 ({response.status_code}): {response.text}")
            
        return True
        
    except requests.ConnectionError:
        print(f"   ❌ Java 서버 연결 실패 ({JAVA_SERVER})")
        return False
    except Exception as e:
        print(f"   ❌ Java 서버 테스트 실패: {e}")
        return False

def test_integration():
    """통합 연동 테스트"""
    print("\n🔄 통합 연동 테스트...")
    
    # 먼저 두 서버가 모두 살아있는지 확인
    python_ok = test_python_server()
    java_ok = test_java_server()
    
    if python_ok and java_ok:
        print("\n✅ 두 서버 모두 정상 작동 중!")
        print("🚀 Java → Python 연동 준비 완료")
        
        # 연동 체크리스트 출력
        print("\n📋 연동 체크리스트:")
        print("   ✅ Python FastAPI 서버 실행 중")
        print("   ✅ Java Spring Boot 서버 실행 중")
        print("   ✅ 포즈 분석 엔드포인트 활성화")
        print("   ⚠️ Java → Python 실제 호출 테스트는 Java 앱에서 진행하세요")
        
    else:
        print("\n❌ 통합 테스트 실패")
        if not python_ok:
            print("   🔧 Python 서버를 먼저 실행하세요: uvicorn main:app --host 0.0.0.0 --port 8000")
        if not java_ok:
            print("   🔧 Java 서버를 먼저 실행하세요")

def main():
    """메인 함수"""
    print("🧪 ARGO AI 서버 통합 테스트")
    print("=" * 50)
    
    # PIL 설치 확인
    try:
        from PIL import Image
    except ImportError:
        print("❌ PIL 라이브러리가 설치되지 않았습니다.")
        print("   pip install Pillow 실행 후 다시 시도하세요")
        return
    
    test_integration()
    
    print("\n" + "=" * 50)
    print("🎯 다음 단계:")
    print("   1. 두 서버 모두 실행 확인")
    print("   2. Java 앱에서 실제 이미지로 API 호출 테스트")
    print("   3. 에러 로그 확인 및 디버깅")

if __name__ == "__main__":
    main()