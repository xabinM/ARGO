from typing import List, Dict, Any
from dataclasses import dataclass
import re

from utils.logger import setup_logger
from utils.validators import validate_coordinates

logger = setup_logger(__name__)

@dataclass 
class NormalizedLocationData:
    """정규화된 장소 데이터 (팀원 요구사항 기준)"""
    id: str
    name: str  
    latitude: float
    longitude: float
    content: str
    parent_name: Optional[str] = None
    location: Optional[str] = None

class DataNormalizer:
    def normalize_field_names(self, raw_data: List[Dict]) -> List[Dict]:
        """이름→name, 위치→latitude/longitude 변환"""
        # TODO: 구현
        pass
    
    def create_parent_child_structure(self, data: List[Dict]) -> List[Dict]:
        """계층적 키워드 구조 생성"""
        # TODO: 구현
        pass