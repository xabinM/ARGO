# config/settings.py - 통합 설정 관리
"""
🎯 ARGO AI Server 통합 설정

퀴즈 생성과 사진 분석 모든 설정을 중앙 관리
"""

import os
from typing import List, Optional
from pydantic import BaseSettings, Field
import torch

class Settings(BaseSettings):
    """통합 설정 클래스"""
    
    # === 기본 설정 ===
    APP_NAME: str = "ARGO AI API Server"
    VERSION: str = "v1.0"
    DEBUG: bool = Field(default=False, env="DEBUG")
    
    # === API 키 설정 ===
    OPENAI_API_KEY: Optional[str] = Field(default=None, env="OPENAI_API_KEY")
    GMS_API_KEY: Optional[str] = Field(default=None, env="GMS_API_KEY")
    GMS_BASE_URL: str = Field(
        default="https://gms.ssafy.io/gmsapi/api.openai.com/v1",
        env="GMS_BASE_URL"
    )
    
    # === 디바이스 설정 ===
    DEVICE: str = Field(
        default="cuda" if torch.cuda.is_available() else "cpu",
        env="DEVICE"
    )
    
    # === 퀴즈 생성 관련 설정 ===
    
    # RAG 설정
    EMBEDDING_MODEL: str = Field(
        default="snunlp/KR-SBERT-V40K-klueNLI-augSTS",
        env="EMBEDDING_MODEL"
    )
    FAISS_INDEX_PATH: str = Field(
        default="data/faiss_index",
        env="FAISS_INDEX_PATH"
    )
    
    # LLM 설정
    LLM_MODEL: str = Field(default="gpt-4o-mini", env="LLM_MODEL")
    LLM_MAX_TOKENS: int = Field(default=500, env="LLM_MAX_TOKENS")
    LLM_TEMPERATURE: float = Field(default=0.3, env="LLM_TEMPERATURE")
    
    # 퀴즈 생성 제한
    MAX_BATCH_SIZE: int = Field(default=50, env="MAX_BATCH_SIZE")
    MAX_SPOTS_PER_LOCATION: int = Field(default=10, env="MAX_SPOTS_PER_LOCATION")
    QUIZ_TIMEOUT_SECONDS: int = Field(default=30, env="QUIZ_TIMEOUT_SECONDS")
    
    # 캐시 설정
    CACHE_TTL_HOURS: int = Field(default=24, env="CACHE_TTL_HOURS")
    CACHE_DIR: str = Field(default="cache", env="CACHE_DIR")
    
    # === 사진 분석 관련 설정 ===
    
    # YOLO 모델 설정
    YOLO_MODEL_PATH: str = Field(
        default="models/yolov8m.pt",
        env="YOLO_MODEL_PATH"
    )
    CONFIDENCE_THRESHOLD: float = Field(default=0.5, env="CONFIDENCE_THRESHOLD")
    MAX_DETECTION_OBJECTS: int = Field(default=50, env="MAX_DETECTION_OBJECTS")
    
    # 파일 업로드 제한
    MAX_FILE_SIZE_MB: int = Field(default=10, env="MAX_FILE_SIZE_MB")
    ALLOWED_EXTENSIONS: List[str] = Field(
        default=["jpg", "jpeg", "png", "bmp"],
        env="ALLOWED_EXTENSIONS"
    )
    
    # 사진 처리 설정
    PHOTO_TIMEOUT_SECONDS: int = Field(default=15, env="PHOTO_TIMEOUT_SECONDS")
    IMAGE_RESIZE_MAX: int = Field(default=1024, env="IMAGE_RESIZE_MAX")
    
    # === 데이터 관련 설정 ===
    
    # 데이터 파일 경로
    HERITAGE_DATA_PATH: str = Field(
        default="data/heritage_complete_database.json",
        env="HERITAGE_DATA_PATH"
    )
    QUIZ_DATABASE_PATH: str = Field(
        default="data/heritage_quiz_database.json", 
        env="QUIZ_DATABASE_PATH"
    )
    EDUCATIONAL_SPOTS_PATH: str = Field(
        default="data/expanded_educational_spots.py",
        env="EDUCATIONAL_SPOTS_PATH"
    )
    
    # === 로깅 설정 ===
    LOG_LEVEL: str = Field(default="INFO", env="LOG_LEVEL")
    LOG_DIR: str = Field(default="logs", env="LOG_DIR")
    LOG_MAX_SIZE_MB: int = Field(default=10, env="LOG_MAX_SIZE_MB")
    LOG_BACKUP_COUNT: int = Field(default=5, env="LOG_BACKUP_COUNT")
    
    # === 성능 설정 ===
    
    # 동시성 제한
    MAX_CONCURRENT_QUIZ_REQUESTS: int = Field(default=5, env="MAX_CONCURRENT_QUIZ_REQUESTS")
    MAX_CONCURRENT_PHOTO_REQUESTS: int = Field(default=3, env="MAX_CONCURRENT_PHOTO_REQUESTS")
    
    # 배치 처리
    BATCH_CHUNK_SIZE: int = Field(default=10, env="BATCH_CHUNK_SIZE")
    
    # === 개발/운영 환경 분리 ===
    ENVIRONMENT: str = Field(default="development", env="ENVIRONMENT")
    
    @property
    def is_production(self) -> bool:
        return self.ENVIRONMENT.lower() == "production"
    
    @property
    def is_development(self) -> bool:
        return self.ENVIRONMENT.lower() == "development"
    
    # === API 키 우선순위 ===
    def get_primary_api_key(self) -> Optional[str]:
        """우선순위에 따라 API 키 반환"""
        return self.GMS_API_KEY or self.OPENAI_API_KEY
    
    def get_base_url(self) -> str:
        """API 키에 따른 베이스 URL 반환"""
        if self.GMS_API_KEY:
            return self.GMS_BASE_URL
        else:
            return "https://api.openai.com/v1"
    
    # === 경로 유틸리티 ===
    def ensure_directories(self):
        """필요한 디렉토리들 생성"""
        import os
        directories = [
            self.CACHE_DIR,
            self.LOG_DIR,
            "data",
            "models",
            os.path.dirname(self.FAISS_INDEX_PATH)
        ]
        
        for directory in directories:
            os.makedirs(directory, exist_ok=True)
    
    # === 검증 메소드 ===
    def validate_settings(self) -> List[str]:
        """설정 유효성 검사"""
        errors = []
        
        # API 키 체크
        if not self.get_primary_api_key():
            errors.append("API 키가 설정되지 않았습니다 (GMS_API_KEY 또는 OPENAI_API_KEY)")
        
        # 모델 파일 체크
        if not os.path.exists(self.YOLO_MODEL_PATH):
            errors.append(f"YOLO 모델 파일이 없습니다: {self.YOLO_MODEL_PATH}")
        
        # GPU 설정 체크
        if self.DEVICE == "cuda" and not torch.cuda.is_available():
            errors.append("CUDA를 사용하도록 설정되었지만 GPU를 사용할 수 없습니다")
        
        # 파일 크기 제한 체크
        if self.MAX_FILE_SIZE_MB > 50:
            errors.append("파일 크기 제한이 너무 큽니다 (권장: 10MB 이하)")
        
        return errors
    
    # === 시스템 정보 ===
    def get_system_info(self) -> dict:
        """시스템 정보 반환"""
        return {
            "device": self.DEVICE,
            "gpu_available": torch.cuda.is_available(),
            "environment": self.ENVIRONMENT,
            "debug_mode": self.DEBUG,
            "api_provider": "GMS" if self.GMS_API_KEY else "OpenAI",
            "quiz_model": self.EMBEDDING_MODEL.split("/")[-1],
            "yolo_model": os.path.basename(self.YOLO_MODEL_PATH)
        }
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = True

# 싱글톤 인스턴스
_settings = None

def get_settings() -> Settings:
    """설정 싱글톤 인스턴스 반환"""
    global _settings
    if _settings is None:
        _settings = Settings()
        _settings.ensure_directories()
    return _settings

# 편의 함수들
def is_production() -> bool:
    return get_settings().is_production

def get_device() -> str:
    return get_settings().DEVICE

def validate_environment() -> List[str]:
    """환경 유효성 검사"""
    settings = get_settings()
    return settings.validate_settings()