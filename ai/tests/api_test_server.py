# api_test_server.py - 업데이트된 FastAPI 서버 (경로 문제 해결 + 최신 구조 반영)
import os
import sys
import json
import asyncio
from contextlib import asynccontextmanager
from typing import Optional
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import uvicorn



# 경로 설정
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir.endswith('tests'):
    # tests 디렉토리에서 실행된 경우
    parent_dir = os.path.dirname(current_dir)
else:
    # ai 루트 디렉토리에서 실행된 경우
    parent_dir = current_dir

sys.path.insert(0, parent_dir)

def find_data_file():
    """데이터 파일 절대경로 찾기"""
    filename = "heritage_complete_database.json"
    
    possible_paths = [
        os.path.join(parent_dir, filename),
        os.path.join(parent_dir, "data", filename),
        os.path.join(current_dir, filename),
        os.path.join(os.getcwd(), filename)
    ]
    
    for path in possible_paths:
        if os.path.exists(path):
            print(f"✅ 데이터 파일 발견: {path}")
            return path
    
    print(f"❌ {filename} 파일을 찾을 수 없습니다")
    print(f"시도한 경로들:")
    for path in possible_paths:
        print(f"  - {path}")
    return None

@asynccontextmanager
async def lifespan(app: FastAPI):
    global api_handler, data_file_path

    print("🚀 ARGO RAG API 서버 시작")
    print(f"📁 현재 디렉토리: {os.getcwd()}")
    print(f"📁 스크립트 디렉토리: {current_dir}")
    print(f"📁 상위 디렉토리: {parent_dir}")

    # 1. 데이터 파일 찾기
    data_file_path = find_data_file()
    if not data_file_path:
        print("⚠️ 데이터 파일을 찾을 수 없습니다. API는 시작되지만 미션 생성은 불가능합니다.")
        print("💡 해결방법: python test_sample_data.py 실행 후 서버 재시작")
        yield
        return

    try:
        # 2. API 핸들러 초기화
        try:
            from rag_pipeline import ARGOAPIHandler
            print("✅ 최신 rag_pipeline 모듈 사용")
        except ImportError:
            try:
                from rag_pipeline import ARGOAPIHandler
                print("✅ rag_pipeline 모듈 사용")
            except ImportError:
                print("❌ RAG 파이프라인 모듈을 찾을 수 없습니다")
                return

        api_handler = ARGOAPIHandler()
        await api_handler.initialize_api(data_file_path)

        print("✅ API 핸들러 초기화 완료")
        print(f"📊 사용 중인 데이터: {data_file_path}")

    except Exception as e:
        print(f"❌ API 핸들러 초기화 실패: {e}")
        import traceback
        traceback.print_exc()
        api_handler = None

    yield

    # 종료 처리
    print("🛑 ARGO RAG API 서버 종료")



# FastAPI 앱 설정
app = FastAPI(
    title="ARGO RAG API",
    description="AR 기반 현장체험학습 플랫폼용 RAG API",
    version="1.1.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

# CORS 설정 (안드로이드 앱 연동용)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 개발용, 프로덕션에서는 특정 도메인만 허용
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 전역 변수
api_handler = None
data_file_path = None

# 요청 모델
class MissionRequest(BaseModel):
    location: str = Field(..., description="현장학습 장소명", example="경복궁")
    grade: int = Field(5, ge=3, le=6, description="학년 (3-6)", example=5)
    group_size: int = Field(4, ge=1, le=10, description="그룹 크기", example=4)
    duration_minutes: int = Field(30, ge=15, le=120, description="활동 시간(분)", example=30)
    mission_type: Optional[str] = Field("퀴즈", description="미션 타입", example="퀴즈")

class HealthResponse(BaseModel):
    status: str
    pipeline_ready: bool
    data_file: Optional[str]
    message: str

# API 엔드포인트들
@app.get("/", tags=["기본"])
async def root():
    """API 루트 엔드포인트"""
    return {
        "service": "ARGO RAG API",
        "version": "1.1.0",
        "description": "AR 기반 현장체험학습 플랫폼용 RAG API",
        "docs": "/docs",
        "health": "/health"
    }

@app.get("/health", response_model=HealthResponse, tags=["상태"])
async def health_check():
    """서버 상태 확인"""
    global api_handler, data_file_path
    
    pipeline_ready = api_handler is not None
    
    return HealthResponse(
        status="healthy" if pipeline_ready else "initializing",
        pipeline_ready=pipeline_ready,
        data_file=data_file_path,
        message="서비스 정상 작동 중" if pipeline_ready else "파이프라인 초기화 필요"
    )

@app.post("/generate-mission", tags=["미션"])
async def generate_mission(request: MissionRequest):
    """미션 생성 API"""
    global api_handler
    
    if api_handler is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="파이프라인이 초기화되지 않았습니다. /health 엔드포인트를 확인하세요."
        )
    
    try:
        # 요청 데이터 검증
        request_data = request.dict()
        
        # API 핸들러로 요청 처리
        result = await api_handler.handle_mission_request(request_data)
        
        if not result.get("success"):
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=result.get("error", "미션 생성 중 오류가 발생했습니다")
            )
        
        return result
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"미션 생성 실패: {str(e)}"
        )

@app.get("/mission-types", tags=["미션"])
async def get_mission_types():
    """지원하는 미션 타입 목록"""
    global api_handler
    
    if api_handler is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="파이프라인이 초기화되지 않았습니다"
        )
    
    return api_handler.get_supported_mission_types()

@app.get("/performance", tags=["상태"])
async def get_performance_metrics():
    """성능 메트릭 조회"""
    global api_handler
    
    if api_handler is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="파이프라인이 초기화되지 않았습니다"
        )
    
    try:
        # ARGOPipeline의 성능 메트릭 조회
        from rag_pipeline import ARGOPipeline
        if hasattr(api_handler, 'pipeline') and isinstance(api_handler.pipeline, ARGOPipeline):
            return await api_handler.get_performance_metrics()
        else:
            return {"message": "성능 메트릭을 사용할 수 없습니다"}
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"성능 메트릭 조회 실패: {str(e)}"
        )

# 서버 이벤트 핸들러
@app.on_event("startup")
async def startup_event():
    """서버 시작 시 초기화"""
    global api_handler, data_file_path
    
    print("🚀 ARGO RAG API 서버 시작")
    print(f"📁 현재 디렉토리: {os.getcwd()}")
    print(f"📁 스크립트 디렉토리: {current_dir}")
    print(f"📁 상위 디렉토리: {parent_dir}")
    
    # 1. 데이터 파일 찾기
    data_file_path = find_data_file()
    if not data_file_path:
        print("⚠️ 데이터 파일을 찾을 수 없습니다. API는 시작되지만 미션 생성은 불가능합니다.")
        print("💡 해결방법: python test_sample_data.py 실행 후 서버 재시작")
        return
    
    # 2. API 핸들러 초기화
    try:
        # rag_pipeline_integrated 대신 최신 구조 사용
        try:
            from rag_pipeline import ARGOAPIHandler
            print("✅ 최신 rag_pipeline 모듈 사용")
        except ImportError:
            try:
                from rag_pipeline_integrated import ARGOAPIHandler
                print("✅ rag_pipeline_integrated 모듈 사용")
            except ImportError:
                print("❌ RAG 파이프라인 모듈을 찾을 수 없습니다")
                return
        
        api_handler = ARGOAPIHandler()
        await api_handler.initialize_api(data_file_path)
        
        print("✅ API 핸들러 초기화 완료")
        print(f"📊 사용 중인 데이터: {data_file_path}")
        
    except Exception as e:
        print(f"❌ API 핸들러 초기화 실패: {e}")
        import traceback
        traceback.print_exc()
        api_handler = None

@app.on_event("shutdown")
async def shutdown_event():
    """서버 종료 시 정리"""
    print("🛑 ARGO RAG API 서버 종료")

# 개발용 실행 함수
def run_development_server():
    """개발용 서버 실행"""
    print("🚀 ARGO RAG API 개발 서버 시작")
    print("📋 사용 가능한 엔드포인트:")
    print("   GET  /            - API 정보")
    print("   GET  /health      - 서버 상태")
    print("   POST /generate-mission - 미션 생성")
    print("   GET  /mission-types    - 미션 타입 목록")
    print("   GET  /performance      - 성능 메트릭")
    print("   GET  /docs        - API 문서 (Swagger)")
    print("   GET  /redoc       - API 문서 (ReDoc)")
    print("")
    print("🌐 서버 주소: http://localhost:8000")
    print("📖 API 문서: http://localhost:8000/docs")
    print("⏹️  종료: Ctrl+C")
    print("")
    
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        reload=False,  # 개발 시에는 True로 설정 가능
        access_log=True
    )

if __name__ == "__main__":
    run_development_server()