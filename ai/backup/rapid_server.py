# fixed_rapid_server.py - OpenAI 클라이언트 오류 해결
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import asyncio
import logging
import uvicorn
import os
import json
import re
from datetime import datetime
from dotenv import load_dotenv

# ✅ OpenAI 클라이언트 수정 - 최신 버전 호환
try:
    from openai import OpenAI
except ImportError:
    print("❌ OpenAI 라이브러리 설치 필요: pip install openai>=1.0.0")
    OpenAI = None

# 환경변수 로드
load_dotenv()

# 요청/응답 모델
class QuizRequest(BaseModel):
    location: str
    spot_name: str
    user_grade: int = 5
    problems_count: int = 1

class BatchQuizRequest(BaseModel):
    location: str
    grades: List[int] = [3, 4, 5, 6]
    max_spots: int = 5
    quizzes_per_spot: int = 2

class QuizResponse(BaseModel):
    success: bool
    data: Optional[dict] = None
    error: Optional[str] = None
    timestamp: str

# FastAPI 앱 생성
app = FastAPI(
    title="ARGO RAG Quiz API - LLM 연동 (오류 수정)",
    description="초등학생 현장학습 퀴즈 생성 API",
    version="1.0.1-fixed"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 전역 변수
rapid_data = None
initialized = False

# ✅ 수정된 LLM 퀴즈 생성기
class FixedQuizGenerator:
    """OpenAI 클라이언트 오류 수정된 퀴즈 생성기"""
    
    def __init__(self):
        self.spots = []
        self.client = None
        self.setup_llm_client()
        
        # 학년별 프롬프트 최적화
        self.grade_prompts = {
            1: "1학년 수준의 아주 쉬운 단어와 그림이나 색깔 위주로",
            2: "2학년이 이해할 수 있는 쉬운 단어와 모양, 크기 위주로", 
            3: "3학년 수준의 기본적인 역사나 과학 내용으로",
            4: "4학년이 배우는 지역사회와 문화 내용으로",
            5: "5학년 수준의 조선시대 역사와 전통문화 내용으로",
            6: "6학년이 배우는 한국사와 심화된 문화재 내용으로"
        }
    
    def setup_llm_client(self):
        """✅ 수정된 LLM 클라이언트 설정"""
        if not OpenAI:
            logging.warning("⚠️ OpenAI 라이브러리가 설치되지 않았습니다")
            return
            
        api_key = os.getenv('GMS_API_KEY') or os.getenv('OPENAI_API_KEY')
        base_url = os.getenv('GMS_BASE_URL')
        
        if not api_key:
            logging.warning("⚠️ LLM API 키가 설정되지 않았습니다")
            return
        
        try:
            # ✅ 수정: 필수 파라미터만 사용
            client_params = {
                "api_key": api_key
            }
            
            # base_url이 있을 때만 추가 (GMS 사용시)
            if base_url:
                client_params["base_url"] = base_url
            
            self.client = OpenAI(**client_params)
            logging.info("✅ LLM 클라이언트 설정 완료")
            
        except Exception as e:
            logging.error(f"❌ LLM 클라이언트 설정 실패: {e}")
            self.client = None
    
    def initialize(self, spots_data):
        self.spots = spots_data
        return True
    
    async def generate_quiz(self, request):
        """실제 LLM으로 고품질 퀴즈 생성"""
        
        location = request.get("location", "")
        spot_name = request.get("spot_name", "")
        grade = request.get("user_grade", 5)
        
        # 1. 관련 스팟 찾기
        matching_spot = self.find_best_spot(location, spot_name)
        
        if not matching_spot:
            return self.create_fallback_quiz(location, spot_name, grade)
        
        # 2. LLM으로 실제 퀴즈 생성
        if self.client:
            try:
                quiz_data = await self.generate_with_llm(matching_spot, grade)
                if quiz_data:
                    return quiz_data
            except Exception as e:
                logging.error(f"❌ LLM 퀴즈 생성 실패: {e}")
        
        # 3. LLM 실패시 향상된 fallback
        return self.create_enhanced_fallback(matching_spot, grade)
    
    def find_best_spot(self, location, spot_name):
        """최적 스팟 찾기"""
        
        # 정확한 이름 매칭 우선
        for spot in self.spots:
            spot_name_clean = spot.get("이름", "").lower()
            if (spot_name.lower() in spot_name_clean or 
                spot_name_clean in spot_name.lower()):
                return spot
        
        # 위치 기반 매칭
        for spot in self.spots:
            spot_location = (spot.get("주소", "") + " " + spot.get("이름", "")).lower()
            if location.lower() in spot_location:
                return spot
        
        return self.spots[0] if self.spots else None
    
    async def generate_with_llm(self, spot_data, grade):
        """실제 LLM으로 퀴즈 생성"""
        
        spot_name = spot_data.get("이름", "")
        description = spot_data.get("설명", "")
        grade_instruction = self.grade_prompts.get(grade, "초등학생 수준으로")
        
        # 고품질 프롬프트 구성
        prompt = f"""당신은 초등학교 현장학습 전문 교육자입니다. 다음 장소에 대한 {grade}학년용 삼지선다 퀴즈를 만들어주세요.

장소: {spot_name}
설명: {description}

요구사항:
1. {grade_instruction} 문제를 만들어주세요
2. 현장에서 직접 관찰하거나 체험할 수 있는 내용
3. 정답이 명확하고 교육적 가치가 있는 문제
4. 3개의 선택지 중 1개만 정답
5. 간단하고 이해하기 쉬운 해설

출력 형식:
문제: [구체적이고 흥미로운 문제]
1) [오답 - 그럴듯하지만 틀린 선택지]
2) [정답 - 명확하고 교육적인 선택지]  
3) [오답 - 그럴듯하지만 틀린 선택지]
정답: 2
해설: [왜 정답인지 간단명료한 설명]"""

        try:
            response = self.client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {"role": "system", "content": "당신은 창의적이고 전문적인 초등교육 전문가입니다."},
                    {"role": "user", "content": prompt}
                ],
                max_tokens=500,
                temperature=0.7
            )
            
            llm_response = response.choices[0].message.content
            return self.parse_llm_response(llm_response, spot_data, grade)
            
        except Exception as e:
            logging.error(f"❌ LLM API 호출 실패: {e}")
            return None
    
    def parse_llm_response(self, response, spot_data, grade):
        """LLM 응답 파싱"""
        
        try:
            # 기본값
            question = ""
            choices = ["선택지 1", "선택지 2", "선택지 3"]
            correct_index = 0
            explanation = ""
            
            lines = response.strip().split('\n')
            
            for line in lines:
                line = line.strip()
                
                if line.startswith('문제:'):
                    question = line.replace('문제:', '').strip()
                elif line.startswith('1)'):
                    choices[0] = line.replace('1)', '').strip()
                elif line.startswith('2)'):
                    choices[1] = line.replace('2)', '').strip()
                elif line.startswith('3)'):
                    choices[2] = line.replace('3)', '').strip()
                elif line.startswith('정답:'):
                    answer_text = line.replace('정답:', '').strip()
                    try:
                        answer_num = int(answer_text)
                        if 1 <= answer_num <= 3:
                            correct_index = answer_num - 1
                    except:
                        correct_index = 1  # 기본값을 2번으로
                elif line.startswith('해설:'):
                    explanation = line.replace('해설:', '').strip()
            
            # 품질 검증
            if not question or len(question) < 10:
                return None
            
            if not all(len(choice.strip()) > 2 for choice in choices):
                return None
            
            # 성공적으로 파싱된 고품질 퀴즈
            return {
                "problem_type": "QUIZ",
                "question": question,
                "choices": choices,
                "correct_index": correct_index,
                "explanation": explanation or f"{spot_data.get('이름', '')}에 대한 정보입니다.",
                "grade": grade,
                "spot_name": spot_data.get("이름", ""),
                "quality_score": 0.95,  # LLM 생성 고품질
                "generation_time": 2.0,
                "generation_method": "LLM_GPT4O"
            }
            
        except Exception as e:
            logging.error(f"❌ LLM 응답 파싱 실패: {e}")
            return None
    
    def create_enhanced_fallback(self, spot_data, grade):
        """향상된 fallback 퀴즈 (LLM 실패시)"""
        
        spot_name = spot_data.get("이름", "")
        description = spot_data.get("설명", "")
        
        # 스팟별 맞춤 퀴즈 생성
        if "궁" in spot_name:
            question = f"{spot_name}은 조선시대에 어떤 용도로 사용되었을까요?"
            choices = ["왕과 왕실 가족이 생활하는 곳", "백성들이 장사하는 곳", "농사를 짓는 곳"]
            correct_index = 0
            explanation = f"{spot_name}은 조선시대 왕실의 궁궐로 사용되었습니다."
        
        elif "박물관" in spot_name:
            question = f"{spot_name}에서 볼 수 있는 것은 무엇일까요?"
            choices = ["역사 유물과 전시품", "살아있는 동물들", "놀이기구"]
            correct_index = 0
            explanation = f"{spot_name}은 우리나라의 역사와 문화를 보여주는 전시품들을 볼 수 있는 곳입니다."
        
        elif "공원" in spot_name or "동물원" in spot_name:
            question = f"{spot_name}에서 할 수 있는 활동은 무엇일까요?"
            choices = ["자연 관찰과 체험 활동", "컴퓨터 게임", "책 읽기"]
            correct_index = 0
            explanation = f"{spot_name}은 자연을 관찰하고 다양한 체험을 할 수 있는 곳입니다."
        
        else:
            question = f"{spot_name}의 특징으로 옳은 것은?"
            choices = ["교육적 가치가 높다", "아무나 들어갈 수 없다", "최근에 만들어졌다"]
            correct_index = 0
            explanation = f"{spot_name}은 우리나라의 소중한 교육 자원입니다."
        
        return {
            "problem_type": "QUIZ",
            "question": question,
            "choices": choices,
            "correct_index": correct_index,
            "explanation": explanation,
            "grade": grade,
            "spot_name": spot_name,
            "quality_score": 0.7,  # 향상된 fallback
            "generation_time": 0.5,
            "generation_method": "Enhanced_Fallback"
        }
    
    def create_fallback_quiz(self, location, spot_name, grade):
        """기본 fallback"""
        return {
            "problem_type": "QUIZ",
            "question": f"{location or spot_name}에 대한 설명으로 옳은 것은?",
            "choices": ["역사적 의미가 있다", "많은 사람들이 방문한다", "교육적 가치가 높다"],
            "correct_index": 0,
            "explanation": f"{location or spot_name}은 우리나라의 소중한 문화유산입니다.",
            "grade": grade,
            "spot_name": location or spot_name,
            "quality_score": 0.5,
            "generation_time": 0.1,
            "generation_method": "Basic_Fallback"
        }

# 수정된 퀴즈 생성기 인스턴스
quiz_generator = FixedQuizGenerator()

@app.on_event("startup")
async def startup_event():
    """서버 시작시 초기화"""
    global rapid_data, initialized
    
    try:
        logging.basicConfig(level=logging.INFO)
        logger = logging.getLogger(__name__)
        logger.info("🚀 ARGO RAG 수정된 LLM 서버 시작...")
        
        # ✅ 내장 고품질 샘플 데이터 (데이터 파일 없어도 작동)
        builtin_spots = [
            {
                "이름": "경복궁",
                "설명": "조선 왕조의 정궁으로 1395년에 창건된 대표적인 궁궐입니다. 근정전에서는 왕이 신하들과 나라 일을 논의했고, 경회루에서는 연회를 열었습니다.",
                "주소": "서울특별시 종로구 세종로",
                "지정종목": "사적",
                "위도": 37.579617,
                "경도": 126.977041
            },
            {
                "이름": "창덕궁", 
                "설명": "조선시대의 이궁으로 자연과 조화를 이룬 아름다운 궁궐입니다. 유네스코 세계문화유산으로 등재되어 있으며, 후원의 비원이 특히 유명합니다.",
                "주소": "서울특별시 종로구 율곡로",
                "지정종목": "사적",
                "위도": 37.582495,
                "경도": 126.991168
            },
            {
                "이름": "국립중앙박물관",
                "설명": "한국의 역사와 문화를 한눈에 볼 수 있는 대표 박물관입니다. 선사시대부터 근현대까지 다양한 유물과 문화재가 전시되어 있습니다.",
                "주소": "서울특별시 용산구 서빙고로",
                "지정종목": "박물관",
                "위도": 37.524172,
                "경도": 126.980270
            },
            {
                "이름": "남산서울타워",
                "설명": "서울의 상징적인 랜드마크로 남산 정상에 위치한 타워입니다. 서울 전경을 한눈에 볼 수 있는 전망대가 있습니다.",
                "주소": "서울특별시 용산구 남산공원길",
                "지정종목": "관광지",
                "위도": 37.551169,
                "경도": 126.988227
            },
            {
                "이름": "서울대공원",
                "설명": "다양한 동물들을 관찰할 수 있는 동물원과 놀이시설이 있는 대규모 공원입니다. 어린이들의 자연 학습에 최적화된 장소입니다.",
                "주소": "경기도 과천시 대공원광장로",
                "지정종목": "공원",
                "위도": 37.434144,
                "경도": 127.008936
            }
        ]
        
        # 데이터 파일이 있으면 사용, 없으면 내장 데이터 사용
        data_file = os.getenv("RAPID_DATA_FILE", "data/heritage_rapid_database.json")
        
        if os.path.exists(data_file):
            with open(data_file, 'r', encoding='utf-8') as f:
                rapid_data = json.load(f)
            spots = rapid_data.get('스팟', [])
            logger.info(f"✅ 외부 데이터 로드: {len(spots)}개 스팟")
        else:
            spots = builtin_spots
            logger.info(f"✅ 내장 데이터 사용: {len(spots)}개 스팟")
        
        quiz_generator.initialize(spots)
        initialized = True
        
        # API 키 확인
        api_key = os.getenv('GMS_API_KEY') or os.getenv('OPENAI_API_KEY')
        if api_key:
            logger.info("✅ LLM API 키 확인됨")
        else:
            logger.warning("⚠️ LLM API 키가 설정되지 않았습니다 - Fallback 모드로 실행")
        
    except Exception as e:
        logger.error(f"❌ 초기화 실패: {e}")
        raise

@app.get("/")
async def root():
    """API 상태 확인"""
    api_configured = bool(os.getenv('GMS_API_KEY') or os.getenv('OPENAI_API_KEY'))
    
    return {
        "message": "ARGO RAG Quiz API - 수정된 LLM 연동",
        "status": "running" if initialized else "initializing", 
        "llm_mode": "LLM_Connected" if api_configured else "Fallback_Mode",
        "timestamp": datetime.now().isoformat(),
        "version": "1.0.1-fixed",
        "features": {
            "real_llm_generation": api_configured,
            "enhanced_fallback": True,
            "grade_optimization": True,
            "builtin_data": True
        },
        "data_info": "내장 고품질 데이터 사용" if not os.path.exists("data/heritage_rapid_database.json") else "외부 데이터 파일 사용"
    }

@app.post("/quiz/generate", response_model=QuizResponse)
async def generate_quiz(request: QuizRequest):
    """실제 LLM으로 고품질 퀴즈 생성"""
    
    if not initialized:
        raise HTTPException(status_code=503, detail="서버 초기화 중")
    
    try:
        # 요청 검증
        if not request.location.strip():
            raise HTTPException(status_code=400, detail="location 필수")
        
        if not request.spot_name.strip():
            raise HTTPException(status_code=400, detail="spot_name 필수")
        
        if not (1 <= request.user_grade <= 6):
            raise HTTPException(status_code=400, detail="user_grade는 1-6 사이")
        
        # 실제 LLM으로 고품질 퀴즈 생성
        quiz_result = await quiz_generator.generate_quiz({
            "location": request.location,
            "spot_name": request.spot_name,
            "user_grade": request.user_grade,
            "problems_count": request.problems_count
        })
        
        return QuizResponse(
            success=True,
            data={
                "quiz": quiz_result,
                "backend_format": quiz_result,
                "quality_info": {
                    "generation_method": quiz_result.get("generation_method", "Unknown"),
                    "quality_score": quiz_result.get("quality_score", 0.0),
                    "llm_used": quiz_result.get("generation_method") == "LLM_GPT4O"
                }
            },
            timestamp=datetime.now().isoformat()
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logging.error(f"❌ 퀴즈 생성 실패: {e}")
        raise HTTPException(status_code=500, detail=f"퀴즈 생성 실패: {str(e)}")

@app.post("/quiz/batch")
async def generate_batch_quiz(request: BatchQuizRequest):
    """배치 퀴즈 생성"""
    
    if not initialized:
        raise HTTPException(status_code=503, detail="서버 초기화 중")
    
    try:
        total_quizzes = len(request.grades) * request.max_spots * request.quizzes_per_spot
        if total_quizzes > 20:  # LLM 부하 고려
            raise HTTPException(status_code=400, detail="배치 크기 너무 큼 (최대 20개)")
        
        batch_results = []
        
        for grade in request.grades:
            for i in range(min(request.max_spots, request.quizzes_per_spot)):
                quiz_result = await quiz_generator.generate_quiz({
                    "location": request.location,
                    "spot_name": f"{request.location}",
                    "user_grade": grade
                })
                
                batch_results.append(quiz_result)
        
        return QuizResponse(
            success=True,
            data={
                "batch_results": batch_results,
                "total_count": len(batch_results),
                "average_quality": sum(q.get("quality_score", 0) for q in batch_results) / len(batch_results),
                "llm_generated_count": sum(1 for q in batch_results if q.get("generation_method") == "LLM_GPT4O")
            },
            timestamp=datetime.now().isoformat()
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logging.error(f"❌ 배치 퀴즈 생성 실패: {e}")
        raise HTTPException(status_code=500, detail=f"배치 퀴즈 생성 실패: {str(e)}")

@app.get("/health")
async def health_check():
    """상태 확인"""
    
    api_configured = bool(os.getenv('GMS_API_KEY') or os.getenv('OPENAI_API_KEY'))
    
    return {
        "status": "healthy" if initialized else "initializing",
        "timestamp": datetime.now().isoformat(),
        "data_loaded": initialized,
        "version": "1.0.1-fixed",
        "llm_status": {
            "configured": api_configured,
            "mode": "LLM_Connected" if api_configured else "Enhanced_Fallback"
        },
        "quality_features": {
            "real_llm_generation": api_configured,
            "grade_specific_prompts": True,
            "enhanced_fallback": True,
            "spot_matching": True,
            "builtin_data": True
        }
    }

if __name__ == "__main__":
    port = int(os.getenv("RAPID_SERVER_PORT", 8000))
    
    print("🚀 ARGO RAG 수정된 LLM 연동 서버 시작")
    print(f"📍 주소: http://localhost:{port}")
    print(f"📚 API 문서: http://localhost:{port}/docs")
    print(f"🔧 상태 확인: http://localhost:{port}/health")
    print(f"🧠 LLM 연동: {'✅' if os.getenv('GMS_API_KEY') or os.getenv('OPENAI_API_KEY') else '❌ API 키 필요'}")
    print(f"📊 데이터: 내장 고품질 데이터 포함")
    
    uvicorn.run(
        "fixed_rapid_server:app",
        host="0.0.0.0", 
        port=port,
        reload=False,
        log_level="info"
    )