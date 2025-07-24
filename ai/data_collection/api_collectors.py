"""
실제 문화재 API 데이터 수집기 - ARGO RAG 파이프라인용
"""
import requests
import xml.etree.ElementTree as ET
import json
import logging
import os
from typing import List, Dict, Optional
from datetime import datetime
import time

logger = logging.getLogger(__name__)

# API 엔드포인트 정의
API_ENDPOINTS = {
    "heritage": {
        "기본목록": "http://www.khs.go.kr/cha/SearchKindOpenapiList.do",
        "상세정보": "http://www.khs.go.kr/cha/SearchKindOpenapiDt.do"
    }
}

class RealAPIDataLoader:
    """실제 API에서 데이터 수집 → ARGO RAG 파이프라인 형식으로 변환"""
    
    def __init__(self, config: Dict = None):
        self.config = config or {
            'api_timeout': 15,
            'api_delay': 0.2,
            'max_pages': 50,
            'items_per_page': 100
        }
        self.working_apis = API_ENDPOINTS["heritage"]
        self.collected_raw_data = []
        self.processed_for_rag = {}
        
        # 데이터 저장 경로 설정
        self.raw_data_dir = "data/raw"
        self.processed_data_dir = "data/processed"
        self._ensure_directory(self.raw_data_dir)
        self._ensure_directory(self.processed_data_dir)
        
        logger.info("RealAPIDataLoader 초기화 완료")
    
    def _ensure_directory(self, directory_path: str) -> None:
        """디렉토리가 없으면 생성"""
        os.makedirs(directory_path, exist_ok=True)
    
    def _save_json_data(self, data, filepath: str) -> bool:
        """JSON 데이터 저장"""
        try:
            directory = os.path.dirname(filepath)
            if directory:
                self._ensure_directory(directory)
            
            with open(filepath, 'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
            
            logger.info(f"💾 파일 저장 완료: {filepath}")
            return True
            
        except Exception as e:
            logger.error(f"❌ 파일 저장 실패: {filepath} - {e}")
            return False
    
    def collect_heritage_data_from_api(self, max_pages: int = 50, items_per_page: int = 100) -> List[Dict]:
        """실제 API에서 문화재 데이터 수집"""
        
        logger.info(f"🚀 실제 API 데이터 수집 시작 (최대 {max_pages}페이지)")
        
        collected_items = []
        
        for page in range(1, max_pages + 1):
            try:
                params = {
                    "pageIndex": page,
                    "pageUnit": items_per_page
                }
                
                response = requests.get(
                    self.working_apis["기본목록"], 
                    params=params, 
                    timeout=self.config.get('api_timeout', 15)
                )
                
                if response.status_code == 200:
                    root = ET.fromstring(response.content)
                    
                    # 전체 개수 확인 (첫 페이지에서만)
                    if page == 1:
                        total_cnt = root.find('totalCnt')
                        if total_cnt is not None:
                            total_count = int(total_cnt.text)
                            logger.info(f"📊 전체 문화재 수: {total_count:,}개")
                            
                            # 실제 필요한 페이지 수 계산
                            actual_max_pages = min(max_pages, (total_count // items_per_page) + 1)
                            logger.info(f"📄 실제 수집할 페이지: {actual_max_pages}페이지")
                    
                    # 아이템 추출
                    items = root.findall('.//item')
                    
                    for item in items:
                        heritage_data = self._extract_heritage_from_xml(item)
                        if heritage_data:
                            collected_items.append(heritage_data)
                    
                    logger.info(f"   페이지 {page}: {len(items)}개 수집 (누적: {len(collected_items)}개)")
                    
                    # 빈 페이지면 중단
                    if len(items) == 0:
                        logger.info(f"빈 페이지 도달 - 수집 중단")
                        break
                    
                    # API 부하 방지
                    time.sleep(self.config.get('api_delay', 0.2))
                    
                else:
                    logger.warning(f"페이지 {page} 실패: {response.status_code}")
                    if response.status_code == 429:  # Too Many Requests
                        logger.info("API 제한 - 잠시 대기")
                        time.sleep(2)
                        continue
                    
            except Exception as e:
                logger.error(f"페이지 {page} 오류: {e}")
                time.sleep(1)
                continue
        
        self.collected_raw_data = collected_items
        logger.info(f"✅ API 데이터 수집 완료: 총 {len(collected_items):,}개")
        
        return collected_items
    
    def _extract_heritage_from_xml(self, item_xml) -> Optional[Dict]:
        """XML에서 문화재 정보 추출"""
        try:
            heritage = {}
            
            # XML에서 안전하게 텍스트 추출하는 헬퍼 함수
            def get_text(tag_name: str) -> str:
                element = item_xml.find(tag_name)
                if element is not None and element.text:
                    return element.text.strip()
                return ""
            
            def get_float(tag_name: str) -> Optional[float]:
                try:
                    text = get_text(tag_name)
                    return float(text) if text else None
                except:
                    return None
            
            # 기본 정보 추출
            heritage['순번'] = get_text('sn')
            heritage['번호'] = get_text('no')
            heritage['지정종목'] = get_text('ccmaName')
            heritage['문화재명'] = get_text('ccbaMnm1')
            heritage['문화재명_한자'] = get_text('ccbaMnm2')
            heritage['지역'] = get_text('ccbaCtcdNm')
            heritage['시군구'] = get_text('ccsiName')
            heritage['관리기관'] = get_text('ccbaAdmin')
            
            # GPS 좌표
            heritage['경도'] = get_float('longitude')
            heritage['위도'] = get_float('latitude')
            
            # 기타 정보
            heritage['등록일'] = get_text('regDt')
            heritage['취소여부'] = get_text('ccbaCncl')
            heritage['종목코드'] = get_text('ccbaKdcd')
            heritage['시도코드'] = get_text('ccbaCtcd')
            heritage['지정번호'] = get_text('ccbaAsno')
            
            # 필수 정보가 있는 경우만 반환
            if heritage['문화재명'] and heritage['지역']:
                return heritage
            
            return None
            
        except Exception as e:
            logger.warning(f"XML 아이템 추출 오류: {e}")
            return None
    
    def convert_to_rag_format(self) -> Dict:
        """수집된 데이터를 ARGO RAG 파이프라인 형식으로 변환"""
        
        logger.info("🔄 ARGO RAG 파이프라인 형식으로 변환 중...")
        
        rag_format = {
            "메타데이터": {
                "수집일시": datetime.now().isoformat(),
                "총_개수": len(self.collected_raw_data),
                "데이터소스": "국가유산청 실제 API",
                "변환일시": datetime.now().isoformat(),
                "파이프라인_버전": "ARGO-v1.1"
            },
            "스팟": []  # ARGO RAG 파이프라인이 기대하는 키
        }
        
        for item in self.collected_raw_data:
            spot = self._convert_api_item_to_spot(item)
            if spot:
                rag_format["스팟"].append(spot)
        
        self.processed_for_rag = rag_format
        logger.info(f"✅ ARGO RAG 형식 변환 완료: {len(rag_format['스팟'])}개 스팟")
        
        return rag_format
    
    def _convert_api_item_to_spot(self, api_item: Dict) -> Optional[Dict]:
        """API 아이템을 RAG 파이프라인 스팟 형식으로 변환"""
        
        try:
            # GPS 좌표 포맷팅
            gps = []
            latitude = api_item.get('위도')
            longitude = api_item.get('경도')
            if latitude and longitude:
                gps = [latitude, longitude]
            
            # RAG 파이프라인에서 사용하는 필드만 포함
            spot = {
                # 임베딩용 + 메타데이터용 공통 필드
                "이름": api_item['문화재명'],
                "위치": f"{api_item['지역']} {api_item.get('시군구', '')}".strip(),
                "지정종목": api_item['지정종목'],
                "설명": self._generate_description(api_item),
                "상세분류": api_item.get('문화재명_한자', ''),
                
                # 임베딩용 추가 필드
                "지정번호": api_item.get('지정번호', ''),
                
                # 메타데이터용 필드 
                "GPS": gps,  # [위도, 경도] 형식
                "위도": latitude,  # 명시적 위도
                "경도": longitude  # 명시적 경도
            }
            
            return spot
            
        except Exception as e:
            logger.warning(f"아이템 변환 실패: {api_item.get('문화재명', 'Unknown')} - {e}")
            return None
    
    def _generate_description(self, heritage_item: Dict) -> str:
        """문화재 정보로부터 간결한 설명 생성 (RAG 파이프라인 최적화)"""
        
        name = heritage_item['문화재명']
        category = heritage_item['지정종목']
        location = f"{heritage_item['지역']} {heritage_item.get('시군구', '')}"
        
        # 간결하고 교육적인 설명 (임베딩 효율성 고려)
        description = f"{name}은(는) {location}에 위치한 {category}입니다. "
        
        # 지정종목에 따른 핵심 특징만 추가
        if "국보" in category:
            description += "우리나라 최고 등급의 문화재로 역사적 가치가 매우 높습니다."
        elif "보물" in category:
            description += "역사적, 예술적으로 중요한 가치를 지닌 문화재입니다."
        elif "사적" in category:
            description += "역사적으로 중요한 사건이 일어난 장소입니다."
        elif "천연기념물" in category:
            description += "학술적 가치가 높은 자연유산입니다."
        elif "명승" in category:
            description += "경치가 아름답고 역사적 가치가 있는 곳입니다."
        else:
            description += "우리나라의 소중한 문화유산입니다."
        
        return description
    
    def save_raw_data(self, filename: str = None) -> str:
        """원본 API 데이터 저장"""
        if not filename:
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"heritage_raw_{timestamp}.json"
        
        filepath = os.path.join(self.raw_data_dir, filename)
        
        raw_data = {
            "메타데이터": {
                "수집일시": datetime.now().isoformat(),
                "총_개수": len(self.collected_raw_data),
                "데이터소스": "국가유산청 실제 API",
                "파이프라인": "ARGO RAG v1.1"
            },
            "원본데이터": self.collected_raw_data
        }
        
        self._save_json_data(raw_data, filepath)
        logger.info(f"💾 원본 데이터 저장: {filepath}")
        return filepath
    
    def save_rag_data(self, filename: str = None) -> str:
        """ARGO RAG 형식 데이터 저장"""
        if not self.processed_for_rag:
            logger.error("변환된 RAG 데이터가 없습니다. convert_to_rag_format()을 먼저 실행하세요.")
            return ""
        
        if not filename:
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"argo_heritage_data_{timestamp}.json"
        
        filepath = os.path.join(self.processed_data_dir, filename)
        self._save_json_data(self.processed_for_rag, filepath)
        
        logger.info(f"💾 ARGO RAG 데이터 저장: {filepath}")
        return filepath
    
    async def collect_and_save(self, max_pages: int = 20) -> str:
        """전체 프로세스: 수집 → 변환 → 저장"""
        
        logger.info(f"🚀 ARGO RAG 파이프라인용 데이터 수집 시작")
        
        # 1. API 데이터 수집
        api_data = self.collect_heritage_data_from_api(max_pages=max_pages)
        
        if not api_data:
            raise Exception("API 데이터 수집 실패")
        
        # 2. 원본 데이터 저장
        raw_file = self.save_raw_data()
        
        # 3. ARGO RAG 형식으로 변환
        rag_data = self.convert_to_rag_format()
        
        # 4. ARGO RAG 데이터 저장
        rag_file = self.save_rag_data()
        
        logger.info(f"✅ ARGO RAG 파이프라인용 데이터 수집 완료")
        logger.info(f"   원본 파일: {raw_file}")
        logger.info(f"   ARGO RAG 파일: {rag_file}")
        
        return rag_file

def main():
    """독립 실행용 메인 함수"""
    import asyncio
    
    async def run():
        # 기본 설정
        config = {
            'api_timeout': 15,
            'api_delay': 0.3,
            'max_pages': 10,   # 테스트용
            'items_per_page': 50
        }
        
        loader = RealAPIDataLoader(config)
        
        try:
            rag_file = await loader.collect_and_save(max_pages=config['max_pages'])
            print(f"🎉 성공! ARGO RAG 파일 생성: {rag_file}")
            print(f"📊 수집된 데이터: {len(loader.collected_raw_data)}개")
            return rag_file
        except Exception as e:
            logger.error(f"데이터 수집 실패: {e}")
            return None
    
    return asyncio.run(run())

if __name__ == "__main__":
    main()