# rag_pipeline/models.py - 데이터 모델 정의
from dataclasses import dataclass
from typing import Dict, List, Optional

@dataclass
class MissionOutput:
    """ARGO 미션 출력 데이터 클래스"""
    # 교사용 정보 (웹 대시보드용)
    teacher_version: Dict
    # 학생용 정보 (모바일 앱용)
    student_version: Dict
    # 메타데이터 (API 응답용)
    metadata: Dict

@dataclass
class ProcessedSpot:
    """전처리된 스팟 데이터"""
    name: str
    location: str
    description: str
    gps: List[float]
    safety_score: float
    education_link: str
    category: str
    detailed_class: str
    grade_level: int

@dataclass
class SearchResult:
    """검색 결과 데이터"""
    spot: Dict
    relevance_score: float
    vector_similarity: Optional[float] = None
    grade_bonus: float = 0.0
    safety_weight: float = 1.0

@dataclass
class MissionRequest:
    """미션 생성 요청 데이터"""
    location: str
    grade: int = 5
    group_size: int = 4
    duration_minutes: int = 30
    mission_type: str = "퀴즈"
    context: str = ""