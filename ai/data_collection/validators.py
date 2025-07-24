"""
API 동작 검증 및 테스트
"""
import requests
import xml.etree.ElementTree as ET
import json
import time
from datetime import datetime
from typing import Dict, List, Tuple
import logging

from config.api_config import API_ENDPOINTS, API_PARAMS
from utils.logging_utils import setup_logger

logger = setup_logger(__name__)

class APIValidator:
    """API 동작 검증기"""
    
    def __init__(self):
        self.validated_apis = {}
        self.failed_apis = {}
        
    def test_single_api_thoroughly(self, api_name: str, base_url: str, test_params: dict = None) -> bool:
        """하나의 API를 철저히 테스트"""
        
        logger.info(f"🔍 [{api_name}] 상세 검증")
        
        if test_params is None:
            test_params = API_PARAMS["default"]
        
        try:
            # 1. 기본 연결 테스트
            response = requests.get(base_url, timeout=10)
            logger.info(f"📊 응답 상태: {response.status_code}")
            logger.info(f"📏 응답 크기: {len(response.content)} bytes")
            logger.info(f"🔧 Content-Type: {response.headers.get('content-type', 'Unknown')}")
            
            if response.status_code != 200:
                logger.error(f"❌ 기본 연결 실패: {response.status_code}")
                self.failed_apis[api_name] = {
                    "오류": "HTTP 연결 실패",
                    "상태코드": response.status_code,
                    "응답": response.text[:200]
                }
                return False
            
            # 2. 파라미터 포함 테스트
            param_response = requests.get(base_url, params=test_params, timeout=10)
            logger.info(f"📊 파라미터 응답: {param_response.status_code}")
            
            # 3. 응답 내용 분석
            content = param_response.text if param_response.status_code == 200 else response.text
            
            if content.strip().startswith('<?xml') or content.strip().startswith('<'):
                logger.info("✅ XML 형식 응답 확인")
                success = self._analyze_xml_response(content, api_name)
            elif content.strip().startswith('{') or content.strip().startswith('['):
                logger.info("✅ JSON 형식 응답 확인")
                success = self._analyze_json_response(content, api_name)
            else:
                logger.warning("❌ 알 수 없는 응답 형식")
                logger.debug(f"응답 미리보기: {content[:200]}")
                success = False
            
            if success:
                self.validated_apis[api_name] = {
                    "URL": base_url,
                    "상태": "정상",
                    "응답형식": "XML" if "xml" in content.lower() else "기타",
                    "테스트일시": datetime.now().isoformat()
                }
                logger.info(f"✅ {api_name} 검증 완료")
                return True
            else:
                logger.error(f"❌ {api_name} 검증 실패")
                return False
                
        except requests.exceptions.Timeout:
            logger.error(f"⏰ 타임아웃: {api_name}")
            self.failed_apis[api_name] = {"오류": "타임아웃"}
            return False
            
        except requests.exceptions.ConnectionError:
            logger.error(f"🔌 연결 오류: {api_name}")
            self.failed_apis[api_name] = {"오류": "연결 실패"}
            return False
            
        except Exception as e:
            logger.error(f"❌ 예상치 못한 오류: {e}")
            self.failed_apis[api_name] = {"오류": str(e)}
            return False
    
    def _analyze_xml_response(self, xml_content: str, api_name: str) -> bool:
        """XML 응답 상세 분석"""
        try:
            root = ET.fromstring(xml_content)
            
            logger.info(f"🔧 XML 루트 태그: {root.tag}")
            
            # 데이터 구조 분석
            children = list(root)
            logger.info(f"🔧 직계 자식 수: {len(children)}")
            
            # 주요 정보 확인
            total_cnt = root.find('.//totalCnt')
            if total_cnt is not None:
                logger.info(f"📊 전체 데이터 수: {total_cnt.text}")
            
            # 아이템 확인
            items = root.findall('.//item')
            if items:
                logger.info(f"📦 발견된 아이템: {len(items)}개")
                
                if len(items) > 0:
                    first_item = items[0]
                    item_fields = [child.tag for child in first_item]
                    logger.info(f"🏷️ 아이템 필드들: {item_fields[:10]}...")
                    
                return True
            else:
                logger.warning("❌ 아이템 데이터 없음")
                return False
                
        except ET.ParseError as e:
            logger.error(f"❌ XML 파싱 오류: {e}")
            return False
    
    def _analyze_json_response(self, json_content: str, api_name: str) -> bool:
        """JSON 응답 상세 분석"""
        try:
            data = json.loads(json_content)
            logger.info(f"🔧 JSON 최상위 키: {list(data.keys()) if isinstance(data, dict) else '리스트 형태'}")
            return True
        except json.JSONDecodeError as e:
            logger.error(f"❌ JSON 파싱 오류: {e}")
            return False
    
    def validate_all_heritage_apis(self) -> Dict:
        """모든 문화재 API 검증"""
        
        logger.info("🔍 전체 문화재 API 검증 시작")
        
        working_apis = []
        
        for api_name, url in API_ENDPOINTS["heritage"].items():
            logger.info(f"\n⏳ 테스트 중: {api_name}")
            success = self.test_single_api_thoroughly(api_name, url)
            
            if success:
                working_apis.append(api_name)
            
            # API 부하 방지
            time.sleep(1)
        
        return self.generate_validation_report()
    
    def generate_validation_report(self) -> Dict:
        """검증 결과 리포트"""
        
        logger.info("\n" + "=" * 60)
        logger.info("📋 API 검증 최종 리포트")
        logger.info("=" * 60)
        
        logger.info(f"\n✅ 정상 동작 API ({len(self.validated_apis)}개):")
        for api_name, info in self.validated_apis.items():
            logger.info(f"   🟢 {api_name}")
            logger.info(f"      URL: {info['URL']}")
            logger.info(f"      응답: {info['응답형식']}")
        
        logger.info(f"\n❌ 동작 실패 API ({len(self.failed_apis)}개):")
        for api_name, error_info in self.failed_apis.items():
            logger.info(f"   🔴 {api_name}: {error_info.get('오류', '알 수 없음')}")
        
        success_rate = len(self.validated_apis) / (len(self.validated_apis) + len(self.failed_apis)) * 100 if (len(self.validated_apis) + len(self.failed_apis)) > 0 else 0
        
        if self.validated_apis:
            logger.info(f"\n💡 권장사항:")
            logger.info(f"   1. 정상 동작하는 {len(self.validated_apis)}개 API만 사용")
            logger.info(f"   2. 실패한 API들은 일단 제외하고 개발 진행")
            logger.info(f"   3. 나중에 API 키 발급 받아서 재테스트")
        else:
            logger.warning(f"\n⚠️ 경고: 동작하는 API가 없습니다!")
            logger.warning(f"   1. 네트워크 연결 확인")
            logger.warning(f"   2. API 키 발급 필요 여부 확인")
            logger.warning(f"   3. 공공데이터포털에서 직접 확인")
        
        return {
            "working_apis": self.validated_apis,
            "failed_apis": self.failed_apis,
            "success_rate": success_rate
        }

def validate_heritage_apis() -> Dict:
    """문화재 API 검증 실행"""
    validator = APIValidator()
    return validator.validate_all_heritage_apis()

def main():
    """독립 실행용 메인 함수"""
    result = validate_heritage_apis()
    
    if result:
        print(f"\n🎯 최종 결론:")
        print(f"   성공률: {result['success_rate']:.1f}%")
        print(f"   사용 가능한 API: {len(result['working_apis'])}개")
        
        if result['working_apis']:
            print(f"\n▶️ 다음 단계: 동작하는 API로만 RAG 구축")
        else:
            print(f"\n▶️ 다음 단계: API 키 발급 또는 대안 찾기")
    else:
        print(f"\n🚨 네트워크 또는 서버 문제로 검증 실패")

if __name__ == "__main__":
    main()