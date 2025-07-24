# tests/run_all_tests.py - 전체 테스트 실행기 (경로 문제 해결)
import asyncio
import os
import sys
import json

# 경로 설정
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)

async def run_dependency_check():
    """필수 라이브러리 확인"""
    required_libs = ['sentence_transformers', 'faiss', 'fastapi', 'uvicorn', 'aiohttp']
    missing = []
    
    print("📦 의존성 라이브러리 확인:")
    for lib in required_libs:
        try:
            if lib == 'faiss':
                import faiss
            else:
                __import__(lib)
            print(f"   ✅ {lib}")
        except ImportError:
            print(f"   ❌ {lib}")
            missing.append(lib)
    
    if missing:
        print(f"\n💡 설치 명령: pip install {' '.join(missing)}")
        return False
    return True

async def run_data_check():
    """테스트 데이터 확인 및 절대경로 반환"""
    filename = "heritage_complete_database.json"
    
    # 가능한 경로들 (절대경로로 변환)
    possible_paths = [
        os.path.join(current_dir, filename),              # tests/
        os.path.join(parent_dir, filename),               # ai/
        os.path.join(parent_dir, "data", filename),       # ai/data/
        os.path.join(os.getcwd(), filename),              # 현재 작업디렉토리
        os.path.join(os.getcwd(), "..", filename)         # 상위 디렉토리
    ]
    
    print("📁 테스트 데이터 확인:")
    for path in possible_paths:
        abs_path = os.path.abspath(path)
        if os.path.exists(abs_path):
            try:
                with open(abs_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                spot_count = len(data.get('스팟', []))
                
                if spot_count > 0:
                    print(f"   ✅ 발견: {abs_path} ({spot_count}개 스팟)")
                    return abs_path
                else:
                    print(f"   ⚠️ 빈 데이터: {abs_path}")
            except Exception as e:
                print(f"   ❌ 오류: {abs_path} - {e}")
        else:
            print(f"   ❌ 없음: {abs_path}")
    
    print(f"\n❌ 유효한 테스트 데이터 없음")
    print(f"💡 생성 명령:")
    print(f"   cd {parent_dir}")
    print(f"   python test_sample_data.py")
    return None

async def run_environment_check():
    """환경 및 모듈 경로 확인"""
    print("🔧 환경 확인:")
    print(f"   현재 디렉토리: {os.getcwd()}")
    print(f"   테스트 디렉토리: {current_dir}")
    print(f"   상위 디렉토리: {parent_dir}")
    
    # rag_pipeline 모듈 확인
    rag_pipeline_path = os.path.join(parent_dir, "rag_pipeline")
    if os.path.exists(rag_pipeline_path):
        print(f"   ✅ rag_pipeline 폴더 존재: {rag_pipeline_path}")
        
        # 주요 파일들 확인
        key_files = ["main_pipeline.py", "__init__.py", "api_handler.py"]
        for file in key_files:
            file_path = os.path.join(rag_pipeline_path, file)
            if os.path.exists(file_path):
                print(f"   ✅ {file}")
            else:
                print(f"   ❌ {file} 누락")
        
        # sys.path에 추가
        if parent_dir not in sys.path:
            sys.path.insert(0, parent_dir)
            print(f"   ✅ Python 경로에 추가: {parent_dir}")
        
        return True
    else:
        print(f"   ❌ rag_pipeline 폴더 없음: {rag_pipeline_path}")
        return False

async def main():
    """전체 테스트 실행"""
    print("🎯 ARGO RAG 파이프라인 전체 테스트")
    print("="*60)
    
    # 1. 의존성 확인
    if not await run_dependency_check():
        return
    
    # 2. 환경 확인
    if not await run_environment_check():
        return
    
    # 3. 데이터 확인
    data_file = await run_data_check()
    if not data_file:
        return
    
    print(f"\n✅ 사전 확인 완료!")
    print(f"📁 사용할 데이터: {data_file}")
    
    # 4. 파이프라인 테스트
    print(f"\n1️⃣ 파이프라인 기능 테스트")
    try:
        # 현재 파일과 같은 디렉토리의 test_argo_pipeline.py 실행
        from test_argo_pipeline import run_comprehensive_test
        pipeline_success = await run_comprehensive_test()
    except Exception as e:
        print(f"❌ 파이프라인 테스트 실패: {e}")
        import traceback
        traceback.print_exc()
        pipeline_success = False
    
    # 5. FastAPI 서버 테스트 (선택적)
    print(f"\n2️⃣ FastAPI 서버 테스트 (선택적)")
    print(f"💡 서버 테스트를 원한다면:")
    print(f"   터미널 1: cd {parent_dir} && python api_test_server.py")
    print(f"   터미널 2: cd {parent_dir} && python tests/test_fastapi_server.py")
    
    # 6. 최종 결과
    if pipeline_success:
        print(f"\n🎉 핵심 기능 테스트 완료!")
        print(f"📋 다음 단계:")
        print(f"   1. FastAPI 서버 구축 완료")
        print(f"   2. 안드로이드 앱 연결 준비")
        print(f"   3. 실제 공공데이터 API 연동")
        
        print(f"\n🚀 즉시 실행 가능한 명령:")
        print(f"   cd {parent_dir}")
        print(f"   python api_test_server.py")
    else:
        print(f"\n🔧 문제 해결 필요")
        print(f"💡 디버깅 정보:")
        print(f"   - 작업디렉토리: {os.getcwd()}")
        print(f"   - 데이터파일: {data_file}")
        print(f"   - Python경로: {sys.path[:3]}...")

if __name__ == "__main__":
    asyncio.run(main())