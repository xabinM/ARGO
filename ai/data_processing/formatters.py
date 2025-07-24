"""
데이터 포맷 변환기
API 데이터 → RAG 파이프라인 형식 변환
"""
from typing import List, Dict
from datetime import datetime
import logging

from utils.logging_utils import setup_logger

logger = setup_logger(__name__)

class HeritageDataFormatter:
    """문화재 데이터 포맷 변환기"""
    
    def __init__(self):
        pass
    
    def convert_api_data_to_rag_format(self, api_data: List[Dict]) -> Dict:
        """API 데이터를 RAG 파이프라인 형식으로 변환"""
        
        logger.info("🔄 API 데이터 → RAG 형식 변환 시작")
        
        rag_format = {
            "메타데이터": {
                "수집일시": datetime.now().isoformat(),
                "총_개수": len(api_data),
                "데이터소스": "국가유산청 실제 API",
                "변환일시": datetime.now().isoformat()
            },
            "스팟": []  # RAG 파이프라인이 기대하는 키
        }
        
        for item in api_data:
            spot = self._convert_single_item(item)
            if spot:
                rag_format["스팟"].append(spot)
        
        logger.info(f"✅ 변환 완료: {len(rag_format['스팟'])}개 스팟")
        return rag_format
    
    def _convert_single_item(self, api_item: Dict) -> Dict:
        """단일 API 아이템을 RAG 스팟 형식으로 변환"""
        
        try:
            spot = {
                "이름": api_item['문화재명'],
                "위치": f"{api_item['지역']} {api_item['시군구']}".strip(),
                "설명": self._generate_description(api_item),
                "GPS": self._format_gps(api_item),
                "지정종목": api_item['지정종목'],
                "상세분류": api_item.get('문화재명_한자', ''),
                "교육과정_연계": self._determine_education_link(api_item),
                
                # 추가 메타데이터
                "관리기관": api_item.get('관리기관', ''),
                "등록일": api_item.get('등록일', ''),
                "지정번호": api_item.get('지정번호', '')
            }
            
            return spot
            
        except Exception as e:
            logger.warning(f"아이템 변환 실패: {api_item.get('문화재명', 'Unknown')} - {e}")
            return None
    
    def _format_gps(self, api_item: Dict) -> List:
        """GPS 좌표 포맷팅"""
        lat = api_item.get('위도')
        lon = api_item.get('경도')
        
        if lat and lon:
            return [lat, lon]
        return []
    
    def _generate_description(self, heritage_item: Dict) -> str:
        """문화재 정보로부터 교육용 설명 생성"""
        
        name = heritage_item['문화재명']
        category = heritage_item['지정종목']
        location = f"{heritage_item['지역']} {heritage_item['시군구']}"
        admin = heritage_item.get('관리기관', '')
        
        # 기본 설명 템플릿
        description = f"{name}은(는) {category}로 지정된 문화유산입니다. "
        description += f"{location}에 위치하고 있으며"
        
        if admin:
            description += f", {admin}에서 관리하고 있습니다."
        else:
            description += "."
        
        # 지정종목에 따른 추가 설명
        description += self._get_category_description(category)
        
        # 지역별 특색 추가
        description += self._get_location_description(location)
        
        return description
    
    def _get_category_description(self, category: str) -> str:
        """지정종목에 따른 설명 추가"""
        
        if "국보" in category:
            return " 국보는 우리나라 문화재 중 가장 중요하고 귀중한 것들입니다."
        elif "보물" in category:
            return " 보물은 역사적, 예술적으로 중요한 가치를 가진 문화재입니다."
        elif "사적" in category:
            return " 사적은 역사적 사건이 일어났거나 중요한 유적이 있는 곳입니다."
        elif "명승" in category:
            return " 명승은 경치가 아름답고 역사적 가치가 있는 곳입니다."
        elif "천연기념물" in category:
            return " 천연기념물은 학술적 가치가 높은 동식물이나 지질현상입니다."
        elif "무형문화재" in category:
            return " 무형문화재는 전통적인 기술이나 예능을 의미합니다."
        else:
            return ""
    
    def _get_location_description(self, location: str) -> str:
        """지역별 특색 설명 추가"""
        
        if "서울" in location:
            return " 조선시대의 역사와 문화를 느낄 수 있는 곳입니다."
        elif "경주" in location:
            return " 신라 천년의 역사가 담긴 곳입니다."
        elif "부여" in location or "공주" in location:
            return " 백제의 찬란한 문화를 보여주는 곳입니다."
        elif "경기" in location:
            return " 수도권의 중요한 문화유산입니다."
        elif "강원" in location:
            return " 자연과 어우러진 아름다운 문화유산입니다."
        elif "전라" in location or "전북" in location or "전남" in location:
            return " 호남 지역의 독특한 문화적 특색을 보여줍니다."
        elif "경상" in location or "경북" in location or "경남" in location:
            return " 영남 지역의 오랜 역사를 간직한 곳입니다."
        elif "충청" in location or "충북" in location or "충남" in location:
            return " 우리나라 중부 지역의 문화적 중심지입니다."
        elif "제주" in location:
            return " 제주도만의 독특한 문화적 특징을 보여줍니다."
        else:
            return ""
    
    def _determine_education_link(self, heritage_item: Dict) -> str:
        """교육과정 연계 정보 자동 생성"""
        
        category = heritage_item['지정종목']
        name = heritage_item['문화재명']
        
        # 문화재 이름 기반 분류
        if any(keyword in name for keyword in ['궁', '전', '문', '성', '관']):
            return "사회 5학년 2학기 - 조선시대 정치와 사회"
        elif any(keyword in name for keyword in ['