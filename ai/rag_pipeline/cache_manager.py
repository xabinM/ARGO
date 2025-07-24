# rag_pipeline/cache_manager.py - 캐시 관리 시스템
import os
import json
import hashlib
import logging
from typing import Dict, Optional
from datetime import datetime, timedelta

logger = logging.getLogger(__name__)

class CacheManager:
    """미션 생성 결과 캐싱 시스템"""
    
    def __init__(self, cache_dir: str = "cache"):
        self.cache_dir = cache_dir
        self.memory_cache = {}
        self.cache_ttl = timedelta(hours=24)  # 24시간 캐시 유지
        
        # 캐시 디렉토리 생성
        os.makedirs(cache_dir, exist_ok=True)
        logger.info(f"캐시 매니저 초기화: {cache_dir}")
    
    def _generate_cache_key(self, location: str, mission_type: str, grade: int, 
                          group_size: int, duration: int) -> str:
        """캐시 키 생성"""
        cache_data = f"{location}_{mission_type}_{grade}_{group_size}_{duration}"
        return hashlib.md5(cache_data.encode()).hexdigest()
    
    def get_cached_mission(self, location: str, mission_type: str, grade: int, 
                          group_size: int, duration: int) -> Optional[Dict]:
        """캐시에서 미션 조회"""
        cache_key = self._generate_cache_key(location, mission_type, grade, group_size, duration)
        
        # 1. 메모리 캐시 확인
        if cache_key in self.memory_cache:
            cached_data, timestamp = self.memory_cache[cache_key]
            if datetime.now() - timestamp < self.cache_ttl:
                logger.info(f"메모리 캐시 히트: {cache_key[:8]}")
                return cached_data
            else:
                # 만료된 캐시 제거
                del self.memory_cache[cache_key]
        
        # 2. 파일 캐시 확인
        cache_file = os.path.join(self.cache_dir, f"{cache_key}.json")
        if os.path.exists(cache_file):
            try:
                with open(cache_file, 'r', encoding='utf-8') as f:
                    cached_data = json.load(f)
                
                # 캐시 만료 시간 확인
                timestamp = datetime.fromisoformat(cached_data['timestamp'])
                if datetime.now() - timestamp < self.cache_ttl:
                    logger.info(f"파일 캐시 히트: {cache_key[:8]}")
                    
                    # 메모리 캐시에도 저장
                    self.memory_cache[cache_key] = (cached_data['data'], timestamp)
                    
                    return cached_data['data']
                else:
                    # 만료된 파일 캐시 삭제
                    os.remove(cache_file)
                    
            except Exception as e:
                logger.warning(f"캐시 파일 읽기 실패: {e}")
                if os.path.exists(cache_file):
                    os.remove(cache_file)
        
        return None
    
    def cache_mission(self, location: str, mission_type: str, grade: int, 
                     group_size: int, duration: int, mission_data: Dict):
        """미션 결과 캐싱"""
        cache_key = self._generate_cache_key(location, mission_type, grade, group_size, duration)
        timestamp = datetime.now()
        
        # 1. 메모리 캐시 저장
        self.memory_cache[cache_key] = (mission_data, timestamp)
        
        # 2. 파일 캐시 저장
        cache_file = os.path.join(self.cache_dir, f"{cache_key}.json")
        cache_content = {
            'data': mission_data,
            'timestamp': timestamp.isoformat(),
            'cache_key': cache_key
        }
        
        try:
            with open(cache_file, 'w', encoding='utf-8') as f:
                json.dump(cache_content, f, ensure_ascii=False, indent=2)
            
            logger.info(f"캐시 저장 완료: {cache_key[:8]}")
            
        except Exception as e:
            logger.warning(f"캐시 파일 저장 실패: {e}")
    
    def clear_expired_cache(self):
        """만료된 캐시 정리"""
        cleared_count = 0
        
        # 메모리 캐시 정리
        expired_keys = []
        for cache_key, (data, timestamp) in self.memory_cache.items():
            if datetime.now() - timestamp >= self.cache_ttl:
                expired_keys.append(cache_key)
        
        for key in expired_keys:
            del self.memory_cache[key]
            cleared_count += 1
        
        # 파일 캐시 정리
        if os.path.exists(self.cache_dir):
            for filename in os.listdir(self.cache_dir):
                if filename.endswith('.json'):
                    filepath = os.path.join(self.cache_dir, filename)
                    try:
                        with open(filepath, 'r', encoding='utf-8') as f:
                            cached_data = json.load(f)
                        
                        timestamp = datetime.fromisoformat(cached_data['timestamp'])
                        if datetime.now() - timestamp >= self.cache_ttl:
                            os.remove(filepath)
                            cleared_count += 1
                            
                    except Exception as e:
                        logger.warning(f"캐시 파일 정리 실패: {e}")
                        # 문제가 있는 파일은 삭제
                        try:
                            os.remove(filepath)
                            cleared_count += 1
                        except:
                            pass
        
        if cleared_count > 0:
            logger.info(f"만료된 캐시 {cleared_count}개 정리 완료")
    
    def get_cache_stats(self) -> Dict:
        """캐시 통계 정보"""
        file_cache_count = 0
        if os.path.exists(self.cache_dir):
            file_cache_count = len([f for f in os.listdir(self.cache_dir) if f.endswith('.json')])
        
        return {
            "memory_cache_count": len(self.memory_cache),
            "file_cache_count": file_cache_count,
            "cache_ttl_hours": self.cache_ttl.total_seconds() / 3600,
            "cache_directory": self.cache_dir
        }