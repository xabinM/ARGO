
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
