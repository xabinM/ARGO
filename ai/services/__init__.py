# services/__init__.py - 간소화된 서비스 모듈 초기화
"""
🎯 ARGO AI 서비스 모듈

포함된 서비스:
- QuizService: 퀴즈 생성 서비스  
- SpotService: 스팟 관리 서비스
- PoseService: 포즈 분석 서비스
"""

from .quiz_service import QuizService
from .spot_service import SpotService
from .pose_service import PoseService

__all__ = [
    "QuizService",
    "SpotService", 
    "PoseService"
]

# 버전 정보
__version__ = "2.0.0-rapid"