#!/usr/bin/env python3
"""
🧪 ARGO AI Java-Python 완전 연동 테스트
실제 운영 환경과 동일한 흐름으로 테스트
"""

import requests
import time
import json
import io
from PIL import Image, ImageDraw
import threading
import queue
from datetime import datetime

# 서버 설정
PYTHON_SERVER = "http://localhost:8000"
JAVA_SERVER = "http://localhost:8080"

class IntegrationTester:
    def __init__(self):
        self.test_results = []
        self.log_queue = queue.Queue()
        
    def log(self, message, level="INFO"):
        """로그 출력 및 저장"""
        timestamp = datetime.now().strftime("%H:%M:%S.%f")[:-3]
        log_msg = f"[{timestamp}] {level}: {message}"
        print(log_msg)
        self.log_queue.put(log_msg)
        
    def create_realistic_test_image(self, pose_type="armscrossed_pose"):
        """포즈별 현실적인 테스트 이미지 생성"""
        self.log(f"🎨 {pose_type} 포즈 테스트 이미지 생성 중...")
        
        # 더 큰 이미지로 생성 (실제 스마트폰 사진과 유사)
        img = Image.new('RGB', (400, 600), color='lightblue')
        draw = ImageDraw.Draw(img)
        
        # 더 상세한 사람 모양 그리기
        if pose_type == "armscrossed_pose":
            # 머리
            draw.ellipse([175, 50, 225, 100], fill='tan')
            # 몸통
            draw.rectangle([185, 100, 215, 200], fill='red')
            # 팔짱 낀 모습 (X자 형태)
            draw.line([185, 120, 230, 140], fill='tan', width=8)  # 오른팔
            draw.line([215, 120, 170, 140], fill='tan', width=8)  # 왼팔
            # 다리
            draw.line([195, 200, 180, 280], fill='blue', width=10)
            draw.line([205, 200, 220, 280], fill='blue', width=10)
            
        elif pose_type == "sitting_pose":
            # 앉은 자세
            draw.ellipse([175, 50, 225, 100], fill='tan')
            draw.rectangle([185, 100, 215, 180], fill='red')
            # 구부린 다리
            draw.line([195, 180, 185, 220], fill='blue', width=10)
            draw.line([205, 180, 215, 220], fill='blue', width=10)
            draw.line([185, 220, 170, 240], fill='blue', width=10)
            draw.line([215, 220, 230, 240], fill='blue', width=10)
            
        elif pose_type == "handsup_pose":
            # 손들기
            draw.ellipse([175, 50, 225, 100], fill='tan')
            draw.rectangle([185, 100, 215, 200], fill='red')
            # 위로 든 팔
            draw.line([185, 110, 170, 80], fill='tan', width=8)   # 왼팔
            draw.line([215, 110, 230, 80], fill='tan', width=8)   # 오른팔
            # 다리
            draw.line([195, 200, 185, 280], fill='blue', width=10)
            draw.line([205, 200, 215, 280], fill='blue', width=10)
        
        # 배경 추가 (노이즈)
        for i in range(50):
            x, y = draw.textsize(".", font=None)
            draw.point((i*8, i*6), fill='gray')
        
        # 바이트 스트림으로 변환
        img_buffer = io.BytesIO()
        img.save(img_buffer, format='JPEG', quality=85)
        img_buffer.seek(0)
        
        self.log(f"✅ 테스트 이미지 생성 완료: {img.size[0]}x{img.size[1]}")
        return img_buffer

    def test_python_server_direct(self, pose_type="armscrossed_pose"):
        """Python 서버 직접 테스트"""
        self.log("🐍 Python 서버 직접 테스트 시작")
        
        try:
            # 헬스 체크
            health_response = requests.get(f"{PYTHON_SERVER}/pose/health", timeout=10)
            self.log(f"📊 Python 헬스 체크: {health_response.status_code}")
            
            if health_response.status_code == 200:
                health_data = health_response.json()
                self.log(f"✅ Python 서비스 상태: {health_data.get('status')}")
                self.log(f"📈 지원 포즈: {health_data.get('supported_poses', [])}")
            
            # 포즈 분석 테스트
            test_image = self.create_realistic_test_image(pose_type)
            
            files = {'file': (f'test_{pose_type}.jpg', test_image, 'image/jpeg')}
            data = {'pose_select': pose_type}
            
            self.log(f"📡 Python 포즈 분석 요청: {pose_type}")
            start_time = time.time()
            
            response = requests.post(
                f"{PYTHON_SERVER}/pose/predict",
                files=files,
                data=data,
                timeout=60
            )
            
            end_time = time.time()
            processing_time = end_time - start_time
            
            self.log(f"📊 Python 응답: {response.status_code} (처리시간: {processing_time:.2f}초)")
            
            if response.status_code == 200:
                result = response.json()
                self.log(f"✅ Python 성공: {result.get('success')}")
                self.log(f"👥 감지된 사람: {result.get('detected_people')}")
                self.log(f"🎯 포즈 결과: {result.get('pose_result')}")
                self.log(f"📊 신뢰도: {result.get('confidence')}")
                return True, result
            else:
                self.log(f"❌ Python 실패: {response.text}")
                return False, None
                
        except Exception as e:
            self.log(f"❌ Python 서버 테스트 실패: {e}", "ERROR")
            return False, None

    def test_java_server_direct(self, pose_type="armscrossed_pose"):
        """Java 서버 직접 테스트"""
        self.log("☕ Java 서버 직접 테스트 시작")
        
        try:
            # 헬스 체크 (가능하면)
            try:
                health_response = requests.get(f"{JAVA_SERVER}/actuator/health", timeout=5)
                self.log(f"📊 Java 헬스 체크: {health_response.status_code}")
            except:
                self.log("⚠️ Java 헬스 체크 없음 (정상)")
            
            # Java API 테스트
            test_image = self.create_realistic_test_image(pose_type)
            
            files = {'image': (f'test_{pose_type}.jpg', test_image, 'image/jpeg')}
            data = {'pose': pose_type}
            
            self.log(f"📡 Java API 요청: {pose_type}")
            start_time = time.time()
            
            response = requests.post(
                f"{JAVA_SERVER}/api/problem/selfie/determine",
                files=files,
                data=data,
                timeout=90  # Java → Python 연동이므로 더 긴 타임아웃
            )
            
            end_time = time.time()
            processing_time = end_time - start_time
            
            self.log(f"📊 Java 응답: {response.status_code} (총 처리시간: {processing_time:.2f}초)")
            
            if response.status_code == 200:
                result = response.json()
                self.log(f"✅ Java 성공: {result}")
                
                # Java 응답 구조 분석
                if 'result' in result:
                    inner_result = result['result']
                    self.log(f"👥 감지된 사람: {inner_result.get('detected_people')}")
                    self.log(f"🎯 포즈 결과: {inner_result.get('pose_result')}")
                    self.log(f"📊 신뢰도: {inner_result.get('confidence')}")
                
                return True, result
            else:
                self.log(f"❌ Java 실패 ({response.status_code}): {response.text}", "ERROR")
                return False, None
                
        except Exception as e:
            self.log(f"❌ Java 서버 테스트 실패: {e}", "ERROR")
            return False, None

    def test_integration_flow(self, pose_type="armscrossed_pose"):
        """통합 연동 플로우 테스트"""
        self.log("🔄 통합 연동 플로우 테스트 시작")
        self.log("=" * 60)
        
        # 1단계: Python 서버 테스트
        python_success, python_result = self.test_python_server_direct(pose_type)
        
        self.log("-" * 40)
        
        # 2단계: Java 서버 테스트 (Java → Python 연동)
        java_success, java_result = self.test_java_server_direct(pose_type)
        
        self.log("-" * 40)
        
        # 3단계: 결과 비교 및 분석
        self.analyze_integration_results(python_success, python_result, java_success, java_result)
        
        return python_success and java_success

    def analyze_integration_results(self, python_success, python_result, java_success, java_result):
        """연동 결과 분석"""
        self.log("📊 연동 결과 분석")
        
        if python_success and java_success:
            self.log("🎉 완전한 연동 성공!")
            
            # 결과 일치성 확인
            if python_result and java_result:
                py_people = python_result.get('detected_people', 0)
                py_confidence = python_result.get('confidence', 0)
                
                # Java 응답에서 실제 결과 추출
                java_inner = java_result.get('result', {}) if 'result' in java_result else java_result
                java_people = java_inner.get('detected_people', 0)
                java_confidence = java_inner.get('confidence', 0)
                
                self.log(f"📈 결과 일치성 검증:")
                self.log(f"   감지된 사람 수: Python({py_people}) vs Java({java_people})")
                self.log(f"   신뢰도: Python({py_confidence:.3f}) vs Java({java_confidence:.3f})")
                
                if py_people == java_people:
                    self.log("✅ 감지된 사람 수 일치")
                else:
                    self.log("⚠️ 감지된 사람 수 불일치")
                    
        elif python_success and not java_success:
            self.log("⚠️ Python은 성공, Java 연동 실패")
            self.log("🔧 Java → Python 연동 설정 확인 필요")
            
        elif not python_success and java_success:
            self.log("⚠️ Java는 성공, Python 직접 테스트 실패")
            self.log("🔧 Python 서버 상태 확인 필요")
            
        else:
            self.log("❌ 전체 연동 실패")
            self.log("🔧 두 서버 모두 확인 필요")

    def run_comprehensive_test(self):
        """포괄적인 연동 테스트"""
        self.log("🚀 ARGO AI 포괄적인 연동 테스트 시작")
        self.log("=" * 80)
        
        # 테스트할 포즈 목록
        test_poses = [
            "armscrossed_pose",  # 주요 테스트
            "sitting_pose",      # 추가 테스트
            "handsup_pose"       # 추가 테스트
        ]
        
        success_count = 0
        total_tests = len(test_poses)
        
        for i, pose in enumerate(test_poses, 1):
            self.log(f"\n🎯 테스트 {i}/{total_tests}: {pose}")
            self.log("=" * 60)
            
            success = self.test_integration_flow(pose)
            if success:
                success_count += 1
                
            time.sleep(2)  # 서버 부하 방지
        
        # 최종 결과
        self.log("\n" + "=" * 80)
        self.log("📋 최종 테스트 결과")
        self.log(f"✅ 성공: {success_count}/{total_tests}")
        self.log(f"❌ 실패: {total_tests - success_count}/{total_tests}")
        self.log(f"📊 성공률: {(success_count/total_tests)*100:.1f}%")
        
        if success_count == total_tests:
            self.log("🎉 모든 테스트 통과! 연동 완벽 성공!")
        elif success_count > 0:
            self.log("⚠️ 부분적 성공 - 일부 포즈에서 문제 발생")
        else:
            self.log("❌ 전체 실패 - 연동 설정 점검 필요")
        
        return success_count == total_tests

def main():
    """메인 실행 함수"""
    print("🧪 ARGO AI Java-Python 연동 테스트")
    print("현재 시간:", datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    print("=" * 80)
    
    # PIL 설치 확인
    try:
        from PIL import Image, ImageDraw
    except ImportError:
        print("❌ PIL 라이브러리가 설치되지 않았습니다.")
        print("pip install Pillow 실행 후 다시 시도하세요")
        return
    
    # 테스트 실행
    tester = IntegrationTester()
    
    # 빠른 테스트 (단일 포즈)
    print("\n🚀 빠른 연동 테스트 (armscrossed_pose)")
    quick_success = tester.test_integration_flow("armscrossed_pose")
    
    print("\n" + "=" * 80)
    
    # 상세 테스트 여부 확인
    if quick_success:
        print("✅ 빠른 테스트 성공! 추가 포즈로 상세 테스트를 진행하시겠습니까?")
        print("계속하려면 Enter, 종료하려면 Ctrl+C")
        try:
            input()
            tester.run_comprehensive_test()
        except KeyboardInterrupt:
            print("\n테스트 종료")
    else:
        print("❌ 빠른 테스트 실패 - 기본 연동부터 확인하세요")
    
    print("\n🎯 다음 단계:")
    print("   1. 로그에서 구체적인 오류 확인")
    print("   2. Java/Python 서버 설정 점검")
    print("   3. 네트워크 연결 상태 확인")
    print("   4. 실제 모바일 앱에서 최종 테스트")

if __name__ == "__main__":
    main()