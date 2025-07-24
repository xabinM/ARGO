from dataclasses import dataclass
from typing import List, Dict, Optional, Any
from datetime import datetime
import json

@dataclass
class LocationSpot:
    """현장학습 장소 스팟 정보"""
    id: str
    name: str
    location: str
    description: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    category: Optional[str] = None
    subcategory: Optional[str] = None
    heritage_number: Optional[str] = None
    era: Optional[str] = None
    education_grade: Optional[str] = None
    education_subject: Optional[str] = None
    education_curriculum: Optional[str] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    
    def to_dict(self) -> Dict[str, Any]:
        """딕셔너리로 변환"""
        result = {
            'id': self.id,
            'name': self.name,
            'location': self.location,
            'description': self.description,
            'latitude': self.latitude,
            'longitude': self.longitude,
            'category': self.category,
            'subcategory': self.subcategory,
            'heritage_number': self.heritage_number,
            'era': self.era,
            'education_grade': self.education_grade,
            'education_subject': self.education_subject,
            'education_curriculum': self.education_curriculum
        }
        
        if self.created_at:
            result['created_at'] = self.created_at.isoformat()
        if self.updated_at:
            result['updated_at'] = self.updated_at.isoformat()
            
        return result
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'LocationSpot':
        """딕셔너리에서 생성"""
        created_at = None
        updated_at = None
        
        if 'created_at' in data and data['created_at']:
            created_at = datetime.fromisoformat(data['created_at'])
        if 'updated_at' in data and data['updated_at']:
            updated_at = datetime.fromisoformat(data['updated_at'])
            
        return cls(
            id=data['id'],
            name=data['name'],
            location=data['location'],
            description=data['description'],
            latitude=data.get('latitude'),
            longitude=data.get('longitude'),
            category=data.get('category'),
            subcategory=data.get('subcategory'),
            heritage_number=data.get('heritage_number'),
            era=data.get('era'),
            education_grade=data.get('education_grade'),
            education_subject=data.get('education_subject'),
            education_curriculum=data.get('education_curriculum'),
            created_at=created_at,
            updated_at=updated_at
        )

@dataclass
class MissionRequest:
    """미션 생성 요청"""
    location: str
    spot_name: Optional[str] = None
    user_grade: int = 5
    group_size: int = 4
    duration_minutes: int = 30
    preferred_type: Optional[str] = None  # 퀴즈, 관찰미션, 체험미션
    difficulty: str = "medium"  # easy, medium, hard
    context: Optional[str] = None
    subject: Optional[str] = None  # 과목 (사회, 과학, 국어 등)
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            'location': self.location,
            'spot_name': self.spot_name,
            'user_grade': self.user_grade,
            'group_size': self.group_size,
            'duration_minutes': self.duration_minutes,
            'preferred_type': self.preferred_type,
            'difficulty': self.difficulty,
            'context': self.context,
            'subject': self.subject
        }

@dataclass
class MissionResult:
    """미션 생성 결과"""
    mission_id: str
    mission_type: str
    title: str
    content: str
    quiz_questions: Optional[List[Dict]] = None
    learning_objectives: Optional[str] = None
    safety_notes: Optional[str] = None
    estimated_time: Optional[int] = None
    quality_score: float = 0.0
    generation_time: float = 0.0
    source_spots: List[str] = None
    metadata: Dict[str, Any] = None
    created_at: Optional[datetime] = None
    
    def __post_init__(self):
        if self.source_spots is None:
            self.source_spots = []
        if self.metadata is None:
            self.metadata = {}
        if self.created_at is None:
            self.created_at = datetime.now()
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            'mission_id': self.mission_id,
            'mission_type': self.mission_type,
            'title': self.title,
            'content': self.content,
            'quiz_questions': self.quiz_questions,
            'learning_objectives': self.learning_objectives,
            'safety_notes': self.safety_notes,
            'estimated_time': self.estimated_time,
            'quality_score': self.quality_score,
            'generation_time': self.generation_time,
            'source_spots': self.source_spots,
            'metadata': self.metadata,
            'created_at': self.created_at.isoformat() if self.created_at else None
        }

@dataclass
class DatabaseSchema:
    """전체 데이터베이스 스키마"""
    version: str = "1.0"
    total_count: int = 0
    spots: List[LocationSpot] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    
    def __post_init__(self):
        if self.spots is None:
            self.spots = []
        if self.created_at is None:
            self.created_at = datetime.now()
        self.updated_at = datetime.now()
        self.total_count = len(self.spots)
    
    def add_spot(self, spot: LocationSpot):
        """스팟 추가"""
        spot.created_at = datetime.now()
        spot.updated_at = datetime.now()
        self.spots.append(spot)
        self.total_count = len(self.spots)
        self.updated_at = datetime.now()
    
    def update_spot(self, spot_id: str, updated_spot: LocationSpot):
        """스팟 업데이트"""
        for i, spot in enumerate(self.spots):
            if spot.id == spot_id:
                updated_spot.updated_at = datetime.now()
                self.spots[i] = updated_spot
                self.updated_at = datetime.now()
                break
    
    def remove_spot(self, spot_id: str):
        """스팟 제거"""
        self.spots = [spot for spot in self.spots if spot.id != spot_id]
        self.total_count = len(self.spots)
        self.updated_at = datetime.now()
    
    def get_spot_by_id(self, spot_id: str) -> Optional[LocationSpot]:
        """ID로 스팟 조회"""
        for spot in self.spots:
            if spot.id == spot_id:
                return spot
        return None
    
    def get_spots_by_category(self, category: str) -> List[LocationSpot]:
        """카테고리별 스팟 조회"""
        return [spot for spot in self.spots if spot.category == category]
    
    def get_spots_by_location(self, location: str) -> List[LocationSpot]:
        """지역별 스팟 조회"""
        return [spot for spot in self.spots if location.lower() in spot.location.lower()]
    
    def to_dict(self) -> Dict[str, Any]:
        """딕셔너리로 변환"""
        return {
            'version': self.version,
            'total_count': self.total_count,
            'spots': [spot.to_dict() for spot in self.spots],
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'DatabaseSchema':
        """딕셔너리에서 생성"""
        created_at = None
        updated_at = None
        
        if 'created_at' in data and data['created_at']:
            created_at = datetime.fromisoformat(data['created_at'])
        if 'updated_at' in data and data['updated_at']:
            updated_at = datetime.fromisoformat(data['updated_at'])
        
        spots = []
        if 'spots' in data:
            spots = [LocationSpot.from_dict(spot_data) for spot_data in data['spots']]
        
        return cls(
            version=data.get('version', '1.0'),
            total_count=data.get('total_count', len(spots)),
            spots=spots,
            created_at=created_at,
            updated_at=updated_at
        )
    
    def save_to_file(self, file_path: str):
        """파일로 저장"""
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(self.to_dict(), f, ensure_ascii=False, indent=2)
    
    @classmethod
    def load_from_file(cls, file_path: str) -> 'DatabaseSchema':
        """파일에서 로드"""
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        return cls.from_dict(data)

# 스키마 유효성 검사 함수들
def validate_location_spot(spot: LocationSpot) -> List[str]:
    """LocationSpot 유효성 검사"""
    errors = []
    
    if not spot.id:
        errors.append("ID는 필수입니다.")
    if not spot.name:
        errors.append("이름은 필수입니다.")
    if not spot.location:
        errors.append("위치는 필수입니다.")
    if not spot.description:
        errors.append("설명은 필수입니다.")
    
    if spot.latitude is not None and not (-90 <= spot.latitude <= 90):
        errors.append("위도는 -90에서 90 사이여야 합니다.")
    if spot.longitude is not None and not (-180 <= spot.longitude <= 180):
        errors.append("경도는 -180에서 180 사이여야 합니다.")
    
    return errors

def validate_mission_request(request: MissionRequest) -> List[str]:
    """MissionRequest 유효성 검사"""
    errors = []
    
    if not request.location:
        errors.append("장소는 필수입니다.")
    if not (1 <= request.user_grade <= 6):
        errors.append("학년은 1-6 사이여야 합니다.")
    if not (1 <= request.group_size <= 20):
        errors.append("그룹 크기는 1-20 사이여야 합니다.")
    if not (10 <= request.duration_minutes <= 180):
        errors.append("소요 시간은 10-180분 사이여야 합니다.")
    if request.difficulty not in ['easy', 'medium', 'hard']:
        errors.append("난이도는 easy, medium, hard 중 하나여야 합니다.")
    
    return errors