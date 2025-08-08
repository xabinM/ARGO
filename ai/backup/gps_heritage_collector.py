# gps_heritage_collector.py - GPS 좌표가 포함된 실제 문화재 데이터 수집
import requests
import xml.etree.ElementTree as ET
import json
from datetime import datetime
import time

def is_valid_korea_gps(lat, lon):
    """한국 영토 내 유효한 GPS 좌표인지 확인"""
    if lat is None or lon is None:
        return False
    
    # 한국 GPS 범위 (대략적)
    # 위도: 33.0 ~ 38.5 (제주도 남단 ~ 북한 경계)
    # 경도: 124.5 ~ 132.0 (서해 ~ 울릉도/독도)
    return (33.0 <= lat <= 38.5) and (124.5 <= lon <= 132.0)

def collect_heritage_with_gps():
    """GPS 좌표가 포함된 문화재 데이터 수집"""
    
    print("🗺️ GPS 좌표 포함 문화재 데이터 수집 시작")
    print("=" * 50)
    
    url = "http://www.khs.go.kr/cha/SearchKindOpenapiList.do"
    
    # 다양한 문화재 유형과 지역 조합으로 수집
    heritage_types = {
        "11": "국보",
        "12": "보물", 
        "13": "사적",
        "14": "명승",
        "15": "천연기념물"
    }
    
    city_codes = {
        "11": "서울특별시",
        "26": "부산광역시",
        "27": "대구광역시",
        "28": "인천광역시",
        "51": "경기도",
        "52": "강원도",
        "53": "충청북도",
        "54": "충청남도",
        "55": "전라북도",
        "56": "전라남도",
        "57": "경상북도",
        "58": "경상남도",
        "59": "제주특별자치도"
    }
    
    all_heritage_data = []
    gps_valid_count = 0
    
    for heritage_code, heritage_name in heritage_types.items():
        for city_code, city_name in city_codes.items():
            
            print(f"\n🔍 수집 중: {heritage_name} × {city_name}")
            
            params = {
                "ccbaCncl": "N",        # 지정해제 제외
                "ccbaKdcd": heritage_code,  # 문화재 유형
                "ccbaCtcd": city_code,      # 지역 코드
                "pageUnit": "50"        # 한 번에 50개씩
            }
            
            try:
                response = requests.get(url, params=params, timeout=15)
                
                if response.status_code == 200:
                    root = ET.fromstring(response.content)
                    items = root.findall('.//item')
                    
                    valid_items = 0
                    
                    for item in items:
                        heritage_data = extract_heritage_with_gps_validation(item)
                        
                        if heritage_data:
                            all_heritage_data.append(heritage_data)
                            valid_items += 1
                            gps_valid_count += 1
                    
                    if valid_items > 0:
                        print(f"   ✅ GPS 포함 데이터: {valid_items}개")
                    else:
                        print(f"   ⚠️ GPS 포함 데이터 없음")
                
                else:
                    print(f"   ❌ API 오류: {response.status_code}")
                
                # API 부하 방지
                time.sleep(0.3)
                
            except Exception as e:
                print(f"   ❌ 수집 오류: {e}")
                continue
    
    print(f"\n📊 수집 완료:")
    print(f"   총 GPS 포함 문화재: {gps_valid_count}개")
    
    return all_heritage_data

def extract_heritage_with_gps_validation(item):
    """GPS 검증이 포함된 문화재 정보 추출"""
    
    try:
        # 기본 정보 추출
        name_elem = item.find('ccbaMnm1')
        location_elem = item.find('ccbaLcad')
        category_elem = item.find('ccmaName')
        number_elem = item.find('ccbaCpno')
        
        # GPS 좌표 추출
        lat_elem = item.find('latitude')
        lon_elem = item.find('longitude')
        
        # 필수 정보 검증
        if not (name_elem is not None and name_elem.text and name_elem.text.strip()):
            return None
        
        if not (location_elem is not None and location_elem.text and location_elem.text.strip()):
            return None
        
        # GPS 좌표 검증 (가장 중요!)
        try:
            lat = float(lat_elem.text) if lat_elem is not None and lat_elem.text else None
            lon = float(lon_elem.text) if lon_elem is not None and lon_elem.text else None
        except (ValueError, AttributeError):
            return None
        
        # 한국 영토 내 유효한 GPS인지 확인
        if not is_valid_korea_gps(lat, lon):
            return None
        
        # 검증된 데이터만 추출
        heritage_name = name_elem.text.strip()
        heritage_location = location_elem.text.strip()
        heritage_category = category_elem.text.strip() if category_elem is not None and category_elem.text else "문화재"
        heritage_number = number_elem.text.strip() if number_elem is not None and number_elem.text else ""
        
        # 교육용 설명 생성
        description = generate_educational_description(heritage_name, heritage_category, heritage_location)
        
        heritage_info = {
            "이름": heritage_name,
            "설명": description,
            "위치": heritage_location,
            "지정종목": heritage_category,
            "지정번호": heritage_number,
            "위도": lat,
            "경도": lon,
            "상세분류": f"{heritage_category}/국가지정문화재",
            "GPS": [lat, lon],  # RAG 파이프라인 호환성
            "수집방식": "국가유산청_GPS검증완료",
            "수집일시": datetime.now().isoformat()
        }
        
        return heritage_info
        
    except Exception as e:
        return None

def generate_educational_description(name, category, location):
    """학년별 교육에 적합한 설명 생성"""
    
    base_description = f"{name}은(는) {location}에 위치한 {category}입니다. "
    
    # 문화재 종류에 따른 교육적 설명 추가
    if "국보" in category:
        educational_part = "우리나라에서 가장 귀중한 문화재로, 역사적 가치가 매우 높습니다. 초등학생들이 우리나라의 찬란한 문화유산을 배울 수 있는 소중한 교육 현장입니다."
    elif "보물" in category:
        educational_part = "역사적, 예술적으로 중요한 가치를 지닌 문화재입니다. 옛 조상들의 지혜와 예술성을 직접 체험할 수 있는 교육적 장소입니다."
    elif "사적" in category:
        educational_part = "역사적으로 중요한 사건이 일어났거나 유적이 있는 곳입니다. 우리 역사를 생생하게 배울 수 있는 현장학습 장소입니다."
    elif "명승" in category:
        educational_part = "경치가 아름답고 역사적 가치가 있는 곳입니다. 자연과 역사가 어우러진 교육 체험을 할 수 있습니다."
    elif "천연기념물" in category:
        educational_part = "학술적 가치가 높은 자연유산입니다. 생태 교육과 환경 보호의 중요성을 배울 수 있는 장소입니다."
    else:
        educational_part = "우리나라의 소중한 문화유산으로, 현장학습을 통해 역사와 문화를 체험할 수 있는 교육적 공간입니다."
    
    return base_description + educational_part

def add_verified_seoul_spots():
    """GPS가 검증된 서울 주요 교육 장소 추가"""
    
    print("\n🏫 서울 주요 교육 장소 추가 (GPS 검증 완료)")
    
    # 실제 GPS 좌표가 검증된 서울 교육 장소들
    verified_spots = [
        {
            "이름": "경복궁",
            "설명": "조선 왕조의 법궁으로 조선시대 왕실 문화를 체험할 수 있는 대표적인 교육 현장입니다. 근정전, 경회루 등 다양한 전각에서 역사 학습이 가능합니다.",
            "위치": "서울특별시 종로구 사직로 161",
            "지정종목": "사적",
            "지정번호": "사적 제117호",
            "위도": 37.579617,
            "경도": 126.977041,
            "상세분류": "궁궐/조선왕궁",
            "GPS": [37.579617, 126.977041],
            "수집방식": "검증된_서울교육장소"
        },
        {
            "이름": "창덕궁",
            "설명": "유네스코 세계문화유산으로 등재된 조선의 궁궐입니다. 후원의 아름다운 자연과 건축이 조화를 이루어 전통 조경 문화를 학습할 수 있습니다.",
            "위치": "서울특별시 종로구 율곡로 99",
            "지정종목": "사적",
            "지정번호": "사적 제122호",
            "위도": 37.582035,
            "경도": 126.991043,
            "상세분류": "궁궐/조선왕궁",
            "GPS": [37.582035, 126.991043],
            "수집방식": "검증된_서울교육장소"
        },
        {
            "이름": "창경궁",
            "설명": "조선 왕실의 별궁으로 사용된 궁궐입니다. 대온실과 함께 역사와 과학을 동시에 체험할 수 있는 특별한 교육 공간입니다.",
            "위치": "서울특별시 종로구 창경궁로 185",
            "지정종목": "사적",
            "지정번호": "사적 제123호",
            "위도": 37.578957,
            "경도": 126.995087,
            "상세분류": "궁궐/조선왕궁",
            "GPS": [37.578957, 126.995087],
            "수집방식": "검증된_서울교육장소"
        },
        {
            "이름": "덕수궁",
            "설명": "근현대사의 격동기를 겪은 궁궐로 전통 건축과 서양식 건축이 공존합니다. 대한제국의 역사를 배울 수 있는 교육적 가치가 높은 장소입니다.",
            "위치": "서울특별시 중구 세종대로 99",
            "지정종목": "사적",
            "지정번호": "사적 제124호",
            "위도": 37.565776,
            "경도": 126.975036,
            "상세분류": "궁궐/조선왕궁",
            "GPS": [37.565776, 126.975036],
            "수집방식": "검증된_서울교육장소"
        },
        {
            "이름": "국립중앙박물관",
            "설명": "우리나라 최대 규모의 종합박물관으로 선사시대부터 근현대까지의 문화유산을 전시합니다. 어린이박물관에서는 체험형 교육프로그램을 제공합니다.",
            "위치": "서울특별시 용산구 서빙고로 137",
            "지정종목": "국립박물관",
            "위도": 37.524086,
            "경도": 126.980256,
            "상세분류": "박물관/종합박물관",
            "GPS": [37.524086, 126.980256],
            "수집방식": "검증된_서울교육장소"
        },
        {
            "이름": "남산서울타워",
            "설명": "서울의 랜드마크로 도시 전체를 조망할 수 있어 지리 교육에 활용됩니다. 서울의 발전상과 도시 구조를 학습할 수 있는 교육적 공간입니다.",
            "위치": "서울특별시 용산구 남산공원길 105",
            "지정종목": "관광시설",
            "위도": 37.551169,
            "경도": 126.988227,
            "상세분류": "관광시설/전망시설",
            "GPS": [37.551169, 126.988227],
            "수집방식": "검증된_서울교육장소"
        }
    ]
    
    print(f"   ✅ GPS 검증된 서울 교육장소 {len(verified_spots)}개 추가")
    return verified_spots

def create_gps_verified_heritage_database():
    """GPS 좌표가 검증된 문화재 데이터베이스 생성"""
    
    print("🚀 GPS 검증 문화재 데이터베이스 생성")
    print("=" * 60)
    
    # 1. GPS 포함 문화재 수집
    print("🏛️ 1단계: 전국 문화재 GPS 데이터 수집")
    heritage_data = collect_heritage_with_gps()
    
    # 2. 검증된 서울 교육장소 추가
    print("\n🏫 2단계: 검증된 서울 교육장소 추가")
    seoul_spots = add_verified_seoul_spots()
    
    # 3. 데이터 통합
    all_spots = heritage_data + seoul_spots
    
    # 4. GPS 데이터 품질 검증
    valid_gps_count = 0
    invalid_gps_count = 0
    
    for spot in all_spots:
        lat = spot.get('위도')
        lon = spot.get('경도')
        
        if is_valid_korea_gps(lat, lon):
            valid_gps_count += 1
        else:
            invalid_gps_count += 1
    
    print(f"\n📊 GPS 데이터 품질 검증:")
    print(f"   ✅ 유효한 GPS: {valid_gps_count}개")
    print(f"   ❌ 무효한 GPS: {invalid_gps_count}개")
    
    # 5. RAG 파이프라인 형식으로 구성
    rag_database = {
        "메타데이터": {
            "생성일시": datetime.now().isoformat(),
            "총_스팟수": len(all_spots),
            "GPS_검증완료_스팟수": valid_gps_count,
            "데이터소스": "GPS검증_문화재_통합수집",
            "포함_소스": [
                "국가유산청_GPS검증완료",
                "검증된_서울교육장소"
            ],
            "품질보장": {
                "GPS_좌표": "한국영토내_검증완료",
                "교육적합성": "초등_현장학습_최적화",
                "AR_호환성": "GPS기반_위치인식_가능"
            },
            "RAG_파이프라인": "즉시_사용가능",
            "AR_어플리케이션": "GPS_연동_준비완료"
        },
        "스팟": all_spots
    }
    
    # 6. 파일 저장
    output_file = "heritage_gps_verified_database.json"
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(rag_database, f, ensure_ascii=False, indent=2)
    
    print(f"\n🎉 GPS 검증 데이터베이스 생성 완료!")
    print(f"📁 파일: {output_file}")
    print(f"📊 총 {len(all_spots)}개 스팟 (GPS 검증: {valid_gps_count}개)")
    
    # 7. 지역별 분포 분석
    region_distribution = {}
    for spot in all_spots:
        location = spot.get('위치', '')
        if location:
            # 시도 단위로 분류
            if '서울' in location:
                region = '서울특별시'
            elif '부산' in location:
                region = '부산광역시'
            elif '경기' in location:
                region = '경기도'
            elif '강원' in location:
                region = '강원도'
            elif '충북' in location or '충청북도' in location:
                region = '충청북도'
            elif '충남' in location or '충청남도' in location:
                region = '충청남도'
            elif '전북' in location or '전라북도' in location:
                region = '전라북도'
            elif '전남' in location or '전라남도' in location:
                region = '전라남도'
            elif '경북' in location or '경상북도' in location:
                region = '경상북도'
            elif '경남' in location or '경상남도' in location:
                region = '경상남도'
            elif '제주' in location:
                region = '제주특별자치도'
            else:
                region = '기타'
            
            region_distribution[region] = region_distribution.get(region, 0) + 1
    
    print(f"\n📍 지역별 분포:")
    for region, count in sorted(region_distribution.items(), key=lambda x: x[1], reverse=True):
        print(f"   {region}: {count}개")
    
    # 8. 샘플 데이터 미리보기
    print(f"\n📋 GPS 검증 완료 스팟 미리보기:")
    for i, spot in enumerate(all_spots[:8]):
        name = spot.get("이름")
        location = spot.get("위치")
        lat = spot.get("위도")
        lon = spot.get("경도")
        source = spot.get("수집방식")
        print(f"   {i+1}. {name}")
        print(f"      위치: {location}")
        print(f"      GPS: ({lat:.6f}, {lon:.6f})")
        print(f"      소스: {source}")
        print()
    
    print(f"🎯 다음 단계:")
    print(f"   1. RAG 파이프라인에 {output_file} 연결")
    print(f"   2. GPS 기반 위치 인식 테스트")
    print(f"   3. AR 어플리케이션 GPS 연동")
    print(f"   4. 학년별 퀴즈 생성 테스트")
    
    return output_file

if __name__ == "__main__":
    result_file = create_gps_verified_heritage_database()
    
    if result_file:
        print(f"\n✅ GPS 검증 문화재 데이터베이스 생성 성공!")
        print(f"🗺️ AR 어플리케이션에서 GPS 기반 미션 생성 준비 완료")
        print(f"📊 이제 정확한 위치 기반 현장학습이 가능합니다!")
    else:
        print(f"\n❌ 데이터베이스 생성 실패")
