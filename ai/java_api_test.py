#!/usr/bin/env python3
"""
🧪 Java API 직접 테스트 스크립트
Swagger UI 대신 Python으로 직접 Java API 테스트
"""

import requests
import io
from PIL import Image
import base64

def create_test_image():
    """테스트용 이미지 생성"""
    # 100x100 픽셀의 컬러 이미지 생성 (사람 모양 비슷하게)
    img = Image.new('RGB', (200, 300), color='white')
    
    # 간단한 사람 모양 그리기 (머리, 몸통, 팔, 다리)
    from PIL import ImageDraw
    draw = ImageDraw.Draw(img)
    
    # 머리 (원)
    draw.ellipse([75, 20, 125, 70], fill='black')
    
    # 몸통 (사각형)
    draw.rectangle([90, 70, 110, 150], fill='black')
    
    # 팔 (선)
    draw.line([90, 90, 60, 120], fill='black', width=5)  # 왼팔
    draw.line([110, 90, 140, 120], fill='black', width=5)  # 오른팔
    
    # 다리 (선)
    draw.line([95, 150, 80, 200], fill='black', width=5)  # 왼다리
    draw.line([105, 150, 120, 200], fill='black', width=5)  # 오른다리
    
    # 바이트 스트림으로 변환
    img_buffer = io.BytesIO()
    img.save(img_buffer, format='JPEG')
    img_buffer.seek(0)
    
    return img_buffer

def test_java_api():
    """Java API 직접 테스트"""
    print("🧪 Java API 직접 테스트 시작")
    print("=" * 50)
    
    # 테스트용 이미지 생성
    print("🎨 테스트 이미지 생성 중...")
    test_image = create_test_image()
    
    # API 요청
    url = "http://localhost:8080/api/problem/selfie/determine"
    
    # 지원하는 포즈 목록
    pose_types = [
        "SITTING_POSE",
        "EAR_POSE", 
        "HANDSUP_POSE",
        "ARMSCROSSED_POSE",
        "AKIMBO_POSE",
        "HEART_POSE"
    ]
    
    print(f"🎯 테스트할 포즈: {pose_types[3]} (ARMSCROSSED_POSE)")
    
    try:
        # multipart/form-data 요청 (Java Controller와 정확히 매칭)
        files = {
            'image': ('test_person.jpg', test_image, 'image/jpeg')  # 🔥 수정: multipartFile → image
        }
        data = {
            'pose': 'ARMSCROSSED_POSE'  # 이미 맞음
        }
        
        print("📡 Java API 호출 중...")
        response = requests.post(url, files=files, data=data, timeout=60)
        
        print(f"📊 응답 상태: {response.status_code}")
        
        if response.status_code == 200:
            print("✅ 요청 성공!")
            result = response.json()
            print("📋 응답 내용:")
            print(f"   성공 여부: {result.get('success')}")
            print(f"   감지된 사람: {result.get('detected_people')}")
            print(f"   포즈 결과: {result.get('pose_result')}")
            print(f"   신뢰도: {result.get('confidence')}")
            
            if result.get('processing_time'):
                print(f"   처리 시간: {result.get('processing_time')}초")
            
        elif response.status_code == 403:
            print("❌ 403 에러: 접근 권한 없음")
            print("🔧 해결 방법:")
            print("   1. Java 서버가 제대로 실행되고 있는지 확인")
            print("   2. Spring Security 설정 확인")
            print("   3. /api/problem/** 경로 접근 허용 설정")
            
        elif response.status_code == 404:
            print("❌ 404 에러: API 엔드포인트 없음")
            print("🔧 확인사항:")
            print("   1. 올바른 URL: /api/problem/selfie/determine")
            print("   2. Java 컨트롤러가 제대로 등록되었는지 확인")
            
        else:
            print(f"❌ 예상치 못한 에러: {response.status_code}")
            print(f"응답 내용: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ 연결 실패: Java 서버가 실행되지 않음")
        print("🔧 Java 서버를 먼저 실행하세요:")
        print("   ./gradlew bootRun")
        
    except requests.exceptions.Timeout:
        print("❌ 타임아웃: 요청 처리 시간 초과")
        print("🔧 Python 서버와 포즈 분석 모델 상태 확인")
        
    except Exception as e:
        print(f"❌ 예상치 못한 오류: {e}")

def test_connectivity():
    """기본 연결 테스트"""
    print("\n🔍 연결 테스트")
    print("-" * 30)
    
    # Java 서버 기본 연결
    try:
        response = requests.get("http://localhost:8080", timeout=5)
        print(f"✅ Java 서버 연결: {response.status_code}")
    except:
        print("❌ Java 서버 연결 실패")
    
    # Python 서버 연결
    try:
        response = requests.get("http://localhost:8000/health", timeout=5)
        print(f"✅ Python 서버 연결: {response.status_code}")
    except:
        print("❌ Python 서버 연결 실패")

if __name__ == "__main__":
    # PIL 설치 확인
    try:
        from PIL import Image, ImageDraw
    except ImportError:
        print("❌ PIL 라이브러리가 없습니다.")
        print("pip install Pillow 실행 후 다시 시도하세요")
        exit(1)
    
    test_connectivity()
    test_java_api()
    
    print("\n" + "=" * 50)
    print("🎯 다음 단계:")
    print("   1. 에러가 있다면 Java 서버 설정 확인")
    print("   2. 성공했다면 실제 이미지로 추가 테스트")
    print("   3. 모바일 앱에서 최종 테스트")