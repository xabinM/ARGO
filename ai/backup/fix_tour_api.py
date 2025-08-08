# fix_tour_api.py - Tour API 문제 해결 시도
import requests
import json
import os
from dotenv import load_dotenv
import urllib.parse

load_dotenv()

class TourAPIFixer:
    """Tour API 문제 해결 시도"""
    
    def __init__(self):
        self.api_key = os.getenv('TOUR_API_KEY')
        
        # 다양한 베이스 URL 시도
        self.base_urls = [
            "http://apis.data.go.kr/B551011/KorService",
            "https://apis.data.go.kr/B551011/KorService", 
            "http://api.visitkorea.or.kr/openapi/service/rest/KorService",
            "https://api.visitkorea.or.kr/openapi/service/rest/KorService"
        ]
    
    def try_all_combinations(self):
        """모든 조합 시도"""
        
        print("🔧 Tour API 모든 조합 시도")
        print("=" * 50)
        
        # 1. API 키 인코딩/디코딩 확인
        print("1. API 키 상태 확인:")
        print(f"   원본 키: {self.api_key[:20]}...")
        
        # URL 인코딩된 키인지 확인
        try:
            decoded_key = urllib.parse.unquote(self.api_key)
            if decoded_key != self.api_key:
                print(f"   디코딩된 키: {decoded_key[:20]}...")
                print("   ✅ 현재 키가 인코딩된 상태입니다")
            else:
                print("   ℹ️ 현재 키가 디코딩된 상태입니다")
        except:
            print("   ⚠️ 키 상태 확인 실패")
        
        # 2. 다양한 URL + 키 조합 시도
        keys_to_try = [
            ("원본키", self.api_key),
            ("디코딩키", urllib.parse.unquote(self.api_key)),
            ("인코딩키", urllib.parse.quote(self.api_key))
        ]
        
        for key_name, key_value in keys_to_try:
            print(f"\n2. {key_name} 테스트:")
            
            for i, base_url in enumerate(self.base_urls):
                print(f"   URL {i+1}: {base_url}")
                success = self._test_url_with_key(base_url, key_value)
                if success:
                    print(f"   🎉 성공! {key_name} + {base_url}")
                    return base_url, key_value
                else:
                    print(f"   ❌ 실패")
        
        print("\n❌ 모든 조합 실패")
        return None, None
    
    def _test_url_with_key(self, base_url: str, api_key: str) -> bool:
        """특정 URL과 키 조합 테스트"""
        
        # 가장 간단한 API부터 테스트 (지역코드)
        url = f"{base_url}/areaCode"
        params = {
            'serviceKey': api_key,
            'numOfRows': 5,
            'pageNo': 1,
            'MobileOS': 'ETC',
            'MobileApp': 'ARGO_FIX',
            '_type': 'json'
        }
        
        try:
            response = requests.get(url, params=params, timeout=10)
            
            if response.status_code == 200:
                try:
                    data = response.json()
                    if 'response' in data:
                        header = data['response'].get('header', {})
                        if header.get('resultCode') == '0000':
                            return True
                except:
                    pass
            
            return False
            
        except Exception as e:
            return False
    
    def try_different_endpoints(self):
        """다른 엔드포인트들 시도"""
        
        print("\n3. 다른 엔드포인트 시도:")
        
        # 성공한 조합이 있으면 사용, 없으면 기본값
        working_url, working_key = self.try_all_combinations()
        
        if not working_url:
            # 기본값으로 다시 시도
            working_url = "http://apis.data.go.kr/B551011/KorService"
            working_key = self.api_key
        
        endpoints = [
            ("지역코드", "/areaCode"),
            ("서비스분류", "/categoryCode"), 
            ("관광지목록", "/areaBasedList"),
            ("키워드검색", "/searchKeyword"),
            ("상세정보", "/detailCommon")
        ]
        
        working_endpoints = []
        
        for name, endpoint in endpoints:
            print(f"   테스트: {name} ({endpoint})")
            
            url = working_url + endpoint
            params = {
                'serviceKey': working_key,
                'numOfRows': 3,
                'pageNo': 1,
                'MobileOS': 'ETC',
                'MobileApp': 'ARGO_TEST',
                '_type': 'json'
            }
            
            # 관광지목록의 경우 추가 파라미터
            if endpoint == "/areaBasedList":
                params.update({
                    'areaCode': '1',  # 서울
                    'contentTypeId': '12'  # 관광지
                })
            
            # 키워드검색의 경우
            elif endpoint == "/searchKeyword":
                params['keyword'] = '경복궁'
            
            # 상세정보의 경우 (임시 contentId)
            elif endpoint == "/detailCommon":
                params['contentId'] = '264308'  # 경복궁 ID
            
            try:
                response = requests.get(url, params=params, timeout=10)
                
                if response.status_code == 200:
                    try:
                        data = response.json()
                        if 'response' in data:
                            header = data['response'].get('header', {})
                            if header.get('resultCode') == '0000':
                                body = data['response'].get('body', {})
                                total_count = body.get('totalCount', 0)
                                print(f"      ✅ 성공! 데이터 {total_count}개")
                                working_endpoints.append((name, endpoint, url))
                                continue
                
                print(f"      ❌ 실패 (Status: {response.status_code})")
                
            except Exception as e:
                print(f"      ❌ 오류: {e}")
        
        if working_endpoints:
            print(f"\n🎉 동작하는 엔드포인트 발견:")
            for name, endpoint, full_url in working_endpoints:
                print(f"   ✅ {name}: {full_url}")
            
            return working_url, working_key, working_endpoints
        else:
            print(f"\n❌ 동작하는 엔드포인트 없음")
            return None, None, []
    
    def create_working_collector(self):
        """동작하는 수집기 생성"""
        
        working_url, working_key, endpoints = self.try_different_endpoints()
        
        if not working_url:
            print("❌ 동작하는 API 조합을 찾을 수 없습니다")
            return None
        
        # 동작하는 수집기 코드 생성
        collector_code = f'''
# working_heritage_collector.py - 동작 확인된 설정
import requests
import json
from datetime import datetime

class WorkingHeritageCollector:
    def __init__(self):
        self.api_key = "{working_key}"
        self.base_url = "{working_url}"
        
    def collect_seoul_heritage(self):
        """서울 문화재 수집"""
        
        url = f"{{self.base_url}}/areaBasedList"
        params = {{
            'serviceKey': self.api_key,
            'numOfRows': 50,
            'pageNo': 1,
            'MobileOS': 'ETC',
            'MobileApp': 'ARGO_WORKING',
            'areaCode': '1',  # 서울
            'contentTypeId': '14',  # 문화시설
            '_type': 'json'
        }}
        
        try:
            response = requests.get(url, params=params, timeout=15)
            
            if response.status_code == 200:
                data = response.json()
                if 'response' in data:
                    body = data['response'].get('body', {{}})
                    items = body.get('items', {{}})
                    
                    if items and 'item' in items:
                        return items['item']
            
            return []
            
        except Exception as e:
            print(f"수집 실패: {{e}}")
            return []
    
    def save_heritage_data(self):
        """수집 및 저장"""
        
        data = self.collect_seoul_heritage()
        
        if data:
            # RAG 형식으로 변환
            spots = []
            for item in data:
                spot = {{
                    "이름": item.get('title', ''),
                    "설명": item.get('title', '') + " 관련 교육 체험 장소입니다.",
                    "위도": float(item.get('mapy', 0)) if item.get('mapy') else 0,
                    "경도": float(item.get('mapx', 0)) if item.get('mapx') else 0,
                    "주소": item.get('addr1', ''),
                    "위치": "서울특별시",
                    "지정종목": "문화시설",
                    "수집방식": "Tour_API_실제데이터"
                }}
                
                if spot["위도"] and spot["경도"]:
                    spots.append(spot)
            
            # 저장
            rag_data = {{
                "메타데이터": {{
                    "생성일시": datetime.now().isoformat(),
                    "총_스팟수": len(spots),
                    "데이터소스": "Tour_API_실제수집",
                    "API_URL": "{working_url}",
                    "수집성공": True
                }},
                "스팟": spots
            }}
            
            with open("heritage_real_database.json", "w", encoding="utf-8") as f:
                json.dump(rag_data, f, ensure_ascii=False, indent=2)
            
            print(f"✅ 실제 데이터 {{len(spots)}}개 수집 완료!")
            print("📁 파일: heritage_real_database.json")
            return "heritage_real_database.json"
        else:
            print("❌ 데이터 수집 실패")
            return None

if __name__ == "__main__":
    collector = WorkingHeritageCollector()
    collector.save_heritage_data()
'''
        
        with open("working_heritage_collector.py", "w", encoding="utf-8") as f:
            f.write(collector_code)
        
        print(f"\n📁 working_heritage_collector.py 생성됨")
        print(f"💡 실행: python working_heritage_collector.py")
        
        return "working_heritage_collector.py"

def main():
    fixer = TourAPIFixer()
    result = fixer.create_working_collector()
    
    if result:
        print(f"\n🎯 다음 단계:")
        print(f"   1. python working_heritage_collector.py")
        print(f"   2. heritage_real_database.json 확인")
        print(f"   3. RAG 파이프라인에 연결")

if __name__ == "__main__":
    main()