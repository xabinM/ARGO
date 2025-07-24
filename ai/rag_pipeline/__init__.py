# rag_pipeline/__init__.py - 패키지 초기화
"""
ARGO RAG Pipeline Package

AR 기반 현장체험학습 플랫폼을 위한 RAG 파이프라인
- Ko-SBERT 기반 한국어 임베딩
- FAISS 벡터 검색
- 교육과정 연계 미션 생성
- 교사용/학생용 출력 분리
"""

# 핵심 클래스들만 import (에러 처리 포함)
try:
    from .main_pipeline import ARGOPipeline
    from .api_handler import ARGOAPIHandler
    from .models import MissionOutput, ProcessedSpot, SearchResult, MissionRequest
    from .preprocessor import ARGODataPreprocessor
    from .retriever import ARGORAGRetriever
    from .generator import ARGOMissionGenerator
    from .formatter import ARGOOutputFormatter
    from .llm_client import LLMClient
    from .cache_manager import CacheManager
except ImportError as e:
    # import 실패 시에도 패키지가 로드되도록
    import warnings
    warnings.warn(f"일부 모듈 로드 실패: {e}", ImportWarning)

__version__ = "1.0.0"
__author__ = "ARGO Team"

# 메인 인터페이스 (외부에서 사용할 클래스들)
__all__ = [
    "ARGOPipeline",
    "ARGOAPIHandler", 
    "MissionOutput",
    "MissionRequest",
    "SmartMissionGenerator",  # 업데이트된 생성기 추가
    "LLMClient",
    "CacheManager",
    # 고급 사용자용 (선택적)
    "ARGODataPreprocessor",
    "ARGORAGRetriever",
    "ARGOOutputFormatter",
    "ProcessedSpot",
    "SearchResult"
]

# 표준 로깅 설정
import logging
logging.getLogger(__name__).addHandler(logging.NullHandler())