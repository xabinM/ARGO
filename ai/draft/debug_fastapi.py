# debug_fastapi.py - FastAPI LLM 디버깅
"""
FastAPI 서버에서 LLM 호출 실패 원인 찾기
production_ready_fastapi.py의 lifespan 함수를 수정하여 상세 디버깅
"""

import os
import logging
from dotenv import load_dotenv
import openai

load_dotenv()

def debug_llm_initialization():
    """LLM 초기화 과정 상세 디버깅"""
    print("🔍 FastAPI LLM 초기화 디버깅")
    print("=" * 50)
    
    # 1. 환경변수 확인
    gms_key = os.getenv('GMS_API_KEY')
    openai_key = os.getenv('OPENAI_API_KEY')
    gms_base_url = os.getenv('GMS_BASE_URL', 'https://gms.ssafy.io/gmsapi/api.openai.com/v1')
    
    print(f"🔑 GMS_API_KEY: {'있음' if gms_key else '없음'}")
    print(f"🔑 OPENAI_API_KEY: {'있음' if openai_key else '없음'}")
    print(f"🌐 GMS_BASE_URL: {gms_base_url}")
    
    # 2. API 키 선택 로직 테스트
    api_keys = [
        os.getenv("OPENAI_API_KEY"),
        os.getenv("GMS_API_KEY"), 
        os.getenv("API_KEY")
    ]
    
    selected_key = next((key for key in api_keys if key), None)
    print(f"🎯 선택된 키: {'GMS' if selected_key == gms_key else 'OpenAI' if selected_key == openai_key else 'None'}")
    
    if not selected_key:
        print("❌ API 키가 선택되지 않았습니다!")
        return False
    
    # 3. 클라이언트 초기화 테스트
    try:
        # FastAPI와 동일한 로직
        base_url = (
            os.getenv("GMS_BASE_URL", "https://gms.ssafy.io/gmsapi/api.openai.com/v1")
            if "GMS" in os.environ
            else "https://api.openai.com/v1"
        )
        
        print(f"🔗 사용할 base_url: {base_url}")
        print(f"🔍 'GMS' in os.environ: {'GMS' in os.environ}")
        print(f"🔍 'GMS_API_KEY' in os.environ: {'GMS_API_KEY' in os.environ}")
        
        client = openai.OpenAI(api_key=selected_key, base_url=base_url)
        print("✅ 클라이언트 생성 성공")
        
        # 4. API 호출 테스트
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[{"role": "user", "content": "Hello"}],
            max_tokens=5
        )
        print("✅ API 호출 성공")
        print(f"📝 응답: {response.choices[0].message.content}")
        
        # 5. 실제 퀴즈 생성 프롬프트 테스트
        quiz_prompt = """초등학교 5학년용 삼지선다 퀴즈를 만드세요.

장소: 경복궁 근정전
설명: 조선시대 정전으로 중요한 행사를 거행하던 곳

형식:
문제: [질문]
1) [선택지1] 2) [선택지2] 3) [선택지3]
정답: [번호]
해설: [설명]"""

        quiz_response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": "당신은 초등학생 교육 전문가입니다."},
                {"role": "user", "content": quiz_prompt}
            ],
            max_tokens=400,
            temperature=0.2
        )
        
        print("✅ 퀴즈 생성 테스트 성공")
        print(f"📝 퀴즈 응답 (처음 200자):")
        print(quiz_response.choices[0].message.content[:200])
        
        return True
        
    except Exception as e:
        print(f"❌ 오류 발생: {e}")
        print(f"🔍 오류 타입: {type(e).__name__}")
        
        # 상세 오류 정보
        if hasattr(e, 'response'):
            print(f"🔍 HTTP 상태: {e.response.status_code if hasattr(e.response, 'status_code') else 'N/A'}")
            print(f"🔍 응답 내용: {e.response.text if hasattr(e.response, 'text') else 'N/A'}")
        
        return False

def test_environment_detection():
    """환경 감지 로직 테스트"""
    print("\n🧪 환경 감지 로직 테스트")
    print("-" * 30)
    
    # 현재 환경변수 상태
    env_vars = dict(os.environ)
    gms_related = {k: v for k, v in env_vars.items() if 'GMS' in k.upper()}
    
    print("🔍 GMS 관련 환경변수:")
    for key, value in gms_related.items():
        print(f"   {key}: {value[:20]}..." if len(value) > 20 else f"   {key}: {value}")
    
    # FastAPI의 base_url 선택 로직 검증
    condition_check = "GMS" in os.environ
    print(f"\n🎯 'GMS' in os.environ: {condition_check}")
    
    if condition_check:
        selected_url = os.getenv("GMS_BASE_URL", "https://gms.ssafy.io/gmsapi/api.openai.com/v1")
        print(f"✅ GMS URL 선택: {selected_url}")
    else:
        selected_url = "https://api.openai.com/v1"
        print(f"✅ OpenAI URL 선택: {selected_url}")
    
    return selected_url

if __name__ == "__main__":
    print("🚀 FastAPI LLM 디버깅 시작\n")
    
    # 환경 감지 테스트
    detected_url = test_environment_detection()
    
    # LLM 초기화 테스트  
    success = debug_llm_initialization()
    
    print(f"\n🎯 결론:")
    print(f"   환경 감지: {detected_url}")
    print(f"   LLM 초기화: {'성공' if success else '실패'}")
    
    if not success:
        print(f"\n💡 해결 방법:")
        print(f"   1. .env 파일에서 GMS_API_KEY 확인")
        print(f"   2. FastAPI 서버 재시작")
        print(f"   3. production_ready_fastapi.py의 lifespan 함수 디버깅 추가")