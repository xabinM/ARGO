# quiz/__init__.py - 간소화된 퀴즈 모듈 초기화
"""
🎯 Rapid 퀴즈 생성 모듈

포함된 구성요소:
- RapidQuizGenerator: 통합 퀴즈 생성기
"""

from .rapid_complete_generator import RapidQuizGenerator

__all__ = [
    "RapidQuizGenerator"
]

# 버전 정보
__version__ = "1.0.0-rapid"