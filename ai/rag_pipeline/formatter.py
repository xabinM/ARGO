# rag_pipeline/formatter.py - 출력 포맷터
from datetime import datetime
from typing import Dict
from .models import MissionOutput

class ARGOOutputFormatter:
    """ARGO 전용 출력 포맷터 - 교사용/학생용 분리"""
    
    def format_for_argo(self, mission_data: Dict, spot_info: Dict, request: Dict) -> MissionOutput:
        """ARGO 프로젝트용 출력 포맷팅 (교사/학생 모두 모바일)"""
        
        # 교사용 버전 (모바일 앱용 - 교사 계정)
        teacher_version = {
            "mission_overview": {
                "id": f"argo_{int(datetime.now().timestamp())}",
                "quality_score": mission_data['quality_score'],
                "estimated_time": f"{request.get('duration_minutes', 30)}분",
                "auto_determined": mission_data.get('auto_determination_reasoning', '') != ''
            },
            # 모바일 친화적으로 간소화된 교사 정보
            "teaching_tips": [
                "미션 전 안전 수칙 확인",
                "팀별 역할 분담 도움", 
                "완료 후 성과 공유"
            ],
            "quality_info": {
                "ai_confidence": mission_data.get('quality_eval', {}).get('등급', 'N/A'),
                "safety_verified": mission_data.get('safety_included', False)
            },
            # 웹 대시보드용 복잡한 정보들 제거 (기존 technical_info, spot_metadata 등)
        }
        
        # 학생용 버전은 기존과 동일
        student_version = {
            "mission_id": teacher_version["mission_overview"]["id"],
            "spot_name": spot_info['name'],
            "location": spot_info['location'],
            "gps": spot_info['gps'],
            "mission_content": self._extract_student_content(mission_data['content']),
            "mission_type": self._get_app_friendly_type(mission_data['mission_type']),
            "estimated_time": teacher_version["mission_overview"]["estimated_time"],
            "team_size": request.get('group_size', 4),
            "safety_notes": ["문화재 보호", "팀 단위 행동", "안전구역 준수"]
        }
        
        # 메타데이터 (API 응답용)
        metadata = {
            "generation_time": datetime.now().isoformat(),
            "pipeline_version": "ARGO-v1.1",  # 버전 업데이트
            "source_spot": spot_info['name'],
            "auto_determination": mission_data.get('auto_determination_reasoning', ''),
            "mission_type_scores": mission_data.get('mission_type_scores', []),
            "performance": {
                "quality_score": mission_data['quality_score'],
                "safety_score": spot_info.get('safety_score', 0.8),
                "grade_appropriateness": True
            }
        }
        
        return MissionOutput(
            teacher_version=teacher_version,
            student_version=student_version,
            metadata=metadata
        )

    def _extract_student_content(self, full_content: str) -> str:
        """학생용 깔끔한 콘텐츠 추출"""
        lines = full_content.split('\n')
        student_lines = []
        
        # 교사용 정보 제외
        skip_patterns = ['⚠️ 안전수칙', '💡 권장사항', '교사', '준비사항']
        
        for line in lines:
            if any(pattern in line for pattern in skip_patterns):
                break
            if line.strip():
                student_lines.append(line)
        
        return '\n'.join(student_lines)
    
    def _get_app_friendly_type(self, mission_type: str) -> str:
        """앱 친화적 미션 타입"""
        type_map = {
            '퀴즈': '🎯 퀴즈 도전',
            '관찰미션': '👀 관찰 탐험',
            '체험미션': '🎭 역할 체험',
            '사진미션': '📸 포토 미션'
        }
        return type_map.get(mission_type, '🎯 미션')