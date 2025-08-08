# services/spot_service.py - 스팟 관리 서비스
"""
🎯 스팟 데이터 관리 서비스

주요 기능:
- 스팟 데이터 로딩 및 인덱싱
- 효율적인 검색 기능
- 캐싱 및 성능 최적화
"""

import logging
from typing import Dict, List, Optional
from functools import lru_cache

logger = logging.getLogger(__name__)

class SpotService:
    """스팟 관리 서비스"""
    
    def __init__(self):
        self.spots_database = {}  # spot_id -> spot_info
        self.spots_by_name = {}   # spot_name -> spot_info
        self.location_index = {}  # location -> [spot_ids]
        self.is_initialized = False
    
    def initialize(self):
        """서비스 초기화"""
        logger.info("📍 스팟 서비스 초기화 중...")
        
        try:
            # 스팟 데이터 로딩
            self._load_spots_data()
            
            # 인덱스 구축
            self._build_indexes()
            
            self.is_initialized = True
            logger.info(f"✅ 스팟 서비스 초기화 완료: {len(self.spots_database)}개 스팟")
            
        except Exception as e:
            logger.error(f"❌ 스팟 서비스 초기화 실패: {e}")
            self.is_initialized = False
    
    def _load_spots_data(self):
        """스팟 데이터 로딩"""
        try:
            from expanded_educational_spots import EXPANDED_EDUCATIONAL_SPOTS
            
            if not EXPANDED_EDUCATIONAL_SPOTS:
                logger.warning("⚠️ 스팟 데이터가 비어있습니다")
                return
            
            for idx, spot in enumerate(EXPANDED_EDUCATIONAL_SPOTS):
                spot_id = idx + 1
                # 데이터 정제 및 보강
                processed_spot = self._process_spot_data(spot, spot_id)
                self.spots_database[spot_id] = processed_spot
                
        except ImportError:
            logger.error("❌ expanded_educational_spots 모듈을 찾을 수 없습니다")
            self._create_minimal_data()
    
    def _process_spot_data(self, spot: Dict, spot_id: int) -> Dict:
        """스팟 데이터 처리 및 보강"""
        processed = spot.copy()
        processed["spot_id"] = spot_id
        
        # 필수 필드 검증 및 기본값 설정
        required_fields = {
            "메인장소": "알 수 없는 장소",
            "세부스팟": f"스팟 {spot_id}",
            "이름": f"스팟 {spot_id}",
            "위치": "위치 정보 없음",
            "설명": "설명이 없습니다.",
            "교육키워드": ["일반", "교육"]
        }
        
        for field, default_value in required_fields.items():
            if field not in processed or not processed[field]:
                processed[field] = default_value
        
        # 좌표 정보 검증
        if "위도" not in processed or "경도" not in processed:
            processed["위도"] = 37.5665  # 서울 기본 좌표
            processed["경도"] = 126.9780
        
        # 학년 적합도 추가 (없으면 전체 학년)
        if "학년적합도" not in processed:
            processed["학년적합도"] = [1, 2, 3, 4, 5, 6]
        
        return processed
    
    def _build_indexes(self):
        """검색용 인덱스 구축"""
        self.spots_by_name.clear()
        self.location_index.clear()
        
        for spot_id, spot_info in self.spots_database.items():
            # 이름 기반 인덱스
            spot_name = spot_info["세부스팟"]
            main_location = spot_info["메인장소"]
            
            self.spots_by_name[spot_name] = spot_info
            
            # 메인 장소로도 검색 가능 (중복 방지)
            if main_location not in self.spots_by_name:
                self.spots_by_name[main_location] = spot_info
            
            # 위치 기반 인덱스
            if main_location not in self.location_index:
                self.location_index[main_location] = []
            self.location_index[main_location].append(spot_id)
    
    def _create_minimal_data(self):
        """최소 데이터 생성 (비상용)"""
        logger.info("📝 최소 스팟 데이터 생성")
        
        minimal_spots = [
            {
                "메인장소": "경복궁",
                "세부스팟": "근정전",
                "이름": "경복궁 근정전",
                "위치": "서울 종로구",
                "위도": 37.5788,
                "경도": 126.9770,
                "설명": "조선시대의 정전으로 임금이 신하들과 조정 업무를 보던 곳",
                "교육키워드": ["조선시대", "궁궐", "정치", "역사"],
                "학년적합도": [3, 4, 5, 6]
            },
            {
                "메인장소": "서울대공원",
                "세부스팟": "사슴사",
                "이름": "서울대공원 사슴사",
                "위치": "경기 과천시",
                "위도": 37.4363,
                "경도": 127.0182,
                "설명": "사슴의 생태를 관찰할 수 있는 동물 전시 공간",
                "교육키워드": ["동물", "생태", "자연", "관찰"],
                "학년적합도": [1, 2, 3, 4]
            }
        ]
        
        for idx, spot in enumerate(minimal_spots):
            spot_id = idx + 1
            processed_spot = self._process_spot_data(spot, spot_id)
            self.spots_database[spot_id] = processed_spot
    
    @lru_cache(maxsize=128)
    def find_spot_by_name(self, spot_name: str) -> Optional[Dict]:
        """스팟명으로 스팟 검색 (캐시됨)"""
        if not self.is_initialized:
            return None
        
        # 정확한 매칭 우선
        if spot_name in self.spots_by_name:
            return self.spots_by_name[spot_name]
        
        # 부분 매칭
        for name, spot_info in self.spots_by_name.items():
            if spot_name in name or name in spot_name:
                return spot_info
        
        return None
    
    def find_spot_by_id(self, spot_id: int) -> Optional[Dict]:
        """스팟 ID로 스팟 검색"""
        if not self.is_initialized:
            return None
        
        return self.spots_database.get(spot_id)
    
    def get_spots_by_location(self, location: str, limit: int = 10) -> List[Dict]:
        """위치별 스팟 목록 조회"""
        if not self.is_initialized:
            return []
        
        result = []
        
        # 정확한 위치 매칭
        if location in self.location_index:
            spot_ids = self.location_index[location][:limit]
            result = [self.spots_database[sid] for sid in spot_ids]
        
        # 부분 매칭으로 확장
        if len(result) < limit:
            for loc, spot_ids in self.location_index.items():
                if location in loc or loc in location:
                    for sid in spot_ids:
                        if len(result) >= limit:
                            break
                        spot = self.spots_database[sid]
                        if spot not in result:
                            result.append(spot)
        
        return result[:limit]
    
    def get_spots(
        self, 
        location: Optional[str] = None, 
        grade: Optional[int] = None,
        limit: int = 20
    ) -> List[Dict]:
        """스팟 목록 조회 (필터링 지원)"""
        if not self.is_initialized:
            return []
        
        # 기본 스팟 목록
        if location:
            spots = self.get_spots_by_location(location, limit * 2)  # 여유분 확보
        else:
            spots = list(self.spots_database.values())
        
        # 학년 필터링
        if grade:
            spots = [
                spot for spot in spots 
                if grade in spot.get("학년적합도", [1, 2, 3, 4, 5, 6])
            ]
        
        # 결과 포맷팅
        formatted_spots = []
        for spot in spots[:limit]:
            formatted_spots.append({
                "spot_id": spot["spot_id"],
                "spot_name": spot["세부스팟"],
                "full_name": spot["이름"],
                "location": spot["메인장소"],
                "description": self._truncate_description(spot["설명"]),
                "latitude": spot["위도"],
                "longitude": spot["경도"],
                "keywords": spot["교육키워드"][:3],
                "suitable_grades": spot.get("학년적합도", [1, 2, 3, 4, 5, 6]),
                "is_recommended": not grade or grade in spot.get("학년적합도", [])
            })
        
        return formatted_spots
    
    def _truncate_description(self, description: str, max_length: int = 100) -> str:
        """설명 텍스트 요약"""
        if len(description) <= max_length:
            return description
        return description[:max_length] + "..."
    
    def get_total_spots(self) -> int:
        """총 스팟 개수 반환"""
        return len(self.spots_database)
    
    def get_locations(self) -> List[str]:
        """사용 가능한 위치 목록 반환"""
        return list(self.location_index.keys())
    
    def get_spot_statistics(self) -> Dict:
        """스팟 통계 정보 반환"""
        if not self.is_initialized:
            return {}
        
        # 위치별 통계
        location_stats = {}
        for location, spot_ids in self.location_index.items():
            location_stats[location] = len(spot_ids)
        
        # 학년별 통계
        grade_stats = {i: 0 for i in range(1, 7)}
        for spot in self.spots_database.values():
            for grade in spot.get("학년적합도", []):
                if 1 <= grade <= 6:
                    grade_stats[grade] += 1
        
        # 키워드 통계
        keyword_count = {}
        for spot in self.spots_database.values():
            for keyword in spot.get("교육키워드", []):
                keyword_count[keyword] = keyword_count.get(keyword, 0) + 1
        
        top_keywords = sorted(keyword_count.items(), key=lambda x: x[1], reverse=True)[:10]
        
        return {
            "total_spots": len(self.spots_database),
            "total_locations": len(self.location_index),
            "location_distribution": location_stats,
            "grade_distribution": grade_stats,
            "top_keywords": top_keywords,
            "coordinates_range": self._get_coordinates_range()
        }
    
    def _get_coordinates_range(self) -> Dict:
        """좌표 범위 계산"""
        if not self.spots_database:
            return {}
        
        lats = [spot["위도"] for spot in self.spots_database.values()]
        lons = [spot["경도"] for spot in self.spots_database.values()]
        
        return {
            "latitude": {"min": min(lats), "max": max(lats)},
            "longitude": {"min": min(lons), "max": max(lons)},
            "center": {
                "latitude": sum(lats) / len(lats),
                "longitude": sum(lons) / len(lons)
            }
        }
    
    def validate_spot_data(self) -> Dict:
        """스팟 데이터 검증"""
        issues = {
            "missing_coordinates": [],
            "empty_descriptions": [],
            "missing_keywords": [],
            "invalid_grades": []
        }
        
        for spot_id, spot in self.spots_database.items():
            # 좌표 검증
            if not spot.get("위도") or not spot.get("경도"):
                issues["missing_coordinates"].append(spot_id)
            
            # 설명 검증
            if not spot.get("설명") or len(spot["설명"]) < 10:
                issues["empty_descriptions"].append(spot_id)
            
            # 키워드 검증
            if not spot.get("교육키워드") or len(spot["교육키워드"]) == 0:
                issues["missing_keywords"].append(spot_id)
            
            # 학년 검증
            grades = spot.get("학년적합도", [])
            if not grades or not all(1 <= g <= 6 for g in grades):
                issues["invalid_grades"].append(spot_id)
        
        return {
            "total_issues": sum(len(v) for v in issues.values()),
            "issues": issues,
            "data_quality_score": 1.0 - (sum(len(v) for v in issues.values()) / (len(self.spots_database) * 4))
        }