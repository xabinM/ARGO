"""
파일 I/O 헬퍼 함수들
"""
import os
import json
import logging
from typing import Any, Dict, Optional
from datetime import datetime
from pathlib import Path

logger = logging.getLogger(__name__)

def ensure_directory(directory_path: str) -> None:
    """디렉토리가 없으면 생성"""
    Path(directory_path).mkdir(parents=True, exist_ok=True)

def save_json_data(data: Any, filepath: str, ensure_dir: bool = True) -> bool:
    """JSON 데이터 저장"""
    try:
        if ensure_dir:
            directory = os.path.dirname(filepath)
            if directory:
                ensure_directory(directory)
        
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        
        logger.info(f"💾 파일 저장 완료: {filepath}")
        return True
        
    except Exception as e:
        logger.error(f"❌ 파일 저장 실패: {filepath} - {e}")
        return False

def load_json_data(filepath: str) -> Optional[Any]:
    """JSON 데이터 로드"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        logger.info(f"📂 파일 로드 완료: {filepath}")
        return data
        
    except FileNotFoundError:
        logger.warning(f"📂 파일을 찾을 수 없음: {filepath}")
        return None
    except json.JSONDecodeError as e:
        logger.error(f"❌ JSON 파싱 오류: {filepath} - {e}")
        return None
    except Exception as e:
        logger.error(f"❌ 파일 로드 실패: {filepath} - {e}")
        return None

def get_latest_file(directory: str, pattern: str = "*.json") -> Optional[str]:
    """디렉토리에서 가장 최근 파일 찾기"""
    try:
        from glob import glob
        
        files = glob(os.path.join(directory, pattern))
        if not files:
            return None
        
        # 수정 시간 기준으로 최신 파일 반환
        latest_file = max(files, key=os.path.getmtime)
        return latest_file
        
    except Exception as e:
        logger.error(f"❌ 최신 파일 검색 실패: {directory} - {e}")
        return None

def generate_timestamped_filename(prefix: str, extension: str = "json") -> str:
    """타임스탬프가 포함된 파일명 생성"""
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    return f"{prefix}_{timestamp}.{extension}"

def get_file_size_mb(filepath: str) -> float:
    """파일 크기 (MB) 반환"""
    try:
        size_bytes = os.path.getsize(filepath)
        return size_bytes / (1024 * 1024)
    except:
        return 0.0

def cleanup_old_files(directory: str, keep_count: int = 10, pattern: str = "*.json") -> int:
    """오래된 파일들 정리 (최신 N개만 유지)"""
    try:
        from glob import glob
        
        files = glob(os.path.join(directory, pattern))
        if len(files) <= keep_count:
            return 0
        
        # 수정 시간 기준으로 정렬 (최신순)
        files.sort(key=os.path.getmtime, reverse=True)
        
        # 오래된 파일들 삭제
        deleted_count = 0
        for file_to_delete in files[keep_count:]:
            try:
                os.remove(file_to_delete)
                deleted_count += 1
                logger.info(f"🗑️ 오래된 파일 삭제: {file_to_delete}")
            except Exception as e:
                logger.warning(f"파일 삭제 실패: {file_to_delete} - {e}")
        
        return deleted_count
        
    except Exception as e:
        logger.error(f"❌ 파일 정리 실패: {directory} - {e}")
        return 0

def copy_file(source: str, destination: str) -> bool:
    """파일 복사"""
    try:
        import shutil
        
        # 대상 디렉토리 생성
        dest_dir = os.path.dirname(destination)
        if dest_dir:
            ensure_directory(dest_dir)
        
        shutil.copy2(source, destination)
        logger.info(f"📋 파일 복사 완료: {source} → {destination}")
        return True
        
    except Exception as e:
        logger.error(f"❌ 파일 복사 실패: {source} → {destination} - {e}")
        return False

def archive_file(filepath: str, archive_dir: str = "archive") -> bool:
    """파일을 아카이브 디렉토리로 이동"""
    try:
        import shutil
        
        if not os.path.exists(filepath):
            return False
        
        # 아카이브 디렉토리 생성
        ensure_directory(archive_dir)
        
        # 파일명에 타임스탬프 추가
        filename = os.path.basename(filepath)
        name, ext = os.path.splitext(filename)
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        archived_filename = f"{name}_archived_{timestamp}{ext}"
        
        archived_path = os.path.join(archive_dir, archived_filename)
        shutil.move(filepath, archived_path)
        
        logger.info(f"📦 파일 아카이브 완료: {filepath} → {archived_path}")
        return True
        
    except Exception as e:
        logger.error(f"❌ 파일 아카이브 실패: {filepath} - {e}")
        return False

class FileManager:
    """파일 관리 클래스"""
    
    def __init__(self, base_dir: str = "."):
        self.base_dir = base_dir
        
    def get_full_path(self, relative_path: str) -> str:
        """상대 경로를 절대 경로로 변환"""
        return os.path.join(self.base_dir, relative_path)
    
    def ensure_project_structure(self) -> None:
        """프로젝트 디렉토리 구조 생성"""
        directories = [
            "data/raw",
            "data/processed",
            "data/processed/embeddings",
            "data/sample",
            "cache/api_cache",
            "cache/embedding_cache", 
            "cache/mission_cache",
            "logs",
            "models"
        ]
        
        for directory in directories:
            full_path = self.get_full_path(directory)
            ensure_directory(full_path)
        
        logger.info("📁 프로젝트 디렉토리 구조 생성 완료")
    
    def get_data_files(self, data_type: str = "processed") -> list:
        """데이터 파일 목록 반환"""
        data_dir = self.get_full_path(f"data/{data_type}")
        
        try:
            from glob import glob
            json_files = glob(os.path.join(data_dir, "*.json"))
            return sorted(json_files, key=os.path.getmtime, reverse=True)
        except:
            return []
    
    def cleanup_cache(self, older_than_hours: int = 24) -> int:
        """오래된 캐시 파일들 정리"""
        cache_dir = self.get_full_path("cache")
        cutoff_time = datetime.now().timestamp() - (older_than_hours * 3600)
        
        deleted_count = 0
        
        try:
            for root, dirs, files in os.walk(cache_dir):
                for file in files:
                    file_path = os.path.join(root, file)
                    if os.path.getmtime(file_path) < cutoff_time:
                        try:
                            os.remove(file_path)
                            deleted_count += 1
                        except:
                            pass
            
            logger.info(f"🧹 캐시 정리 완료: {deleted_count}개 파일 삭제")
            return deleted_count
            
        except Exception as e:
            logger.error(f"❌ 캐시 정리 실패: {e}")
            return 0

def main():
    """테스트용 메인 함수"""
    # 파일 매니저 테스트
    fm = FileManager(".")
    fm.ensure_project_structure()
    
    # 샘플 데이터 저장 테스트
    sample_data = {"test": "data", "timestamp": datetime.now().isoformat()}
    test_file = "data/sample/test_data.json"
    
    if save_json_data(sample_data, test_file):
        loaded_data = load_json_data(test_file)
        print(f"테스트 성공: {loaded_data}")

if __name__ == "__main__":
    main()