# tests/test_argo_pipeline.py - 통합된 메인 테스트 (경로 문제 해결)
import asyncio
import sys
import os
import json

# 경로 설정
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.append(parent_dir)

def find_test_data():
    """테스트 데이터 파일을 찾아서 절대경로 반환"""
    filename = "heritage_complete_database.json"
    
    # 가능한 경로들
    possible_paths = [
        os.path.join(current_dir, filename),              # tests/heritage_complete_database.json
        os.path.join(parent_dir, filename),               # ai/heritage_complete_database.json
        os.path.join(parent_dir, "data", filename),       # ai/data/heritage_complete_database.json
        os.path.join(os.getcwd(), filename),              # 현재 작업디렉토리/heritage_complete_database.json
        os.path.join(os.getcwd(), "..", filename)         # 상위 디렉토리/heritage_complete_database.json
    ]
    
    for path in possible_paths:
        if os.path.exists(path):
            print(f"✅ 테스트 데이터 발견: {path}")
            
            # 데이터 유효성 간단 체크
            try:
                with open(path, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                spot_count = len(data.get('스팟', []))
                print(f"📊 {spot_count}개 스팟 데이터 확인")
                
                if spot_count > 0:
                    return path
            except Exception as e:
                print(f"⚠️ 데이터 파일 오류: {e}")
                continue
    
    print(f"❌ {filename} 파일을 찾을 수 없습니다")
    print(f"💡 시도한 경로들:")
    for path in possible_paths:
        print(f"   - {path}")
    return None

from rag_pipeline import ARGOPipeline, ARGOAPIHandler

class ARGOPipelineTester:
    """ARGO RAG 파이프라인 통합 테스터"""
    
    def __init__(self):
        self.pipeline = None
        self.api_handler = None
        self.data_file = None
    
    async def setup(self):
        """초기화"""
        # 1. 테스트 데이터 파일 찾기
        self.data_file = find_test_data()
        if not self.data_file:
            raise Exception("테스트 데이터 파일을 찾을 수 없습니다")
        
        print(f"📁 사용할 데이터 파일: {self.data_file}")
        
        # 2. 파이프라인 초기화
        self.pipeline = ARGOPipeline()
        await self.pipeline.initialize(self.data_file)
        
        # 3. API 핸들러 초기화
        self.api_handler = ARGOAPIHandler()
        await self.api_handler.initialize_api(self.data_file)
        
        print("✅ ARGO 시스템 초기화 완료")
    
    async def test_pipeline_basic(self):
        """기본 파이프라인 테스트"""
        test_requests = [
            {"location": "경복궁", "grade": 5, "group_size": 4, "duration_minutes": 30, "mission_type": "퀴즈"},
            {"location": "서울대공원", "grade": 3, "group_size": 6, "duration_minutes": 45, "mission_type": "관찰미션"},
            {"location": "국립과천과학관", "grade": 6, "group_size": 3, "duration_minutes": 60, "mission_type": "체험미션"}
        ]
        
        results = []
        for i, request in enumerate(test_requests, 1):
            print(f"\n🧪 테스트 {i}: {request['location']} - {request['mission_type']}")
            try:
                result = await self.pipeline.generate_argo_mission(request)
                results.append({
                    "test_id": i,
                    "request": request,
                    "success": True,
                    "mission_id": result.student_version['mission_id'],
                    "quality_score": result.teacher_version['mission_overview']['quality_score']
                })
                print(f"   ✅ 성공 - 미션 ID: {result.student_version['mission_id']}")
                print(f"   📊 품질점수: {result.teacher_version['mission_overview']['quality_score']:.2f}")
            except Exception as e:
                results.append({"test_id": i, "request": request, "success": False, "error": str(e)})
                print(f"   ❌ 실패: {e}")
        
        return results
    
    async def test_api_interface(self):
        """API 인터페이스 테스트"""
        test_request = {
            "location": "경복궁",
            "grade": 5,
            "group_size": 4,
            "duration_minutes": 30,
            "mission_type": "퀴즈"
        }
        
        try:
            response = await self.api_handler.handle_mission_request(test_request)
            if response.get('success'):
                print("✅ API 인터페이스 테스트 성공")
                print(f"📱 응답 데이터: {list(response['data'].keys())}")
                return True
            else:
                print(f"❌ API 실패: {response.get('error')}")
                return False
        except Exception as e:
            print(f"❌ API 테스트 예외: {e}")
            return False

async def run_comprehensive_test():
    """종합 테스트 실행"""
    print("🚀 ARGO RAG 파이프라인 종합 테스트")
    print("="*60)
    
    tester = ARGOPipelineTester()
    
    try:
        # 1. 시스템 초기화
        print("📚 시스템 초기화 중...")
        await tester.setup()
        
        # 2. 파이프라인 테스트
        print("\n📊 파이프라인 기능 테스트")
        pipeline_results = await tester.test_pipeline_basic()
        
        # 3. API 인터페이스 테스트  
        print("\n🌐 API 인터페이스 테스트")
        api_success = await tester.test_api_interface()
        
        # 4. 결과 요약
        success_count = sum(1 for r in pipeline_results if r.get('success', False))
        print(f"\n📋 테스트 결과 요약:")
        print(f"   파이프라인: {success_count}/{len(pipeline_results)} 성공")
        print(f"   API 인터페이스: {'성공' if api_success else '실패'}")
        
        # 5. FastAPI 서버 준비 상태 확인
        if success_count > 0 and api_success:
            print(f"\n🎉 테스트 완료! FastAPI 서버 연동 준비됨")
            print(f"💡 다음 단계:")
            print(f"   1. python api_test_server.py (서버 실행)")
            print(f"   2. python tests/test_fastapi_server.py (서버 테스트)")
            return True
        else:
            print(f"\n⚠️ 일부 테스트 실패 - 문제 해결 필요")
            return False
            
    except Exception as e:
        print(f"❌ 테스트 실행 실패: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    asyncio.run(run_comprehensive_test())