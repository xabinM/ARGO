# rag_pipeline/retriever.py - 급속 버전 (간소화)
import logging
from typing import List, Dict, Optional
import re

logger = logging.getLogger(__name__)

class RapidRAGRetriever:
    """급속 버전 검색기 - 단순하지만 효과적"""
    
    def __init__(self):
        self.spots = []
        self.location_index = {}  # 간단한 인덱스
        self.name_index = {}
        self.initialized = False
        
    async def initialize(self, spots_data: List[Dict]):
        """검색기 초기화 (간소화)"""
        
        try:
            logger.info("⚡ 급속 검색기 초기화")
            
            self.spots = spots_data
            self._build_simple_indexes()
            self.initialized = True
            
            logger.info(f"✅ 검색기 준비: {len(self.spots)}개 스팟")
            
        except Exception as e:
            logger.error(f"❌ 검색기 초기화 실패: {e}")
            raise
    
    def _build_simple_indexes(self):
        """간단한 인덱스 구축 (복잡한 임베딩 없이)"""
        
        for i, spot in enumerate(self.spots):
            spot_name = spot.get('이름', '')
            location = spot.get('주소', spot.get('위치', ''))
            
            # 위치별 인덱스
            location_key = self._normalize_text(location)
            if location_key not in self.location_index:
                self.location_index[location_key] = []
            self.location_index[location_key].append(i)
            
            # 이름별 인덱스  
            name_key = self._normalize_text(spot_name)
            if name_key not in self.name_index:
                self.name_index[name_key] = []
            self.name_index[name_key].append(i)
        
        logger.info(f"📊 인덱스 구축: 위치 {len(self.location_index)}개, 이름 {len(self.name_index)}개")
    
    def _normalize_text(self, text: str) -> str:
        """텍스트 정규화 (간단 버전)"""
        if not text:
            return ""
        
        # 기본 정리
        normalized = re.sub(r'\s+', '', text.lower())
        
        # 일반적인 변형 처리
        replacements = {
            '경복궁': '경복궁',
            '창덕궁': '창덕궁', 
            '창경궁': '창경궁',
            '덕수궁': '덕수궁',
            '박물관': '박물관',
            '공원': '공원',
            '타워': '타워'
        }
        
        for old, new in replacements.items():
            if old in normalized:
                normalized = normalized.replace(old, new)
        
        return normalized
    
    def search_by_location_and_name(self, location: str, spot_name: str, 
                                  max_results: int = 5) -> List[Dict]:
        """위치 + 이름으로 검색 (핵심 메서드)"""
        
        if not self.initialized:
            return []
        
        logger.info(f"🔍 검색: {location} - {spot_name}")
        
        results = []
        
        # 1. 정확한 이름 매칭 우선
        name_results = self._search_by_name(spot_name)
        
        # 2. 위치 필터링
        location_filtered = []
        for spot in name_results:
            spot_location = spot.get('주소', spot.get('위치', ''))
            if self._is_location_match(location, spot_location):
                location_filtered.append(spot)
        
        results.extend(location_filtered)
        
        # 3. 결과 부족하면 위치만으로 검색
        if len(results) < max_results:
            location_results = self._search_by_location(location, max_results - len(results))
            
            # 중복 제거
            existing_names = {r.get('이름', '') for r in results}
            for spot in location_results:
                if spot.get('이름', '') not in existing_names:
                    results.append(spot)
        
        logger.info(f"📊 검색 결과: {len(results)}개")
        return results[:max_results]
    
    def search_by_location(self, location: str, max_results: int = 10) -> List[Dict]:
        """위치로만 검색"""
        return self._search_by_location(location, max_results)
    
    def _search_by_name(self, spot_name: str) -> List[Dict]:
        """이름으로 검색"""
        
        normalized_name = self._normalize_text(spot_name)
        results = []
        
        # 정확한 매칭
        if normalized_name in self.name_index:
            for idx in self.name_index[normalized_name]:
                results.append(self.spots[idx])
        
        # 부분 매칭
        if not results:
            for name_key, indices in self.name_index.items():
                if (normalized_name in name_key or 
                    name_key in normalized_name or
                    self._calculate_similarity(normalized_name, name_key) > 0.7):
                    
                    for idx in indices:
                        if self.spots[idx] not in results:
                            results.append(self.spots[idx])
        
        return results
    
    def _search_by_location(self, location: str, max_results: int) -> List[Dict]:
        """위치로 검색"""
        
        normalized_location = self._normalize_text(location)
        results = []
        
        # 위치 인덱스에서 검색
        for location_key, indices in self.location_index.items():
            if self._is_location_match(normalized_location, location_key):
                for idx in indices:
                    if len(results) < max_results:
                        results.append(self.spots[idx])
        
        # 결과 부족하면 전체 스캔
        if len(results) < max_results:
            for spot in self.spots:
                if len(results) >= max_results:
                    break
                
                spot_location = spot.get('주소', spot.get('위치', ''))
                if (spot not in results and 
                    self._is_location_match(location, spot_location)):
                    results.append(spot)
        
        return results[:max_results]
    
    def _is_location_match(self, query_location: str, spot_location: str) -> bool:
        """위치 매칭 확인"""
        
        query_norm = self._normalize_text(query_location)
        spot_norm = self._normalize_text(spot_location)
        
        if not query_norm or not spot_norm:
            return False
        
        # 포함 관계 확인
        return (query_norm in spot_norm or 
                spot_norm in query_norm or
                self._calculate_similarity(query_norm, spot_norm) > 0.6)
    
    def _calculate_similarity(self, text1: str, text2: str) -> float:
        """간단한 유사도 계산"""
        
        if not text1 or not text2:
            return 0.0
        
        # 문자 집합 기반 유사도
        set1 = set(text1)
        set2 = set(text2)
        
        if not set1 or not set2:
            return 0.0
        
        intersection = len(set1 & set2)
        union = len(set1 | set2)
        
        return intersection / union if union > 0 else 0.0
    
    def get_all_locations(self) -> List[str]:
        """모든 위치 목록 (디버깅용)"""
        return list(self.location_index.keys())
    
    def get_all_spot_names(self) -> List[str]:
        """모든 스팟 이름 목록 (디버깅용)"""
        return [spot.get('이름', '') for spot in self.spots]
    
    def get_statistics(self) -> Dict:
        """검색기 통계"""
        return {
            "initialized": self.initialized,
            "total_spots": len(self.spots),
            "location_index_size": len(self.location_index),
            "name_index_size": len(self.name_index)
        }