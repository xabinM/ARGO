import os
from pathlib import Path
from typing import Optional, List
from dataclasses import dataclass
from dotenv import load_dotenv

load_dotenv()

@dataclass
class APIConfig:
    """API 관련 통합 설정"""
    # GMS/OpenAI API
    gms_api_key: str
    gms_base_url: str
    openai_api_key: Optional[str] = None
    
    # LLM 설정
    generation_model: str = "gpt-4o"
    embedding_model: str = "text-embedding-3-large"
    max_tokens: int = 800
    temperature: float = 0.7
    timeout: int = 30
    
    # Heritage API (pipeline_config.py에서 가져온 부분)
    heritage_endpoints: dict = None
    api_timeout: int = 15
    api_delay: float = 0.2
    max_pages: int = 50
    items_per_page: int = 100
    
    def __post_init__(self):
        if self.heritage_endpoints is None:
            self.heritage_endpoints = {
                "기본목록": "http://www.khs.go.kr/cha/SearchKindOpenapiList.do",
                "상세정보": "http://www.khs.go.kr/cha/SearchKindOpenapiDt.do"
            }

@dataclass
class EmbeddingConfig:
    """임베딩 관련 설정"""
    model_name: str = "jhgan/ko-sbert-multitask"
    max_length: int = 512
    batch_size: int = 32
    dimension: int = 768

@dataclass
class FAISSConfig:
    """FAISS 인덱스 설정"""
    index_type: str = "auto"
    nlist: int = 100
    ef_construction: int = 200
    ef_search: int = 50

@dataclass
class MissionConfig:
    """미션 생성 설정"""
    max_missions: int = 3
    default_duration: int = 30
    difficulty_levels: List[str] = None
    mission_types: List[str] = None
    
    def __post_init__(self):
        if self.difficulty_levels is None:
            self.difficulty_levels = ["easy", "medium", "hard"]
        if self.mission_types is None:
            self.mission_types = ["퀴즈", "관찰미션", "체험미션", "사진미션"]

class Settings:
    """통합 설정 관리 클래스"""
    
    def __init__(self):
        self.BASE_DIR = Path(__file__).resolve().parent.parent
        
        # 기존 설정들 유지
        self.api = APIConfig(
            gms_api_key=os.getenv('GMS_API_KEY', ''),
            gms_base_url=os.getenv('GMS_BASE_URL', 'https://gms.ssafy.io/gmsapi/api.openai.com/v1'),
            # ... 기존 설정들
        )
        
        # 새로 추가되는 설정들
        self.embedding = EmbeddingConfig()
        self.faiss = FAISSConfig()
        self.mission = MissionConfig()
        
        # 경로 설정 (pipeline_config.py에서 가져온 부분)
        self.paths = {
            "data": {
                "raw": str(self.BASE_DIR / "data" / "raw"),
                "processed": str(self.BASE_DIR / "data" / "processed"),
                "embeddings": str(self.BASE_DIR / "data" / "processed" / "embeddings")
            },
            "cache": str(self.BASE_DIR / "cache"),
            "logs": str(self.BASE_DIR / "logs")
        }
        
        self._create_directories()

# 전역 설정 인스턴스
settings = Settings()