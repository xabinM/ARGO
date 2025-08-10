"""
로깅 설정 유틸리티
"""
import logging
import logging.handlers
import os
from datetime import datetime
from typing import Optional

from config.pipeline_config import get_env_config
from utils.file_helpers import ensure_directory

def setup_logger(name: str, config: Optional[dict] = None) -> logging.Logger:
    """로거 설정"""
    
    if config is None:
        config = get_env_config()["logging"]
    
    # 로그 디렉토리 생성
    log_dir = "logs"
    ensure_directory(log_dir)
    
    # 로거 생성
    logger = logging.getLogger(name)
    
    # 이미 핸들러가 있으면 중복 방지
    if logger.handlers:
        return logger
    
    logger.setLevel(getattr(logging, config["level"]))
    
    # 포맷터 생성
    formatter = logging.Formatter(
        config["format"],
        datefmt="%Y-%m-%d %H:%M:%S"
    )
    
    # 콘솔 핸들러
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)
    
    # 파일 핸들러 (회전 로그)
    if config.get("file_rotation", True):
        log_filename = os.path.join(log_dir, f"{name.replace('.', '_')}.log")
        
        file_handler = logging.handlers.RotatingFileHandler(
            log_filename,
            maxBytes=config.get("max_file_size_mb", 10) * 1024 * 1024,
            backupCount=config.get("backup_count", 5),
            encoding='utf-8'
        )
        file_handler.setLevel(getattr(logging, config["level"]))
        file_handler.setFormatter(formatter)
        logger.addHandler(file_handler)
    
    return logger

def setup_main_logger() -> logging.Logger:
    """메인 애플리케이션 로거 설정"""
    return setup_logger("main")

def setup_api_logger() -> logging.Logger:
    """API 관련 로거 설정"""
    return setup_logger("api_collection")

def setup_rag_logger() -> logging.Logger:
    """RAG 파이프라인 로거 설정"""
    return setup_logger("rag_pipeline")

def log_function_call(func):
    """함수 호출 로깅 데코레이터"""
    def wrapper(*args, **kwargs):
        logger = logging.getLogger(func.__module__)
        logger.debug(f"🔧 함수 호출: {func.__name__}")
        
        try:
            result = func(*args, **kwargs)
            logger.debug(f"✅ 함수 완료: {func.__name__}")
            return result
        except Exception as e:
            logger.error(f"❌ 함수 오류: {func.__name__} - {e}")
            raise
    
    return wrapper

def log_performance(func):
    """성능 측정 로깅 데코레이터"""
    import time
    
    def wrapper(*args, **kwargs):
        logger = logging.getLogger(func.__module__)
        start_time = time.time()
        
        try:
            result = func(*args, **kwargs)
            end_time = time.time()
            duration = end_time - start_time
            logger.info(f"⏱️ {func.__name__} 실행 시간: {duration:.2f}초")
            return result
        except Exception as e:
            end_time = time.time()
            duration = end_time - start_time
            logger.error(f"❌ {func.__name__} 실패 (소요시간: {duration:.2f}초) - {e}")
            raise
    
    return wrapper

class LogCapture:
    """로그 캡처 컨텍스트 매니저"""
    
    def __init__(self, logger_name: str, level: int = logging.INFO):
        self.logger_name = logger_name
        self.level = level
        self.logs = []
        self.handler = None
    
    def __enter__(self):
        # 메모리 핸들러 생성
        self.handler = logging.Handler()
        self.handler.setLevel(self.level)
        
        # 로그 캡처 함수
        def emit(record):
            self.logs.append(self.handler.format(record))
        
        self.handler.emit = emit
        
        # 로거에 핸들러 추가
        logger = logging.getLogger(self.logger_name)
        logger.addHandler(self.handler)
        
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        # 핸들러 제거
        if self.handler:
            logger = logging.getLogger(self.logger_name)
            logger.removeHandler(self.handler)
    
    def get_logs(self) -> list:
        """캡처된 로그 반환"""
        return self.logs.copy()

def setup_all_loggers():
    """모든 주요 로거들 설정"""
    loggers = {
        "main": setup_main_logger(),
        "api": setup_api_logger(), 
        "rag": setup_rag_logger(),
        "data_collection": setup_logger("data_collection"),
        "data_processing": setup_logger("data_processing"),
        "rag_pipeline": setup_logger("rag_pipeline")
    }
    
    # 루트 로거 설정
    root_logger = logging.getLogger()
    if not root_logger.handlers:
        root_logger.setLevel(logging.WARNING)
        
        # 콘솔 핸들러만 추가 (파일은 개별 로거에서 처리)
        console_handler = logging.StreamHandler()
        console_handler.setLevel(logging.WARNING)
        formatter = logging.Formatter(
            "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
        )
        console_handler.setFormatter(formatter)
        root_logger.addHandler(console_handler)
    
    return loggers

def main():
    """테스트용 메인 함수"""
    # 로거 설정 테스트
    loggers = setup_all_loggers()
    
    # 각 로거 테스트
    for name, logger in loggers.items():
        logger.info(f"✅ {name} 로거 테스트 성공")
        logger.debug(f"🔧 {name} 디버그 메시지")
        logger.warning(f"⚠️ {name} 경고 메시지")
    
    # 로그 캡처 테스트
    with LogCapture("main") as capture:
        main_logger = loggers["main"]
        main_logger.info("캡처 테스트 메시지 1")
        main_logger.warning("캡처 테스트 메시지 2")
    
    captured_logs = capture.get_logs()
    print(f"캡처된 로그 수: {len(captured_logs)}")
    
    print("🎉 로깅 시스템 테스트 완료")

if __name__ == "__main__":
    main()