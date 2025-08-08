# generate_quiz_database.py - 기본 문제 DB 대량 생성
"""
🏭 ARGO 문제 데이터베이스 생성기

기능:
1. expanded_educational_spots.py 기반 스팟별 문제 생성
2. 교육과정 연계 적용 (education_curriculum_integration.py)
3. 학년별(1-6) × 스팟당 3문제 = 약 1,800개 문제 생성
4. JSON 파일로 저장하여 FastAPI에서 활용

실행: python generate_quiz_database.py
예상 소요시간: 20-30분 (API 호출 제한에 따라)
"""

import json
import os
import time
import logging
from datetime import datetime
from typing import List, Dict, Optional
from pathlib import Path
import openai
from dotenv import load_dotenv

# 기존 모듈 import
from expanded_educational_spots import EXPANDED_EDUCATIONAL_SPOTS
from education_curriculum_integration import EducationCurriculumIntegrator

# === 환경 설정 ===
load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[
        logging.FileHandler("logs/quiz_generation.log", encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# === 전역 변수 ===
openai_client = None
curriculum_integrator = None
generation_stats = {
    "total_requested": 0,
    "total_generated": 0,
    "llm_generated": 0,
    "fallback_generated": 0,
    "failed_generation": 0,
    "start_time": "",
    "end_time": "",
    "processing_time_seconds": 0
}

# === OpenAI 클라이언트 설정 ===
def setup_openai_client():
    """OpenAI/GMS 클라이언트 초기화"""
    global openai_client
    
    # API 키 우선순위
    api_keys = [
        ("GMS_API_KEY", "https://gms.ssafy.io/gmsapi/api.openai.com/v1"),
        ("OPENAI_API_KEY", "https://api.openai.com/v1"),
        ("API_KEY", "https://gms.ssafy.io/gmsapi/api.openai.com/v1"),
    ]
    
    for key_name, base_url in api_keys:
        api_key = os.getenv(key_name)
        if api_key and len(api_key.strip()) >= 20:
            try:
                openai_client = openai.OpenAI(
                    api_key=api_key.strip(),
                    base_url=base_url
                )
                
                # 연결 테스트
                test_response = openai_client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[{"role": "user", "content": "test"}],
                    max_tokens=5,
                    timeout=10
                )
                
                logger.info(f"✅ {key_name} API 연결 성공")
                return True
                
            except Exception as e:
                logger.warning(f"⚠️ {key_name} 연결 실패: {e}")
                continue
    
    logger.error("❌ 사용 가능한 API 키 없음")
    return False

# === 교육과정 연계 시스템 초기화 ===
def setup_curriculum_integrator():
    """교육과정 연계 시스템 초기화"""
    global curriculum_integrator
    
    try:
        curriculum_integrator = EducationCurriculumIntegrator()
        logger.info("📚 교육과정 연계 시스템 활성화")
        return True
    except Exception as e:
        logger.warning(f"⚠️ 교육과정 시스템 오류: {e}")
        return False

# === 퀴즈 생성 함수들 ===
def create_enhanced_prompt(spot_info: Dict, grade: int) -> str:
    """교육과정 연계 프롬프트 생성"""
    location = spot_info.get("메인장소", "")
    spot_name = spot_info.get("세부스팟", "")
    description = spot_info.get("설명", "")
    
    # 교육과정 연계 시도
    if curriculum_integrator:
        try:
            return curriculum_integrator.generate_curriculum_enhanced_prompt(
                location, spot_name, grade, description
            )
        except Exception as e:
            logger.debug(f"교육과정 프롬프트 생성 실패: {e}")
    
    # 기본 프롬프트
    keywords = ", ".join(spot_info.get("교육키워드", [])[:3])
    
    if grade <= 2:
        difficulty = "매우 쉬운"
        conditions = "짧고 간단한 단어만 사용하세요."
    elif grade <= 4:
        difficulty = "적당한"
        conditions = "교과서 수준의 어휘를 사용하세요."
    else:
        difficulty = "심화"
        conditions = "교육과정에 맞는 분석적 사고가 필요한 문제를 만드세요."
    
    return f"""초등학교 {grade}학년용 {difficulty} 수준의 삼지선다 퀴즈를 만드세요.

장소: {spot_name}
설명: {description}
키워드: {keywords}

조건:
- {conditions}
- 정확히 3개의 선택지
- 명확한 정답 1개
- 현장학습에 적합한 관찰형 문제

출력 형식:
문제: [질문]
1) [선택지1] 2) [선택지2] 3) [선택지3]
정답: [번호]
해설: [간단한 설명]"""

def parse_llm_response(llm_response: str) -> Optional[Dict]:
    """LLM 응답을 구조화된 퀴즈로 파싱"""
    import re
    
    try:
        lines = [line.strip() for line in llm_response.strip().split('\n') if line.strip()]
        full_text = ' '.join(lines)
        
        parsed_data = {
            "question": "",
            "choices": [],
            "correct_index": 0,
            "explanation": ""
        }
        
        # 문제 추출
        question_patterns = [r"문제\s*[:：]\s*(.+)", r"Q\s*[:：]\s*(.+)"]
        for pattern in question_patterns:
            match = re.search(pattern, full_text)
            if match:
                parsed_data["question"] = match.group(1).strip()
                break
        
        # 선택지 추출
        choice_patterns = [
            r"1\)\s*([^2]+?)\s*2\)\s*([^3]+?)\s*3\)\s*([^정답]+?)(?=정답|해설|$)",
            r"①\s*([^②]+?)\s*②\s*([^③]+?)\s*③\s*([^정답]+?)(?=정답|해설|$)"
        ]
        
        for pattern in choice_patterns:
            match = re.search(pattern, full_text, re.DOTALL)
            if match:
                parsed_data["choices"] = [
                    match.group(1).strip(),
                    match.group(2).strip(),
                    match.group(3).strip()
                ]
                break
        
        # 정답 추출
        answer_patterns = [r"정답\s*[:：]\s*(\d+)", r"답\s*[:：]\s*(\d+)"]
        for pattern in answer_patterns:
            match = re.search(pattern, full_text)
            if match:
                answer_num = int(match.group(1))
                if 1 <= answer_num <= 3:
                    parsed_data["correct_index"] = answer_num - 1
                break
        
        # 해설 추출
        explanation_patterns = [
            r"해설\s*[:：]\s*(.+?)(?=\n|$)",
            r"설명\s*[:：]\s*(.+?)(?=\n|$)"
        ]
        for pattern in explanation_patterns:
            match = re.search(pattern, full_text, re.DOTALL)
            if match:
                parsed_data["explanation"] = match.group(1).strip()
                break
        
        # 유효성 검사
        if (parsed_data["question"] and 
            len(parsed_data["choices"]) == 3 and 
            0 <= parsed_data["correct_index"] <= 2 and
            all(choice.strip() for choice in parsed_data["choices"]) and
            parsed_data["explanation"]):
            
            return parsed_data
        else:
            return None
            
    except Exception as e:
        logger.error(f"파싱 오류: {e}")
        return None

def generate_fallback_quiz(spot_info: Dict, grade: int) -> Dict:
    """폴백 퀴즈 생성"""
    spot_name = spot_info.get("이름", spot_info.get("세부스팟", "스팟"))
    keywords = spot_info.get("교육키워드", ["역사", "문화"])
    
    if grade <= 2:
        return {
            "question": f"{spot_name}은 어디에 있을까요?",
            "choices": ["서울", "부산", "제주도"],
            "correct_index": 0,
            "explanation": f"{spot_name}은 서울에 있는 소중한 곳이에요."
        }
    elif grade <= 4:
        keyword = keywords[0] if keywords else "역사적인 것"
        return {
            "question": f"{spot_name}에서 볼 수 있는 것은 무엇인가요?",
            "choices": [keyword, "현대식 건물", "놀이기구"],
            "correct_index": 0,
            "explanation": f"{spot_name}에서는 {keyword}을 볼 수 있습니다."
        }
    else:
        return {
            "question": f"{spot_name}의 특별한 점은 무엇인가요?",
            "choices": ["문화유산으로서의 가치", "현대적 편의시설", "상업적 목적"],
            "correct_index": 0,
            "explanation": f"{spot_name}은 우리나라의 소중한 문화유산으로 역사적, 교육적 가치가 큽니다."
        }

def generate_single_quiz(spot_info: Dict, grade: int, quiz_number: int = 1) -> Dict:
    """단일 퀴즈 생성 (LLM 또는 폴백)"""
    generation_method = "fallback"
    
    if openai_client:
        try:
            prompt = create_enhanced_prompt(spot_info, grade)
            
            # 다양성을 위해 temperature 조정
            temperature = 0.3 + (quiz_number * 0.1) if quiz_number > 1 else 0.3
            
            response = openai_client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {
                        "role": "system",
                        "content": f"당신은 초등학교 {grade}학년 교육 전문가입니다. 매번 다른 관점에서 흥미로운 삼지선다 퀴즈를 만드세요."
                    },
                    {
                        "role": "user", 
                        "content": f"{prompt}\n\n참고: 이것은 {quiz_number}번째 문제이므로 이전과 다른 관점으로 출제해주세요."
                    }
                ],
                max_tokens=400,
                temperature=min(temperature, 0.7),
                timeout=15
            )
            
            llm_response = response.choices[0].message.content.strip()
            parsed_quiz = parse_llm_response(llm_response)
            
            if parsed_quiz:
                generation_method = "llm"
                generation_stats["llm_generated"] += 1
                return {**parsed_quiz, "generation_method": generation_method}
            else:
                logger.warning("LLM 응답 파싱 실패, 폴백 사용")
                
        except Exception as e:
            logger.error(f"LLM 생성 실패: {e}")
    
    # 폴백 퀴즈 생성
    fallback_quiz = generate_fallback_quiz(spot_info, grade)
    generation_stats["fallback_generated"] += 1
    return {**fallback_quiz, "generation_method": "fallback"}

# === 데이터베이스 생성 메인 함수 ===
def generate_quiz_database():
    """전체 퀴즈 데이터베이스 생성"""
    logger.info("🏭 문제 데이터베이스 생성 시작")
    
    generation_stats["start_time"] = datetime.now().isoformat()
    
    # 디렉토리 생성
    Path("data").mkdir(exist_ok=True)
    Path("logs").mkdir(exist_ok=True)
    
    quiz_database = []
    
    total_spots = len(EXPANDED_EDUCATIONAL_SPOTS)
    logger.info(f"📊 총 {total_spots}개 스팟 처리 예정")
    
    for spot_idx, spot_info in enumerate(EXPANDED_EDUCATIONAL_SPOTS, 1):
        spot_name = spot_info.get("세부스팟", f"스팟{spot_idx}")
        location = spot_info.get("메인장소", "")
        
        logger.info(f"🎯 진행: {spot_idx}/{total_spots} - {location} > {spot_name}")
        
        # 학년별 적합도 확인
        suitable_grades = spot_info.get("학년적합도", [1,2,3,4,5,6])
        
        for grade in suitable_grades:
            # 스팟당 3문제 생성
            for quiz_num in range(1, 4):
                generation_stats["total_requested"] += 1
                
                try:
                    quiz_data = generate_single_quiz(spot_info, grade, quiz_num)
                    
                    # 메타데이터 추가
                    quiz_entry = {
                        "quiz_id": f"{spot_idx:03d}_{grade}_{quiz_num}",
                        "spot_id": spot_idx,
                        "spot_name": spot_name,
                        "location": location,
                        "grade": grade,
                        "quiz_number": quiz_num,
                        **quiz_data,
                        "created_at": datetime.now().isoformat(),
                        "spot_metadata": {
                            "description": spot_info.get("설명", "")[:100],
                            "keywords": spot_info.get("교육키워드", [])[:5],
                            "coordinates": {
                                "latitude": spot_info.get("위도", 0.0),
                                "longitude": spot_info.get("경도", 0.0)
                            }
                        }
                    }
                    
                    # 교육과정 메타데이터 추가
                    if curriculum_integrator:
                        try:
                            educational_metadata = curriculum_integrator.get_educational_metadata(
                                location, spot_name, grade
                            )
                            quiz_entry["educational_metadata"] = educational_metadata
                        except Exception as e:
                            logger.debug(f"교육과정 메타데이터 생성 실패: {e}")
                    
                    quiz_database.append(quiz_entry)
                    generation_stats["total_generated"] += 1
                    
                except Exception as e:
                    logger.error(f"퀴즈 생성 실패 - {location}>{spot_name} (학년:{grade}, 번호:{quiz_num}): {e}")
                    generation_stats["failed_generation"] += 1
                
                # API 제한 방지를 위한 딜레이
                if openai_client and generation_stats["total_requested"] % 10 == 0:
                    time.sleep(1)  # 10개마다 1초 대기
        
        # 진행상황 저장 (50개 스팟마다)
        if spot_idx % 50 == 0:
            save_progress(quiz_database, spot_idx)
    
    # 최종 저장
    save_final_database(quiz_database)
    
    generation_stats["end_time"] = datetime.now().isoformat()
    start_time = datetime.fromisoformat(generation_stats["start_time"])
    end_time = datetime.fromisoformat(generation_stats["end_time"])
    generation_stats["processing_time_seconds"] = int((end_time - start_time).total_seconds())
    
    # 결과 로그
    log_generation_results(quiz_database)

def save_progress(quiz_database: List[Dict], progress_idx: int):
    """중간 진행상황 저장"""
    progress_file = f"data/quiz_database_progress_{progress_idx}.json"
    
    try:
        with open(progress_file, 'w', encoding='utf-8') as f:
            json.dump({
                "progress": f"{progress_idx}/{len(EXPANDED_EDUCATIONAL_SPOTS)}",
                "total_generated": len(quiz_database),
                "quiz_database": quiz_database
            }, f, ensure_ascii=False, indent=2)
        
        logger.info(f"💾 진행상황 저장: {progress_file}")
        
    except Exception as e:
        logger.error(f"진행상황 저장 실패: {e}")

def save_final_database(quiz_database: List[Dict]):
    """최종 데이터베이스 저장"""
    
    # 1. 메인 데이터베이스 파일
    main_db_file = "data/heritage_quiz_database.json"
    
    try:
        with open(main_db_file, 'w', encoding='utf-8') as f:
            json.dump({
                "metadata": {
                    "version": "1.0",
                    "created_at": datetime.now().isoformat(),
                    "total_quizzes": len(quiz_database),
                    "total_spots": len(set(q["spot_id"] for q in quiz_database)),
                    "grade_distribution": {
                        str(grade): len([q for q in quiz_database if q["grade"] == grade])
                        for grade in range(1, 7)
                    },
                    "generation_stats": generation_stats
                },
                "quizzes": quiz_database
            }, f, ensure_ascii=False, indent=2)
        
        logger.info(f"✅ 메인 데이터베이스 저장 완료: {main_db_file}")
        
    except Exception as e:
        logger.error(f"데이터베이스 저장 실패: {e}")
    
    # 2. 백업 파일 (압축된 버전)
    backup_file = f"data/quiz_db_backup_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
    
    try:
        # 간소화된 백업 (메타데이터 제외)
        simplified_db = []
        for quiz in quiz_database:
            simplified_quiz = {
                "quiz_id": quiz["quiz_id"],
                "spot_name": quiz["spot_name"],
                "location": quiz["location"],
                "grade": quiz["grade"],
                "question": quiz["question"],
                "choices": quiz["choices"],
                "correct_index": quiz["correct_index"],
                "explanation": quiz["explanation"]
            }
            simplified_db.append(simplified_quiz)
        
        with open(backup_file, 'w', encoding='utf-8') as f:
            json.dump(simplified_db, f, ensure_ascii=False, indent=2)
        
        logger.info(f"💾 백업 파일 생성: {backup_file}")
        
    except Exception as e:
        logger.warning(f"백업 파일 생성 실패: {e}")

def log_generation_results(quiz_database: List[Dict]):
    """생성 결과 로그"""
    
    logger.info("🎉 문제 데이터베이스 생성 완료!")
    logger.info("=" * 50)
    logger.info(f"📊 생성 통계:")
    logger.info(f"   요청된 문제 수: {generation_stats['total_requested']}")
    logger.info(f"   성공 생성 수: {generation_stats['total_generated']}")
    logger.info(f"   LLM 생성: {generation_stats['llm_generated']}")
    logger.info(f"   폴백 생성: {generation_stats['fallback_generated']}")
    logger.info(f"   실패: {generation_stats['failed_generation']}")
    logger.info(f"   소요 시간: {generation_stats['processing_time_seconds']}초")
    
    # 학년별 분포
    grade_distribution = {}
    location_distribution = {}
    
    for quiz in quiz_database:
        grade = quiz["grade"]
        location = quiz["location"]
        
        grade_distribution[grade] = grade_distribution.get(grade, 0) + 1
        location_distribution[location] = location_distribution.get(location, 0) + 1
    
    logger.info(f"📚 학년별 분포:")
    for grade in sorted(grade_distribution.keys()):
        logger.info(f"   {grade}학년: {grade_distribution[grade]}문제")
    
    logger.info(f"📍 장소별 분포 (상위 5개):")
    top_locations = sorted(location_distribution.items(), key=lambda x: x[1], reverse=True)[:5]
    for location, count in top_locations:
        logger.info(f"   {location}: {count}문제")
    
    # 성공률 계산
    success_rate = (generation_stats['total_generated'] / generation_stats['total_requested']) * 100 if generation_stats['total_requested'] > 0 else 0
    logger.info(f"✅ 전체 성공률: {success_rate:.1f}%")

# === 메인 실행 ===
def main():
    """메인 실행 함수"""
    print("🏭 ARGO 문제 데이터베이스 생성기 시작")
    print("=" * 50)
    
    # 1. 환경 설정 확인
    if not setup_openai_client():
        logger.warning("⚠️ LLM API 없음. 폴백 모드로만 생성됩니다.")
        response = input("계속 진행하시겠습니까? (y/N): ")
        if response.lower() != 'y':
            logger.info("🚫 생성 작업 취소")
            return
    
    # 2. 교육과정 연계 설정
    setup_curriculum_integrator()
    
    # 3. 예상 생성량 안내
    total_spots = len(EXPANDED_EDUCATIONAL_SPOTS)
    estimated_quizzes = 0
    
    for spot in EXPANDED_EDUCATIONAL_SPOTS:
        suitable_grades = spot.get("학년적합도", [1,2,3,4,5,6])
        estimated_quizzes += len(suitable_grades) * 3  # 스팟당 3문제
    
    logger.info(f"📊 예상 생성량: 약 {estimated_quizzes}개 문제")
    logger.info(f"⏱️ 예상 소요시간: {estimated_quizzes//60 + 1}분 (API 제한에 따라 변동)")
    
    response = input("생성을 시작하시겠습니까? (y/N): ")
    if response.lower() != 'y':
        logger.info("🚫 생성 작업 취소")
        return
    
    # 4. 데이터베이스 생성 실행
    try:
        generate_quiz_database()
        logger.info("🎉 모든 작업 완료! 생성된 파일을 확인하세요.")
        logger.info("   📁 data/heritage_quiz_database.json")
        
    except KeyboardInterrupt:
        logger.info("🛑 사용자에 의해 중단됨")
    except Exception as e:
        logger.error(f"❌ 예상치 못한 오류: {e}")
        import traceback
        logger.error(traceback.format_exc())

if __name__ == "__main__":
    main()