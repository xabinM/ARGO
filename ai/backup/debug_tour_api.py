# debug_tour_api.py - Tour API 문제 진단
import requests
import json
import os
from dotenv import load_dotenv

load_dotenv()

class TourAPIDebugger:
    """Tour API 문제 진단기"""
    
    def __init__(self):
        self.api_key = os.getenv('TOUR_API_KEY')
        self.base_url = "http://apis.data.go.kr/B551011/KorService"
        
    def debug_api_connection(self):
        """API 연결 상태 진단"""
        
        print("🔍 Tour API 진단 시작")
        print("=" * 50)
        
        # 1. API 키 확인
        print(f"1. API 키 확인:")
        if not self.api_key:
            print("   ❌ API 키가 설정되지 않았습니다!")
            print("   💡 .env 파일에 TOUR_API_KEY=your_key 추가하세요")
            return
        else:
            key_preview = self.api_key[:20] + "..." if len(self.api_key) > 20 else self.api_key
            print(f"   ✅ API 키 확인됨: {key_preview}")
        
        # 2. 기본 API 테스트 (지역코드 조회)
        print(f"\n2. 기본 API 테스트:")
        self._test_area_code()
        
        # 3. 관광지 데이터 테스트
        print(f"\n3. 관광지 데이터 테스트:")
        self._test_tourist_spots()
        
        # 4. 문화시설 데이터 테스트  
        print(f"\n4. 문화시설 데이터 테스트:")
        self._test_cultural_facilities()
    
    def _test_area_code(self):
        """지역코드 API 테스트"""
        
        url = f"{self.base_url}/areaCode"
        params = {
            'serviceKey': self.api_key,
            'numOfRows': 5,
            'pageNo': 1,
            'MobileOS': 'ETC',
            'MobileApp': 'ARGO_DEBUG',
            '_type': 'json'
        }
        
        try:
            print("   📡 지역코드 API 호출 중...")
            response = requests.get(url, params=params, timeout=10)
            
            print(f"   📊 응답 상태: {response.status_code}")
            print(f"   📏 응답 크기: {len(response.content)} bytes")
            
            if response.status_code == 200:
                try:
                    data = response.json()
                    print(f"   ✅ JSON 파싱 성공")
                    
                    # 응답 구조 분석
                    if 'response' in data:
                        response_data = data['response']
                        header = response_data.get('header', {})
                        body = response_data.get('body', {})
                        
                        print(f"   📋 헤더 정보:")
                        print(f"      - resultCode: {header.get('resultCode')}")
                        print(f"      - resultMsg: {header.get('resultMsg')}")
                        
                        if body and 'items' in body:
                            items = body['items']
                            if items and 'item' in items:
                                item_list = items['item']
                                print(f"   📍 지역 데이터: {len(item_list)}개 확인")
                                
                                # 첫 번째 지역 정보 출력
                                if item_list:
                                    first_area = item_list[0] if isinstance(item_list, list) else item_list
                                    print(f"      예시: {first_area.get('name', 'N/A')} ({first_area.get('code', 'N/A')})")
                            else:
                                print(f"   ⚠️ items 내부에 item 없음: {items}")
                        else:
                            print(f"   ⚠️ body에 items 없음: {body}")
                    else:
                        print(f"   ❌ 응답에 'response' 키 없음")
                        print(f"   📄 실제 응답: {data}")
                        
                except json.JSONDecodeError as e:
                    print(f"   ❌ JSON 파싱 실패: {e}")
                    print(f"   📄 응답 내용: {response.text[:200]}...")
            else:
                print(f"   ❌ HTTP 오류: {response.status_code}")
                print(f"   📄 오류 내용: {response.text[:200]}...")
                
        except Exception as e:
            print(f"   ❌ API 호출 실패: {e}")
    
    def _test_tourist_spots(self):
        """관광지 데이터 테스트"""
        
        url = f"{self.base_url}/areaBasedList"
        params = {
            'serviceKey': self.api_key,
            'numOfRows': 5,
            'pageNo': 1,
            'MobileOS': 'ETC',
            'MobileApp': 'ARGO_DEBUG',
            'arrange': 'P',  # 인기순
            'contentTypeId': '12',  # 관광지
            'areaCode': '1',  # 서울
            '_type': 'json'
        }
        
        try:
            print("   📡 관광지 API 호출 중...")
            response = requests.get(url, params=params, timeout=10)
            
            print(f"   📊 응답 상태: {response.status_code}")
            
            if response.status_code == 200:
                data = response.json()
                
                if 'response' in data:
                    body = data['response'].get('body', {})
                    if body and 'items' in body:
                        items = body['items']
                        if items and 'item' in items:
                            item_list = items['item']
                            count = len(item_list) if isinstance(item_list, list) else 1
                            print(f"   ✅ 관광지 데이터: {count}개 확인")
                            
                            # 첫 번째 관광지 정보
                            first_spot = item_list[0] if isinstance(item_list, list) else item_list
                            print(f"      예시: {first_spot.get('title', 'N/A')}")
                        else:
                            print(f"   ⚠️ 관광지 데이터 없음")
                    else:
                        print(f"   ⚠️ body 또는 items 없음")
                else:
                    print(f"   ❌ response 키 없음")
            else:
                print(f"   ❌ HTTP 오류: {response.status_code}")
                print(f"   📄 오류: {response.text[:200]}...")
                
        except Exception as e:
            print(f"   ❌ 관광지 API 실패: {e}")
    
    def _test_cultural_facilities(self):
        """문화시설 데이터 테스트"""
        
        url = f"{self.base_url}/areaBasedList"
        params = {
            'serviceKey': self.api_key,
            'numOfRows': 5,
            'pageNo': 1,
            'MobileOS': 'ETC',
            'MobileApp': 'ARGO_DEBUG',
            'arrange': 'P',
            'contentTypeId': '14',  # 문화시설
            'areaCode': '1',  # 서울
            '_type': 'json'
        }
        
        try:
            print("   📡 문화시설 API 호출 중...")
            response = requests.get(url, params=params, timeout=10)
            
            print(f"   📊 응답 상태: {response.status_code}")
            
            if response.status_code == 200:
                data = response.json()
                
                if 'response' in data:
                    body = data['response'].get('body', {})
                    if body and 'items' in body:
                        items = body['items']
                        if items and 'item' in items:
                            item_list = items['item']
                            count = len(item_list) if isinstance(item_list, list) else 1
                            print(f"   ✅ 문화시설 데이터: {count}개 확인")
                            
                            # 첫 번째 문화시설 정보
                            first_facility = item_list[0] if isinstance(item_list, list) else item_list
                            print(f"      예시: {first_facility.get('title', 'N/A')}")
                        else:
                            print(f"   ⚠️ 문화시설 데이터 없음")
                    else:
                        print(f"   ⚠️ body 또는 items 없음")
                else:
                    print(f"   ❌ response 키 없음")
            else:
                print(f"   ❌ HTTP 오류: {response.status_code}")
                print(f"   📄 오류: {response.text[:200]}...")
                
        except Exception as e:
            print(f"   ❌ 문화시설 API 실패: {e}")
    
    def generate_working_sample(self):
        """동작하는 샘플 코드 생성"""
        
        print(f"\n" + "=" * 50)
        print("🔧 수정된 샘플 코드:")
        print("=" * 50)
        
        sample_code = '''
# working_tour_api.py - 수정된 Tour API 호출
import requests
import json
import os
from dotenv import load_dotenv

load_dotenv()

def test_working_api():
    api_key = os.getenv('TOUR_API_KEY')
    
    # 지역코드 테스트
    url = "http://apis.data.go.kr/B551011/KorService/areaCode"
    params = {
        'serviceKey': api_key,
        'numOfRows': 10,
        'pageNo': 1,
        'MobileOS': 'ETC',
        'MobileApp': 'ARGO_TEST',
        '_type': 'json'
    }
    
    response = requests.get(url, params=params)
    print(f"Status: {response.status_code}")
    
    if response.status_code == 200:
        data = response.json()
        print("✅ API 연결 성공!")
        return True
    else:
        print(f"❌ API 오류: {response.text}")
        return False

if __name__ == "__main__":
    test_working_api()
'''
        
        with open("working_tour_api.py", "w", encoding="utf-8") as f:
            f.write(sample_code)
        
        print("📁 working_tour_api.py 파일 생성됨")
        print("💡 실행해보세요: python working_tour_api.py")

def main():
    debugger = TourAPIDebugger()
    debugger.debug_api_connection()
    debugger.generate_working_sample()

if __name__ == "__main__":
    main()