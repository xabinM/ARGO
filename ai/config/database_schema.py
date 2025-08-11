from dataclasses import dataclass, field
from typing import List, Dict, Optional, Any
from datetime import datetime
import json

@dataclass
class Coordinates:
    """좌표 정보 (백엔드 Coordinates 엔티티 대응)"""
    latitude: float = 0.0
    longitude: float = 0.0
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            'latitude': self.latitude,
            'longitude': self.longitude
        }
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'Coordinates':
        return cls(
            latitude=data.get('latitude', 0.0),
            longitude=data.get('longitude', 0.0)
        )

@dataclass
class Location:
    """장소 정보 (백엔드 Location 엔티티 대응)"""
    location_id: Optional[int] = None
    name: str = ""
    coordinates: Optional[Coordinates] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    
    def __post_init__(self):
        if self.coordinates is None:
            self.coordinates = Coordinates()
        if self.created_at is None:
            self.created_at = datetime.now()
        if self.updated_at is None:
            self.updated_at = datetime.now()
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            'location_id': self.location_id,
            'name': self.name,
            'coordinates': self.coordinates.to_dict() if self.coordinates else None,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'Location':
        created_at = None
        updated_at = None
        
        if 'created_at' in data and data['created_at']:
            created_at = datetime.fromisoformat(data['created_at'])
        if 'updated_at' in data and data['updated_at']:
            updated_at = datetime.fromisoformat(data['updated_at'])
        
        coordinates = None
        if 'coordinates' in data and data['coordinates']:
            coordinates = Coordinates.from_dict(data['coordinates'])
        
        return cls(
            location_id=data.get('location_id'),
            name=data.get('name', ''),
            coordinates=coordinates,
            created_at=created_at,
            updated_at=updated_at
        )

@dataclass
class Spot:
    """스팟 정보 (백엔드 Spot 엔티티 대응)"""
    id: Optional[int] = None
    name: str = ""
    description: str = ""
    coordinates: Optional[Coordinates] = None
    location_id: Optional[int] = None
    location: Optional[Location] = None
    
    # RAG 파이프라인용 추가 필드 (백엔드에는 없지만 AI 처리용)
    category: Optional[str] = None
    subcategory: Optional[str] = None
    heritage_number: Optional[str] = None
    era: Optional[str] = None
    education_grade: Optional[str] = None
    education_subject: Optional[str] = None
    education_curriculum: Optional[str] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    
    def __post_init__(self):
        if self.coordinates is None:
            self.coordinates = Coordinates()
        if self.created_at is None:
            self.created_at = datetime.now()
        if self.updated_at is None:
            self.updated_at = datetime.now()
    
    def to_dict(self) -> Dict[str, Any]:
        result = {
            'id': self.id,
            'name': self.name,
            'description': self.description,
            'coordinates': self.coordinates.to_dict() if self.coordinates else None,
            'location_id': self.location_id,
            'location': self.location.to_dict() if self.location else None,
            'category': self.category,
            'subcategory': self.subcategory,
            'heritage_number': self.heritage_number,
            'era': self.era,
            'education_grade': self.education_grade,
            'education_subject': self.education_subject,
            'education_curriculum': self.education_curriculum,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }
        return result
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'Spot':
        created_at = None
        updated_at = None
        
        if 'created_at' in data and data['created_at']:
            created_at = datetime.fromisoformat(data['created_at'])
        if 'updated_at' in data and data['updated_at']:
            updated_at = datetime.fromisoformat(data['updated_at'])
        
        coordinates = None
        if 'coordinates' in data and data['coordinates']:
            coordinates = Coordinates.from_dict(data['coordinates'])
        
        location = None
        if 'location' in data and data['location']:
            location = Location.from_dict(data['location'])
        
        return cls(
            id=data.get('id'),
            name=data.get('name', ''),
            description=data.get('description', ''),
            coordinates=coordinates,
            location_id=data.get('location_id'),
            location=location,
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
class QuizProblemOutput:
    """퀴즈 문제 출력 (백엔드 QuizProblem 엔티티 대응)"""
    id: Optional[int] = None
    problem_type: str = "QUIZ"  # 항상 QUIZ
    question: str = ""           # 지문 (Lob)
    choices: List[str] = field(default_factory=list)  # 선택지 (정확히 3개)
    correct_index: int = 0       # 정답 인덱스 (0-2, 0-based)
    explanation: Optional[str] = None  # 해설 (Lob, 옵션)
    
    # 메타데이터 (백엔드에 직접 저장되지 않지만 AI 처리용)
    spot_id: Optional[int] = None
    spot_name: Optional[str] = None
    grade: int = 5
    quality_score: float = 0.0
    generation_time: float = 0.0
    created_at: Optional[datetime] = None
    
    def __post_init__(self):
        if len(self.choices) == 0:
            self.choices = ["", "", ""]  # 기본 3개 선택지
        elif len(self.choices) != 3:
            # 3개가 아닌 경우 조정
            if len(self.choices) < 3:
                self.choices.extend([""] * (3 - len(self.choices)))
            else:
                self.choices = self.choices[:3]
        
        if not (0 <= self.correct_index <= 2):
            self.correct_index = 0
        
        if self.created_at is None:
            self.created_at = datetime.now()
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            'id': self.id,
            'problem_type': self.problem_type,
            'question': self.question,
            'choices': self.choices,
            'correct_index': self.correct_index,
            'explanation': self.explanation,
            'spot_id': self.spot_id,
            'spot_name': self.spot_name,
            'grade': self.grade,
            'quality_score': self.quality_score,
            'generation_time': self.generation_time,
            'created_at': self.created_at.isoformat() if self.created_at else None
        }
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'QuizProblemOutput':
        created_at = None
        if 'created_at' in data and data['created_at']:
            created_at = datetime.fromisoformat(data['created_at'])
        
        return cls(
            id=data.get('id'),
            problem_type=data.get('problem_type', 'QUIZ'),
            question=data.get('question', ''),
            choices=data.get('choices', []),
            correct_index=data.get('correct_index', 0),
            explanation=data.get('explanation'),
            spot_id=data.get('spot_id'),
            spot_name=data.get('spot_name'),
            grade=data.get('grade', 5),
            quality_score=data.get('quality_score', 0.0),
            generation_time=data.get('generation_time', 0.0),
            created_at=created_at
        )

@dataclass
class MissionRequest:
    """미션 생성 요청 (퀴즈 전용으로 단순화)"""
    location: str  # 필수
    spot_name: str  # 필수 (Optional 제거)
    user_grade: int = 5  # 1-6
    problems_count: int = 1  # 생성할 문제 개수
    difficulty: str = "medium"  # easy, medium, hard
    
    # 제거된 필드들: group_size, duration_minutes, preferred_type, context, subject
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            'location': self.location,
            'spot_name': self.spot_name,
            'user_grade': self.user_grade,
            'problems_count': self.problems_count,
            'difficulty': self.difficulty
        }
    
    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'MissionRequest':
        return cls(
            location=data['location'],
            spot_name=data['spot_name'],
            user_grade=data.get('user_grade', 5),
            problems_count=data.get('problems_count', 1),
            difficulty=data.get('difficulty', 'medium')
        )

@dataclass
class BatchQuizRequest:
    """배치 퀴즈 생성 요청"""
    location: str
    grade: int  # 1-6
    spots_count: int  # 스팟 개수
    problems_per_spot: int  # 스팟당 문제 개수
    quality_threshold: float = 0.7  # 품질 임계값
    
    def to_dict(self) -> Dict[str, Any]:
        return {
            'location': self.location,
            'grade': self.grade,
            'spots_count': self.spots_count,
            'problems_per_spot': self.problems_per_spot,
            'quality_threshold': self.quality_threshold
        }

@dataclass
class DatabaseSchema:
    """전체 데이터베이스 스키마 (Spot 기반으로 변경)"""
    version: str = "2.0"  # 버전 업
    total_count: int = 0
    spots: List[Spot] = field(default_factory=list)  # LocationSpot → Spot 변경
    locations: List[Location] = field(default_factory=list)  # 새로 추가
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    
    def __post_init__(self):
        if self.created_at is None:
            self.created_at = datetime.now()
        self.updated_at = datetime.now()
        self.total_count = len(self.spots)
    
    def add_spot(self, spot: Spot):
        """스팟 추가"""
        spot.created_at = datetime.now()
        spot.updated_at = datetime.now()
        self.spots.append(spot)
        self.total_count = len(self.spots)
        self.updated_at = datetime.now()
    
    def add_location(self, location: Location):
        """장소 추가"""
        location.created_at = datetime.now()
        location.updated_at = datetime.now()
        self.locations.append(location)
        self.updated_at = datetime.now()
    
    def get_spot_by_id(self, spot_id: int) -> Optional[Spot]:
        """ID로 스팟 조회"""
        for spot in self.spots:
            if spot.id == spot_id:
                return spot
        return None
    
    def get_spots_by_location_name(self, location_name: str) -> List[Spot]:
        """지역명으로 스팟 조회"""
        return [spot for spot in self.spots 
                if spot.location and location_name.lower() in spot.location.name.lower()]
    
    def get_spots_by_grade(self, grade: int) -> List[Spot]:
        """학년별 스팟 조회"""
        return [spot for spot in self.spots 
                if spot.education_grade and str(grade) in spot.education_grade]
    
    def to_dict(self) -> Dict[str, Any]:
        """딕셔너리로 변환"""
        return {
            'version': self.version,
            'total_count': self.total_count,
            'spots': [spot.to_dict() for spot in self.spots],
            'locations': [location.to_dict() for location in self.locations],
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
            spots = [Spot.from_dict(spot_data) for spot_data in data['spots']]
        
        locations = []
        if 'locations' in data:
            locations = [Location.from_dict(loc_data) for loc_data in data['locations']]
        
        return cls(
            version=data.get('version', '2.0'),
            total_count=data.get('total_count', len(spots)),
            spots=spots,
            locations=locations,
            created_at=created_at,
            updated_at=updated_at
        )

# === 유효성 검사 함수들 ===

def validate_coordinates(coords: Coordinates) -> List[str]:
    """좌표 유효성 검사"""
    errors = []
    
    if not (-90 <= coords.latitude <= 90):
        errors.append("위도는 -90에서 90 사이여야 합니다.")
    if not (-180 <= coords.longitude <= 180):
        errors.append("경도는 -180에서 180 사이여야 합니다.")
    
    return errors

def validate_spot(spot: Spot) -> List[str]:
    """Spot 유효성 검사"""
    errors = []
    
    if not spot.name.strip():
        errors.append("스팟 이름은 필수입니다.")
    if not spot.description.strip():
        errors.append("스팟 설명은 필수입니다.")
    
    if spot.coordinates:
        errors.extend(validate_coordinates(spot.coordinates))
    
    return errors

def validate_quiz_problem(problem: QuizProblemOutput) -> List[str]:
    """QuizProblem 유효성 검사"""
    errors = []
    
    if not problem.question.strip():
        errors.append("문제 지문은 필수입니다.")
    if len(problem.choices) != 3:
        errors.append("선택지는 정확히 3개여야 합니다.")
    if not all(choice.strip() for choice in problem.choices):
        errors.append("모든 선택지는 비어있으면 안 됩니다.")
    if not (0 <= problem.correct_index <= 2):
        errors.append("정답 인덱스는 0-2 사이여야 합니다.")
    if not (1 <= problem.grade <= 6):
        errors.append("학년은 1-6 사이여야 합니다.")
    
    return errors

def validate_mission_request(request: MissionRequest) -> List[str]:
    """MissionRequest 유효성 검사"""
    errors = []
    
    if not request.location.strip():
        errors.append("장소는 필수입니다.")
    if not request.spot_name.strip():
        errors.append("스팟 이름은 필수입니다.")
    if not (1 <= request.user_grade <= 6):
        errors.append("학년은 1-6 사이여야 합니다.")
    if not (1 <= request.problems_count <= 10):
        errors.append("문제 개수는 1-10 사이여야 합니다.")
    if request.difficulty not in ['easy', 'medium', 'hard']:
        errors.append("난이도는 easy, medium, hard 중 하나여야 합니다.")
    
    return errors

def validate_batch_quiz_request(request: BatchQuizRequest) -> List[str]:
    """BatchQuizRequest 유효성 검사"""
    errors = []
    
    if not request.location.strip():
        errors.append("장소는 필수입니다.")
    if not (1 <= request.grade <= 6):
        errors.append("학년은 1-6 사이여야 합니다.")
    if not (1 <= request.spots_count <= 100):
        errors.append("스팟 개수는 1-100 사이여야 합니다.")
    if not (1 <= request.problems_per_spot <= 20):
        errors.append("스팟당 문제 개수는 1-20 사이여야 합니다.")
    if not (0.0 <= request.quality_threshold <= 1.0):
        errors.append("품질 임계값은 0.0-1.0 사이여야 합니다.")
    
    return errors

# === 레거시 지원 함수들 ===

def convert_location_spot_to_spot(location_spot: dict) -> Spot:
    """기존 LocationSpot 데이터를 새로운 Spot으로 변환"""
    coordinates = Coordinates(
        latitude=location_spot.get('latitude', 0.0),
        longitude=location_spot.get('longitude', 0.0)
    )
    
    # Location 객체 생성 (위치 이름으로부터)
    location = Location(
        name=location_spot.get('location', ''),
        coordinates=coordinates
    )
    
    return Spot(
        id=None,  # 새로 생성되므로 None
        name=location_spot.get('name', ''),
        description=location_spot.get('description', ''),
        coordinates=coordinates,
        location=location,
        category=location_spot.get('category'),
        subcategory=location_spot.get('subcategory'),
        heritage_number=location_spot.get('heritage_number'),
        era=location_spot.get('era'),
        education_grade=location_spot.get('education_grade'),
        education_subject=location_spot.get('education_subject'),
        education_curriculum=location_spot.get('education_curriculum')
    )