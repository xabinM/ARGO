# 🗂️ AI 프로젝트 파일 구조 정리

## 📁 디렉토리별 파일 배치

### 1. **data_collection/** - 데이터 수집 관련
```
data_collection/
├── __init__.py
├── api_collectors.py           # ← real_api_data_loader.py 내용
├── heritage_api_client.py      # API 클라이언트 클래스
└── validators.py               # ← careful_api_validation.py 내용
```

**파일 설명:**
- `api_collectors.py`: 실제 문화재 API 데이터 수집기
- `heritage_api_client.py`: API 호출 로직 분리
- `validators.py`: API 동작 검증 및 테스트

### 2. **data_processing/** - 데이터 전처리
```
data_processing/
├── __init__.py
├── preprocessors.py            # 데이터 전처리 클래스들
├── postprocessors.py           # 미션 후처리 클래스들
└── formatters.py               # 데이터 포맷 변환
```

**파일 설명:**
- `preprocessors.py`: `rag_pipeline_ver1.py`의 DataPreprocessor 클래스
- `postprocessors.py`: MissionPostprocessor 클래스
- `formatters.py`: API 데이터 → RAG 형식 변환

### 3. **rag_pipeline/** - RAG 파이프라인 코어
```
rag_pipeline/
├── __init__.py
├── pipeline.py                 # ← rag_pipeline_ver1.py 메인 로직
├── mission_generators.py       # 미션 생성 관련 클래스들
├── embeddings.py               # 임베딩 및 벡터 처리
└── quality_evaluators.py       # 품질 평가 시스템
```

**파일 설명:**
- `pipeline.py`: EnhancedRAGPipeline 메인 클래스
- `mission_generators.py`: 다중 미션 생성, 타입 결정 로직
- `embeddings.py`: FAISS, Ko-SBERT 관련
- `quality_evaluators.py`: 미션 품질 자동 평가

### 4. **config/** - 설정 파일들
```
config/
├── __init__.py
├── api_config.py               # API 엔드포인트, 키 관리
├── model_config.py             # LLM 모델 설정
└── pipeline_config.py          # 파이프라인 설정값들
```

**파일 설명:**
- `api_config.py`: 검증된 API URL들, 파라미터 설정
- `model_config.py`: GPT, Ko-SBERT 모델 설정
- `pipeline_config.py`: 캐시 설정, 성능 파라미터

### 5. **data/** - 실제 데이터 파일들
```
data/
├── raw/                        # 원본 API 데이터
│   ├── heritage_raw_20250722.json
│   └── api_response_cache/
├── processed/                  # 전처리된 데이터
│   ├── heritage_rag_data_20250722.json  # ← RAG용 변환 데이터
│   └── embeddings/
│       ├── heritage_embeddings.npy
│       └── heritage_index.faiss
└── sample/                     # 테스트용 샘플 데이터
    └── sample_heritage_data.json
```

### 6. **tests/** - 테스트 코드들
```
tests/
├── __init__.py
├── test_api_collection.py      # API 수집 테스트
├── test_rag_pipeline.py        # RAG 파이프라인 테스트  
├── test_mission_generation.py  # 미션 생성 테스트
└── integration/
    └── test_full_pipeline.py   # 전체 통합 테스트
```

### 7. **utils/** - 유틸리티 함수들
```
utils/
├── __init__.py
├── file_helpers.py             # 파일 I/O 헬퍼
├── text_utils.py               # 텍스트 처리 유틸
├── gps_utils.py                # GPS 좌표 관련
└── logging_utils.py            # 로깅 설정
```

### 8. **cache/** - 캐시 파일들
```
cache/
├── api_cache/                  # API 응답 캐시
├── embedding_cache/            # 임베딩 캐시
└── mission_cache/              # 생성된 미션 캐시
```

### 9. **logs/** - 로그 파일들
```
logs/
├── api_collection.log          # API 수집 로그
├── rag_pipeline.log            # RAG 파이프라인 로그
└── error.log                   # 에러 로그
```

## 📋 주요 파일 이동 계획

| 현재 파일 | 이동할 위치 | 분할 여부 |
|----------|------------|----------|
| `rag_pipeline_ver1.py` | `rag_pipeline/pipeline.py` + `data_processing/preprocessors.py` | ✅ 분할 |
| `real_api_data_loader.py` | `data_collection/api_collectors.py` | ✅ 이동 |
| `careful_api_validation.py` | `data_collection/validators.py` | ✅ 이동 |
| `enhanced_mission_type_system.py` | `rag_pipeline/mission_generators.py` | ✅ 이동 |
| `smart_time_allocation.py` | `rag_pipeline/time_allocators.py` | ✅ 이동 |

## 🚀 실행 방식 변경

### **Before (현재):**
```bash
python rag_pipeline_ver1.py
python real_api_data_loader.py
```

### **After (정리 후):**
```bash
# 메인 실행
python main.py

# 또는 모듈별 실행
python -m data_collection.api_collectors
python -m rag_pipeline.pipeline
python -m tests.test_full_pipeline
```

## 📝 main.py 구조
```python
# main.py
from data_collection.api_collectors import RealAPIDataLoader
from rag_pipeline.pipeline import EnhancedRAGPipeline
from config.pipeline_config import get_default_config

async def main():
    """메인 실행 함수"""
    
    # 1. 설정 로드
    config = get_default_config()
    
    # 2. 데이터 수집
    loader = RealAPIDataLoader(config)
    data_file = await loader.collect_and_save()
    
    # 3. RAG 파이프라인 실행
    pipeline = EnhancedRAGPipeline(config)
    await pipeline.initialize_with_preprocessing(data_file)
    
    # 4. 미션 생성 테스트
    result = await pipeline.generate_mission(location="경복궁")
    print(result)

if __name__ == "__main__":
    asyncio.run(main())
```

## 🔧 다음 단계

1. **파일 분할 및 이동**: 기존 코드들을 적절한 디렉토리로 분할/이동
2. **Import 경로 수정**: 모듈 간 import 경로 정리
3. **Config 파일 생성**: 하드코딩된 설정값들을 config로 분리
4. **테스트 코드 작성**: 각 모듈별 단위 테스트
5. **main.py 완성**: 전체 파이프라인 실행 스크립트
<<<<<<< HEAD

이렇게 정리하면 **프로덕션 레벨의 깔끔한 프로젝트 구조**가 됩니다!
=======
>>>>>>> 85d03ab8ea8eb07361f83355a846262680534491
