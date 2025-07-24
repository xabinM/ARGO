# rag_pipeline/preprocessor.py - 데이터 전처리
import logging
from typing import Dict, List, Optional

logger = logging.getLogger(__name__)

class ARGODataPreprocessor:
    """ARGO 전용 데이터 전처리기"""
    
    def __init__(self):
        # 초등학생 안전 키워드 필터
        self.safety_keywords = {
            '안전한': 1.0, '보호': 0.8, '주의': 0.6,
            '위험': -0.8, '무서운': -0.6, '혼자': -0.5
        }
        
        # 학년별 어휘 난이도 매핑
        self.grade_vocabulary = {
            3: {'건축물': '건물', '유물': '옛 물건', '문화재': '문화유산'},
            4: {'조성': '만들어짐', '건립': '세워짐'},
            5: {'창건': '처음 만듦', '중건': '다시 만듦'},
            6: {}  # 원문 유지
        }
    
    def preprocess_spot_data(self, spot_data: Dict, target_grade: int = 5) -> Optional[Dict]:
        """스팟 데이터 전처리"""
        try:
            # 1. 기본 정보 추출
            name = spot_data.get('이름', '')
            location = spot_data.get('위치', '')
            description = spot_data.get('설명', '')
            
            # 2. GPS 유효성 검사
            gps = spot_data.get('GPS', [0, 0])
            if not self._is_valid_gps(gps):
                logger.warning(f"잘못된 GPS: {name}")
                return None
            
            # 3. 안전성 점수 계산
            safety_score = self._calculate_safety_score(description)
            if safety_score < 0.6:
                logger.warning(f"안전성 부족: {name}")
                return None
            
            # 4. 학년별 텍스트 조정
            adjusted_description = self._adjust_for_grade(description, target_grade)
            
            # 5. 교육과정 연계 정보 추출
            # education_link = spot_data.get('교육과정_연계', '')
            
            return {
                'name': spot_data.get('이름', ''),
                'location': spot_data.get('위치', ''),
                'category': spot_data.get('지정종목', ''),
                'designation_number': spot_data.get('지정번호', ''),
                'description': adjusted_description,
                'detailed_class': spot_data.get('상세분류', ''),
                'gps': gps,
                'safety_score': safety_score,
                'grade_level': target_grade
            }
            
        except Exception as e:
            logger.error(f"전처리 오류: {e}")
            return None
    
    def _is_valid_gps(self, gps: List) -> bool:
        """GPS 좌표 유효성 검사"""
        if len(gps) != 2:
            return False
        lat, lon = gps
        # 대한민국 대략적 범위
        return (33.0 <= lat <= 39.0) and (124.0 <= lon <= 132.0)
    
    def _calculate_safety_score(self, text: str) -> float:
        """안전성 점수 계산"""
        score = 0.8  # 기본 점수
        text_lower = text.lower()
        
        for keyword, weight in self.safety_keywords.items():
            if keyword in text_lower:
                score += weight * 0.1
        
        return max(0.0, min(1.0, score))
    
    def _adjust_for_grade(self, text: str, grade: int) -> str:
        """학년별 어휘 조정"""
        if grade not in self.grade_vocabulary:
            return text
        
        vocab_map = self.grade_vocabulary[grade]
        for complex_word, simple_word in vocab_map.items():
            text = text.replace(complex_word, simple_word)
        
        return text