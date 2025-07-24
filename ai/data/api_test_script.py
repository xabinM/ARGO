import requests
import json
import xml.etree.ElementTree as ET
from urllib.parse import urlencode
import pandas as pd
import time
from datetime import datetime

class PublicDataAPITester:
    """공공데이터 API 실제 연동 테스트"""
    
    def __init__(self):
        # 이 키들은 실제 신청 후 받아야 함
        self.api_keys = {
            "heritage_info": "YOUR_HERITAGE_API_KEY_HERE",
            "heritage_space": "YOUR_SPACE_API_KEY_HERE"
        }
        
        # API 엔드포인트들
        self.endpoints = {
            "heritage_info": "http://www.cha.go.kr/cha/SearchKindOpenapiList.do",
            "heritage_space": "http://www.cha.go.kr/openapi/selectUnkoInfo.do"
        }
        
        self.test_results = []
    
    def test_heritage_info_api(self, pageIndex=1, pageUnit=10):
        """전국 지정문화재 현황 API 테스트"""
        print("🔍 [테스트 1] 전국 지정문화재 현황 API")
        print("-" * 50)
        
        # API 파라미터 구성
        params = {
            "pageIndex": pageIndex,
            "pageUnit": pageUnit,
            "serviceKey": self.api_keys["heritage_info"]
        }
        
        url = f"{self.endpoints['heritage_info']}?{urlencode(params)}"
        print(f"📡 요청 URL: {url[:100]}...")
        
        try:
            # API 호출
            response = requests.get(url, timeout=10)
            print(f"📊 응답 상태: {response.status_code}")
            print(f"📄 응답 헤더: {dict(response.headers)}")
            
            if response.status_code == 200:
                print("✅ API 호출 성공!")
                
                # XML 응답 파싱 시도
                try:
                    # XML 파싱
                    root = ET.fromstring(response.content)
                    print(f"🔧 XML 루트: {root.tag}")
                    
                    # XML 구조 분석
                    self._analyze_xml_structure(root)
                    
                    # 실제 데이터 추출 시도
                    items = self._extract_heritage_items(root)
                    print(f"📦 추출된 아이템 수: {len(items)}")
                    
                    if items:
                        print("🎯 첫 번째 아이템 예시:")
                        for key, value in items[0].items():
                            print(f"   {key}: {value[:50]}..." if len(str(value)) > 50 else f"   {key}: {value}")
                    
                    self.test_results.append({
                        "api": "heritage_info",
                        "status": "success",
                        "items_count": len(items),
                        "sample_data": items[0] if items else None
                    })
                    
                except ET.ParseError as e:
                    print(f"❌ XML 파싱 오류: {e}")
                    print(f"📄 응답 내용 (처음 500자): {response.text[:500]}")
                    
                    self.test_results.append({
                        "api": "heritage_info", 
                        "status": "xml_parse_error",
                        "error": str(e),
                        "response_preview": response.text[:200]
                    })
                    
            else:
                print(f"❌ API 호출 실패: {response.status_code}")
                print(f"📄 응답 내용: {response.text[:500]}")
                
                self.test_results.append({
                    "api": "heritage_info",
                    "status": "http_error", 
                    "status_code": response.status_code,
                    "error_response": response.text[:200]
                })
                
        except requests.exceptions.RequestException as e:
            print(f"❌ 네트워크 오류: {e}")
            self.test_results.append({
                "api": "heritage_info",
                "status": "network_error",
                "error": str(e)
            })
    
    def test_without_api_key(self):
        """API 키 없이 테스트 (어떤 오류가 나는지 확인)"""
        print("\n🔍 [테스트 2] API 키 없이 호출 테스트")
        print("-" * 50)
        
        # API 키 없이 호출
        url = "http://www.cha.go.kr/cha/SearchKindOpenapiList.do?pageIndex=1&pageUnit=5"
        print(f"📡 요청 URL: {url}")
        
        try:
            response = requests.get(url, timeout=10)
            print(f"📊 응답 상태: {response.status_code}")
            print(f"📄 응답 내용: {response.text[:500]}")
            
            # 응답에서 오류 메시지 패턴 찾기
            if "인증키" in response.text or "SERVICE_KEY" in response.text:
                print("💡 → API 키가 필요한 서비스임을 확인")
            elif "정상" in response.text:
                print("💡 → API 키 없이도 동작하는 서비스일 가능성")
            
            self.test_results.append({
                "api": "no_key_test",
                "status": "completed",
                "status_code": response.status_code,
                "needs_key": "인증키" in response.text or "SERVICE_KEY" in response.text
            })
            
        except Exception as e:
            print(f"❌ 오류: {e}")
    
    def test_alternative_endpoints(self):
        """대체 엔드포인트들 테스트"""
        print("\n🔍 [테스트 3] 대체 엔드포인트 테스트") 
        print("-" * 50)
        
        # 다양한 엔드포인트 시도
        alternative_urls = [
            "https://www.heritage.go.kr/heri/cul/search.do",
            "http://www.cha.go.kr/openapi/service/HeritageService/getCulturalSiteList",
            "https://api.visitkorea.or.kr/openapi/service/rest/KorService/searchStay",
        ]
        
        for i, url in enumerate(alternative_urls, 1):
            print(f"\n📡 [{i}] 테스트 URL: {url}")
            
            try:
                response = requests.get(url, timeout=5)
                print(f"   상태: {response.status_code}")
                print(f"   응답 크기: {len(response.content)} bytes")
                
                if response.status_code == 200:
                    content_preview = response.text[:200].replace('\n', ' ')
                    print(f"   내용 미리보기: {content_preview}...")
                    
            except Exception as e:
                print(f"   오류: {e}")
    
    def _analyze_xml_structure(self, root):
        """XML 구조 분석"""
        print(f"🔧 XML 구조 분석:")
        print(f"   루트 태그: {root.tag}")
        print(f"   루트 속성: {root.attrib}")
        
        # 하위 요소들 탐색
        children = list(root)
        print(f"   직계 자식 수: {len(children)}")
        
        if children:
            for child in children[:3]:  # 처음 3개만
                print(f"   └─ {child.tag}: {child.text[:30] if child.text else 'None'}...")
    
    def _extract_heritage_items(self, root):
        """문화재 아이템 추출"""
        items = []
        
        # 일반적인 XML 패턴들 시도
        possible_item_tags = ['item', 'list', 'data', 'row', 'record']
        
        for tag in possible_item_tags:
            elements = root.findall(f".//{tag}")
            if elements:
                print(f"💡 '{tag}' 태그에서 {len(elements)}개 아이템 발견")
                
                for elem in elements[:3]:  # 처음 3개만 처리
                    item = {}
                    for child in elem:
                        item[child.tag] = child.text
                    
                    if item:  # 비어있지 않은 아이템만 추가
                        items.append(item)
                break
        
        return items
    
    def generate_test_report(self):
        """테스트 결과 리포트 생성"""
        print("\n" + "="*60)
        print("📊 공공데이터 API 테스트 결과 리포트")
        print("="*60)
        
        for i, result in enumerate(self.test_results, 1):
            print(f"\n[{i}] {result['api']} API:")
            print(f"   상태: {result['status']}")
            
            if result['status'] == 'success':
                print(f"   ✅ 성공 - {result['items_count']}개 아이템 수집")
            elif result['status'] == 'xml_parse_error':
                print(f"   ⚠️ XML 파싱 오류 - {result['error']}")
            elif result['status'] == 'http_error':
                print(f"   ❌ HTTP 오류 - 상태코드 {result['status_code']}")
            elif result['status'] == 'network_error':
                print(f"   ❌ 네트워크 오류 - {result['error']}")
        
        print(f"\n📋 총 {len(self.test_results)}개 테스트 완료")
        print(f"🕐 테스트 완료 시간: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # 다음 단계 권장사항
        print(f"\n💡 다음 단계 권장사항:")
        print(f"   1. 공공데이터포털에서 실제 API 키 신청")
        print(f"   2. 성공한 API 엔드포인트 우선 활용") 
        print(f"   3. 응답 데이터 구조 기반으로 파싱 로직 구현")
        print(f"   4. RAG 파이프라인에 실제 데이터 통합")

def run_comprehensive_api_test():
    """종합적인 API 테스트 실행"""
    
    print("🚀 공공데이터 API 실제 연동 테스트 시작")
    print("=" * 60)
    
    tester = PublicDataAPITester()
    
    # 1. 메인 API 테스트
    tester.test_heritage_info_api()
    
    # 2. API 키 없이 테스트
    tester.test_without_api_key()
    
    # 3. 대체 엔드포인트 테스트
    tester.test_alternative_endpoints()
    
    # 4. 결과 리포트
    tester.generate_test_report()
    
    return tester.test_results

# 즉시 실행 가능한 간단한 테스트
def quick_test():
    """즉시 실행 가능한 빠른 테스트"""
    print("⚡ 빠른 API 연결 테스트")
    print("-" * 30)
    
    test_urls = [
        "http://www.cha.go.kr/cha/SearchKindOpenapiList.do?pageIndex=1&pageUnit=5",
        "https://www.heritage.go.kr",
        "https://www.data.go.kr/data/15034324/openapi.do"
    ]
    
    for url in test_urls:
        try:
            response = requests.get(url, timeout=5)
            print(f"✅ {url[:50]}... → {response.status_code}")
        except Exception as e:
            print(f"❌ {url[:50]}... → {e}")

if __name__ == "__main__":
    # 먼저 빠른 테스트 실행
    quick_test()
    print()
    
    # 전체 테스트 실행
    results = run_comprehensive_api_test()
    
    # JSON으로 결과 저장
    with open(f"api_test_results_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json", "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)