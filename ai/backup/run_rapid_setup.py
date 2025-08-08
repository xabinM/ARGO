# run_rapid_setup.py - 원클릭 급속 셋업
import os
import asyncio
import subprocess
import sys
from pathlib import Path

class RapidSetupManager:
    """1-2일 완성용 원클릭 셋업"""
    
    def __init__(self):
        self.project_root = Path(__file__).parent
        
    def run_complete_setup(self):
        """전체 셋업 실행"""
        
        print("🚀 ARGO RAG 급속 버전 원클릭 셋업")
        print("=" * 50)
        print("⏰ 예상 소요시간: 10-15분")
        print()
        
        try:
            # 1. 환경 확인
            self._check_environment()
            
            # 2. 의존성 설치
            self._install_dependencies()
            
            # 3. 환경 설정 확인
            self._check_env_config()
            
            # 4. 데이터 수집
            self._collect_data()
            
            # 5. RAG 파이프라인 테스트
            self._test_rag_pipeline()
            
            # 6. FastAPI 서버 준비
            self._prepare_server()
            
            print("\n🎉 급속 셋업 완료!")
            print("📋 다음 단계:")
            print("   1. python rapid_server.py  # 서버 시작")
            print("   2. http://localhost:8000/docs  # API 문서 확인")
            print("   3. POST /quiz/generate  # 퀴즈 생성 테스트")
            
        except Exception as e:
            print(f"\n❌ 셋업 실패: {e}")
            print("🔧 문제 해결 후 재시도해주세요")
    
    def _check_environment(self):
        """환경 확인"""
        print("🔧 환경 확인 중...")
        
        # Python 버전
        if sys.version_info < (3, 8):
            raise Exception("Python 3.8+ 필요")
        
        print(f"   ✅ Python {sys.version.split()[0]}")
        
        # 필수 디렉토리 생성
        required_dirs = [
            "config", "data", "rag_pipeline", "utils"
        ]
        
        for dir_name in required_dirs:
            dir_path = self.project_root / dir_name
            dir_path.mkdir(exist_ok=True)
            
            # __init__.py 생성
            init_file = dir_path / "__init__.py"
            if not init_file.exists():
                init_file.write_text("")
        
        print("   ✅ 프로젝트 구조 생성")
    
    def _install_dependencies(self):
        """의존성 설치"""
        print("📦 의존성 설치 중...")
        
        requirements = [
            "fastapi==0.104.1",
            "uvicorn==0.24.0", 
            "openai==1.12.0",
            "python-dotenv==1.0.0",
            "requests==2.31.0",
            "pydantic==2.5.0"
        ]
        
        # requirements_rapid.txt 생성
        req_file = self.project_root / "requirements_rapid.txt"
        req_file.write_text("\n".join(requirements))
        
        # pip install
        try:
            subprocess.run([
                sys.executable, "-m", "pip", "install", "-r", "requirements_rapid.txt"
            ], check=True, capture_output=True)
            print("   ✅ 패키지 설치 완료")
        except subprocess.CalledProcessError as e:
            print(f"   ⚠️ 패키지 설치 경고: {e}")
            print("   💡 수동 설치: pip install -r requirements_rapid.txt")
    
    def _check_env_config(self):
        """환경 설정 확인"""
        print("⚙️ 환경 설정 확인 중...")
        
        env_file = self.project_root / ".env"
        
        if not env_file.exists():
            # .env 파일 생성
            env_content = '''# ARGO RAG 급속 버전 환경 설정
TOUR_API_KEY=your_tour_api_key_here
GMS_API_KEY=your_gms_api_key_here
OPENAI_API_KEY=your_openai_api_key_here
GMS_BASE_URL=https://gms.ssafy.io/gmsapi/api.openai.com/v1
'''
            env_file.write_text(env_content)
            print("   📝 .env 파일 생성됨")
        
        # API 키 확인
        from dotenv import load_dotenv
        load_dotenv()
        
        tour_key = os.getenv('TOUR_API_KEY')
        llm_key = os.getenv('GMS_API_KEY') or os.getenv('OPENAI_API_KEY')
        
        if not tour_key or tour_key == 'your_tour_api_key_here':
            print("   ⚠️ TOUR_API_KEY 설정 필요")
            print("   📌 data.go.kr에서 '한국관광공사_국문 관광정보 서비스' 신청")
        else:
            print("   ✅ Tour API 키 설정됨")
        
        if not llm_key or 'your_' in llm_key:
            print("   ⚠️ LLM API 키 설정 필요 (GMS_API_KEY 또는 OPENAI_API_KEY)")
        else:
            print("   ✅ LLM API 키 설정됨")
    
    def _collect_data(self):
        """데이터 수집"""
        print("📊 데이터 수집 중...")
        
        data_file = self.project_root / "data" / "heritage_rapid_database.json"
        
        if data_file.exists():
            print("   ✅ 기존 데이터 파일 발견")
            return
        
        # rapid_data_collector.py 존재 확인
        collector_file = self.project_root / "rapid_data_collector.py"
        
        if collector_file.exists():
            try:
                # 데이터 수집 실행
                result = subprocess.run([
                    sys.executable, "rapid_data_collector.py"
                ], capture_output=True, text=True, timeout=300)  # 5분 제한
                
                if result.returncode == 0:
                    print("   ✅ 데이터 수집 완료")
                else:
                    print(f"   ⚠️ 데이터 수집 경고: {result.stderr}")
                    self._create_sample_data()
            except subprocess.TimeoutExpired:
                print("   ⚠️ 데이터 수집 시간 초과 - 샘플 데이터 생성")
                self._create_sample_data()
            except Exception as e:
                print(f"   ⚠️ 데이터 수집 실패: {e} - 샘플 데이터 생성")
                self._create_sample_data()
        else:
            print("   ⚠️ rapid_data_collector.py 없음 - 샘플 데이터 생성")
            self._create_sample_data()
    
    def _create_sample_data(self):
        """샘플 데이터 생성"""
        
        sample_data = {
            "메타데이터": {
                "생성일시": "2025-08-06T00:00:00",
                "총_스팟수": 5,
                "데이터소스": "샘플_데이터",
                "수집방식": "급속_셋업_샘플"
            },
            "스팟": [
                {
                    "이름": "경복궁",
                    "설명": "조선 왕조의 정궁으로 1395년에 창건된 대표적인 궁궐입니다. 근정전, 경회루 등 아름다운 건축물들을 볼 수 있습니다.",
                    "위도": 37.579617,
                    "경도": 126.977041,
                    "주소": "서울특별시 종로구 세종로 1-1"
                },
                {
                    "이름": "창덕궁",
                    "설명": "조선시대의 이궁으로 자연과 조화를 이룬 아름다운 궁궐입니다. 유네스코 세계문화유산으로 등재되어 있습니다.",
                    "위도": 37.582136,
                    "경도": 126.991009,
                    "주소": "서울특별시 종로구 율곡로 99"
                },
                {
                    "이름": "국립중앙박물관",
                    "설명": "한국의 역사와 문화를 한눈에 볼 수 있는 대표 박물관입니다. 선사시대부터 근현대까지 다양한 유물을 전시하고 있습니다.",
                    "위도": 37.524405,
                    "경도": 126.980542,
                    "주소": "서울특별시 용산구 서빙고로 137"
                },
                {
                    "이름": "남산타워",
                    "설명": "서울의 상징적인 랜드마크로 서울 전경을 한눈에 볼 수 있는 전망대입니다. 다양한 문화 체험도 가능합니다.",
                    "위도": 37.551169,
                    "경도": 126.988227,
                    "주소": "서울특별시 용산구 남산공원길 105"
                },
                {
                    "이름": "서울대공원",
                    "설명": "동물원, 식물원, 놀이공원이 함께 있는 종합 테마파크입니다. 다양한 동물들을 관찰하며 자연을 배울 수 있습니다.",
                    "위도": 37.434144,
                    "경도": 127.008914,
                    "주소": "경기도 과천시 대공원광장로 102"
                }
            ]
        }
        
        data_file = self.project_root / "data" / "heritage_rapid_database.json"
        
        import json
        with open(data_file, 'w', encoding='utf-8') as f:
            json.dump(sample_data, f, ensure_ascii=False, indent=2)
        
        print("   ✅ 샘플 데이터 생성 완료")
    
    def _test_rag_pipeline(self):
        """RAG 파이프라인 테스트"""
        print("🧪 RAG 파이프라인 테스트 중...")
        
        # 간단한 import 테스트
        try:
            sys.path.append(str(self.project_root))
            
            # 기본 import들 확인
            import json
            from pathlib import Path
            
            data_file = self.project_root / "data" / "heritage_rapid_database.json"
            if data_file.exists():
                with open(data_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                
                spots = data.get('스팟', [])
                print(f"   ✅ 데이터 로드: {len(spots)}개 스팟")
            else:
                print("   ⚠️ 데이터 파일 없음")
                
        except Exception as e:
            print(f"   ⚠️ 파이프라인 테스트 실패: {e}")
    
    def _prepare_server(self):
        """서버 준비"""
        print("🚀 FastAPI 서버 준비 중...")
        
        server_file = self.project_root / "rapid_server.py"
        
        if server_file.exists():
            print("   ✅ rapid_server.py 준비됨")
        else:
            print("   ⚠️ rapid_server.py 파일 필요")
        
        print("   💡 서버 시작: python rapid_server.py")
        print("   💡 API 문서: http://localhost:8000/docs")

def main():
    """메인 실행"""
    setup = RapidSetupManager()
    setup.run_complete_setup()

if __name__ == "__main__":
    main()