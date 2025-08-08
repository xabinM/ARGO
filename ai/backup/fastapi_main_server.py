# fastapi_main_server_fixed.py - 퀴즈 템플릿 로딩 및 스팟 검색 문제 해결
"""
🐛 문제 해결:
1. 퀴즈 템플릿 0개 로딩 → 정상 로딩
2. 스팟 검색 실패 → 검색 로직 개선
3. 404 에러 → 정확한 매칭
"""

from fastapi import FastAPI, HTTPException, BackgroundTasks
from pydantic import BaseModel
import json
import logging
import os
import uuid
from typing import List, Dict, Optional
from datetime import datetime
import uvicorn
import asyncio

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# FastAPI 앱 생성
app = FastAPI(
    title="ARGO RAG API 서버 - 수정된 버전",
    description="스팟별 세분화 퀴즈 생성 전용 API (버그 수정)",
    version="ultra-rapid-v1.1-fixed",
)


# === 데이터 모델 ===
class QuizGenerationRequest(BaseModel):
    """퀴즈 생성 요청"""

    location: str  # 메인 장소 (예: "경복궁")
    spot_name: str  # 세부 스팟 (예: "근정전")
    user_grade: int  # 학년 (1-6)
    problems_count: int = 1  # 생성할 문제 수


class BatchGenerationRequest(BaseModel):
    """배치 생성 요청"""

    location: str  # 위치 (예: "경복궁")
    grades: List[int]  # 학년 리스트 [1,2,3,4,5,6]
    spots_count: int = 5  # 스팟 개수
    problems_per_spot: int = 2  # 스팟당 문제 개수


class QuizResponse(BaseModel):
    """퀴즈 응답"""

    success: bool
    question: str
    choices: List[str]
    correct_index: int
    explanation: str
    grade: int
    spot_name: str
    location: str


class BatchResponse(BaseModel):
    """배치 응답"""

    batch_id: str
    status: str  # "processing" | "completed" | "failed"
    total_count: int
    completed_count: int = 0
    quizzes: List[QuizResponse] = []


# === 전역 변수 ===
heritage_data: List[Dict] = []
quiz_templates: Dict = {}
batch_tasks: Dict[str, BatchResponse] = {}


# === 데이터 로드 (수정됨) ===
@app.on_event("startup")
async def startup_event():
    """서버 시작 시 데이터 로드 - 수정된 버전"""
    global heritage_data, quiz_templates

    logger.info("🚀 ARGO RAG API 서버 시작 - 수정된 버전")

    try:
        if os.path.exists("heritage_complete_database.json"):
            with open("heritage_complete_database.json", "r", encoding="utf-8") as f:
                data = json.load(f)
                heritage_data = data.get("스팟", [])
                quiz_templates = data.get("퀴즈템플릿", {})

            logger.info(f"✅ 스팟 데이터 로드: {len(heritage_data)}개")
            logger.info(f"✅ 퀴즈 템플릿 로드: {len(quiz_templates)}개 스팟")

            # 🔍 디버깅: 데이터 구조 확인
            if heritage_data:
                logger.info(f"📊 첫번째 스팟 구조: {list(heritage_data[0].keys())}")

            if quiz_templates:
                logger.info(f"📊 퀴즈 템플릿 키들: {list(quiz_templates.keys())}")
            else:
                logger.warning("⚠️ 퀴즈 템플릿이 비어있습니다!")

        else:
            logger.error("❌ heritage_complete_database.json 파일이 없습니다!")
            logger.info("💡 먼저 'python create_spot_database.py' 실행하세요")

    except Exception as e:
        logger.error(f"❌ 데이터 로드 실패: {e}")

    logger.info("🚀 ARGO RAG API 서버 - 수정된 버전")
    logger.info("📋 제공 API:")
    logger.info("   POST /generate-additional-quiz - 퀴즈 생성 (메인 API)")
    logger.info("   POST /batch-generate-by-location - 위치 기반 배치 생성")
    logger.info("   GET  /batch-status/{batch_id} - 배치 생성 상태 확인")
    logger.info("   GET  /health - 서버 상태")


# === 유틸리티 함수 (수정됨) ===
def find_spot_by_name(location: str, spot_name: str) -> Optional[Dict]:
    """메인장소 + 세부스팟으로 정확한 스팟 찾기 - 수정된 로직"""

    logger.info(f"🔍 스팟 검색: location='{location}', spot_name='{spot_name}'")

    for i, spot in enumerate(heritage_data):
        logger.debug(f"   스팟 {i}: {spot.get('이름', 'N/A')}")

        # 1. 정확한 매칭: 메인장소와 세부스팟이 모두 일치
        if spot.get("메인장소") == location and spot.get("세부스팟") == spot_name:
            logger.info(f"✅ 정확한 매칭 발견: {spot.get('이름')}")
            return spot

        # 2. 유연한 매칭: 이름에 포함된 경우
        spot_full_name = spot.get("이름", "")
        if location in spot_full_name and spot_name in spot_full_name:
            logger.info(f"✅ 유연한 매칭 발견: {spot_full_name}")
            return spot

        # 3. 부분 매칭: spot_name만으로도 매칭
        if spot_name in spot_full_name or spot.get("세부스팟") == spot_name:
            logger.info(f"✅ 부분 매칭 발견: {spot_full_name}")
            return spot

    logger.warning(f"❌ 매칭되는 스팟 없음: {location} > {spot_name}")
    return None


def find_spots_by_location(location: str) -> List[Dict]:
    """위치로 관련 스팟들 찾기"""
    matching_spots = []
    for spot in heritage_data:
        if (
            spot.get("메인장소") == location
            or location in spot.get("이름", "")
            or spot.get("위치", "").startswith(location)
        ):
            matching_spots.append(spot)
    return matching_spots


def generate_quiz_from_template(spot_name: str, grade: int) -> Optional[Dict]:
    """템플릿에서 퀴즈 생성 - 수정된 로직"""

    logger.info(f"🎯 퀴즈 생성: spot_name='{spot_name}', grade={grade}")
    logger.debug(f"📊 사용 가능한 템플릿: {list(quiz_templates.keys())}")

    # 1. 직접 매칭
    if spot_name in quiz_templates:
        grade_str = str(grade)
        grade_quiz = quiz_templates[spot_name].get(grade_str)
        if grade_quiz:
            logger.info(f"✅ 템플릿에서 퀴즈 찾음: {spot_name} - 학년 {grade}")
            return grade_quiz
        else:
            logger.warning(f"⚠️ 학년 {grade} 퀴즈 없음, 기본 퀴즈 생성")

    # 2. 부분 매칭 시도
    for template_key in quiz_templates.keys():
        if spot_name in template_key or template_key in spot_name:
            grade_quiz = quiz_templates[template_key].get(grade)
            if grade_quiz:
                logger.info(
                    f"✅ 부분 매칭으로 퀴즈 찾음: {template_key} - 학년 {grade}"
                )
                return grade_quiz

    # 3. 템플릿이 없는 경우 기본 퀴즈 생성
    logger.info(f"📝 기본 퀴즈 생성: {spot_name}")
    return generate_fallback_quiz(spot_name, grade)


def generate_fallback_quiz(spot_name: str, grade: int) -> Dict:
    """기본 퀴즈 생성 (템플릿 없을 때) - 개선된 버전"""

    # 학년별 문제 난이도 조정
    if grade <= 2:
        return {
            "question": f"{spot_name}은 어떤 곳일까요?",
            "choices": ["역사가 있는 곳", "새로 만든 곳", "외국에 있는 곳"],
            "answer": 0,
            "explanation": f"{spot_name}은(는) 우리나라의 소중한 역사적인 곳이에요.",
        }
    elif grade <= 4:
        return {
            "question": f"{spot_name}에 대한 설명으로 맞는 것은?",
            "choices": ["문화유산이다", "현대 건물이다", "외국 문화재다"],
            "answer": 0,
            "explanation": f"{spot_name}은(는) 우리나라의 소중한 문화유산입니다.",
        }
    else:  # 5-6학년
        return {
            "question": f"{spot_name}의 역사적 의미는?",
            "choices": [
                "조선시대 문화를 보여줌",
                "현대 기술을 보여줌",
                "서양 문화를 보여줌",
            ],
            "answer": 0,
            "explanation": f"{spot_name}은(는) 조선시대의 역사와 문화를 보여주는 중요한 문화재입니다.",
        }


# === API 엔드포인트 ===


@app.post("/generate-additional-quiz", response_model=QuizResponse)
async def generate_additional_quiz(request: QuizGenerationRequest):
    """
    퀴즈 생성 (메인 API) - 수정된 버전
    교사의 미션 추가 생성 요청 처리
    """
    logger.info(
        f"🎯 퀴즈 생성 요청: {request.location} > {request.spot_name} (학년: {request.user_grade})"
    )

    # 입력 검증
    if not (1 <= request.user_grade <= 6):
        raise HTTPException(status_code=400, detail="학년은 1-6 사이여야 합니다")

    # 스팟 찾기 (개선된 검색)
    spot = find_spot_by_name(request.location, request.spot_name)
    if not spot:
        # 스팟을 찾을 수 없을 때 사용 가능한 스팟들 알려주기
        available_spots = find_spots_by_location(request.location)
        available_names = [
            s.get("세부스팟", s.get("이름", "")) for s in available_spots[:5]
        ]

        error_msg = (
            f"'{request.location} > {request.spot_name}' 스팟을 찾을 수 없습니다."
        )
        if available_names:
            error_msg += f" 사용 가능한 스팟: {', '.join(available_names)}"

        raise HTTPException(status_code=404, detail=error_msg)

    # 퀴즈 생성
    quiz_data = generate_quiz_from_template(request.spot_name, request.user_grade)
    if not quiz_data:
        raise HTTPException(status_code=500, detail="퀴즈 생성에 실패했습니다")

    # 응답 생성
    response = QuizResponse(
        success=True,
        question=quiz_data["question"],
        choices=quiz_data["choices"],
        correct_index=quiz_data["answer"],
        explanation=quiz_data["explanation"],
        grade=request.user_grade,
        spot_name=request.spot_name,
        location=request.location,
    )

    logger.info(f"✅ 퀴즈 생성 완료: {request.spot_name} (학년 {request.user_grade})")
    return response


@app.post("/batch-generate-by-location", response_model=Dict)
async def batch_generate_by_location(
    request: BatchGenerationRequest, background_tasks: BackgroundTasks
):
    """
    위치 기반 배치 생성
    기본퀴즈DB 생성용
    """
    logger.info(f"🏭 배치 생성 요청: {request.location} (학년: {request.grades})")

    # 입력 검증
    if not all(1 <= grade <= 6 for grade in request.grades):
        raise HTTPException(status_code=400, detail="학년은 1-6 사이여야 합니다")

    # 해당 위치 스팟들 찾기
    location_spots = find_spots_by_location(request.location)
    if not location_spots:
        raise HTTPException(
            status_code=404, detail=f"'{request.location}' 관련 스팟을 찾을 수 없습니다"
        )

    # 제한된 스팟 개수만 처리
    selected_spots = location_spots[: request.spots_count]

    # 배치 작업 생성
    batch_id = str(uuid.uuid4())
    total_count = len(selected_spots) * len(request.grades) * request.problems_per_spot

    batch_task = BatchResponse(
        batch_id=batch_id,
        status="processing",
        total_count=total_count,
        completed_count=0,
        quizzes=[],
    )
    batch_tasks[batch_id] = batch_task

    # 백그라운드에서 배치 처리
    background_tasks.add_task(
        process_batch_generation,
        batch_id,
        selected_spots,
        request.grades,
        request.problems_per_spot,
    )

    logger.info(f"🚀 배치 작업 시작: {batch_id} (총 {total_count}개 예상)")

    return {
        "batch_id": batch_id,
        "status": "processing",
        "total_count": total_count,
        "message": f"{request.location} 지역 {len(selected_spots)}개 스팟 처리 시작",
    }


async def process_batch_generation(
    batch_id: str, spots: List[Dict], grades: List[int], problems_per_spot: int
):
    """배치 생성 백그라운드 처리"""
    logger.info(f"🔄 배치 처리 시작: {batch_id}")

    batch_task = batch_tasks[batch_id]

    try:
        for spot in spots:
            spot_name = (
                spot.get("세부스팟") or spot.get("이름", "").split()[-1]
            )  # 세부스팟명 추출
            location = (
                spot.get("메인장소") or spot.get("이름", "").split()[0]
            )  # 메인장소 추출

            for grade in grades:
                for i in range(problems_per_spot):
                    # 퀴즈 생성
                    quiz_data = generate_quiz_from_template(spot_name, grade)

                    if quiz_data:
                        quiz_response = QuizResponse(
                            success=True,
                            question=quiz_data["question"],
                            choices=quiz_data["choices"],
                            correct_index=quiz_data["answer"],
                            explanation=quiz_data["explanation"],
                            grade=grade,
                            spot_name=spot_name,
                            location=location,
                        )
                        batch_task.quizzes.append(quiz_response)

                    # 진행률 업데이트
                    batch_task.completed_count += 1

                    # CPU 부하 방지를 위한 소량 지연
                    await asyncio.sleep(0.01)

        batch_task.status = "completed"
        logger.info(f"✅ 배치 처리 완료: {batch_id} ({batch_task.completed_count}개)")

    except Exception as e:
        logger.error(f"❌ 배치 처리 실패: {batch_id} - {e}")
        batch_task.status = "failed"


@app.get("/batch-status/{batch_id}")
async def get_batch_status(batch_id: str):
    """배치 생성 상태 확인"""
    if batch_id not in batch_tasks:
        raise HTTPException(status_code=404, detail="배치 작업을 찾을 수 없습니다")

    batch_task = batch_tasks[batch_id]

    progress = 0
    if batch_task.total_count > 0:
        progress = (batch_task.completed_count / batch_task.total_count) * 100

    return {
        "batch_id": batch_id,
        "status": batch_task.status,
        "progress": round(progress, 2),
        "completed": batch_task.completed_count,
        "total": batch_task.total_count,
        "quizzes_count": len(batch_task.quizzes),
    }


@app.get("/health")
async def health_check():
    """서버 상태 확인 - 디버깅 정보 추가"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "loaded_spots": len(heritage_data),
        "quiz_templates": len(quiz_templates),
        "quiz_template_keys": list(quiz_templates.keys()) if quiz_templates else [],
        "sample_spots": [s.get("이름", "") for s in heritage_data[:3]],
        "active_batches": len(
            [b for b in batch_tasks.values() if b.status == "processing"]
        ),
        "version": "ultra-rapid-v1.1-fixed",
    }


@app.get("/debug/spots")
async def debug_spots():
    """디버깅: 스팟 정보 확인"""
    return {
        "total_spots": len(heritage_data),
        "spots_summary": [
            {
                "이름": spot.get("이름", ""),
                "메인장소": spot.get("메인장소", ""),
                "세부스팟": spot.get("세부스팟", ""),
            }
            for spot in heritage_data[:10]  # 처음 10개만
        ],
        "quiz_templates": (
            {
                key: list(value.keys()) if isinstance(value, dict) else value
                for key, value in quiz_templates.items()
            }
            if quiz_templates
            else "No templates loaded"
        ),
    }


@app.get("/")
async def root():
    """루트 엔드포인트"""
    return {
        "message": "🚀 ARGO RAG API 서버 - 수정된 버전",
        "version": "ultra-rapid-v1.1-fixed",
        "feature": "스팟별 정확한 퀴즈 생성 (버그 수정)",
        "fixes": [
            "퀴즈 템플릿 로딩 문제 해결",
            "스팟 검색 로직 개선",
            "404 에러 수정",
            "디버깅 정보 추가",
        ],
        "apis": [
            "POST /generate-additional-quiz - 퀴즈 생성 (메인)",
            "POST /batch-generate-by-location - 배치 생성",
            "GET /batch-status/{batch_id} - 상태 확인",
            "GET /health - 서버 상태",
            "GET /debug/spots - 디버깅 정보",
        ],
    }


# === 서버 실행 ===
if __name__ == "__main__":
    logger.info("🚀 Ultra Rapid RAG API 서버 시작 - 수정된 버전")

    # 데이터 파일 존재 확인
    if not os.path.exists("heritage_complete_database.json"):
        logger.error("❌ heritage_complete_database.json이 없습니다!")
        logger.info("💡 먼저 'python create_spot_database.py'를 실행하세요")
        exit(1)

    uvicorn.run(
        "fastapi_main_server_fixed:app",
        host="0.0.0.0",
        port=8000,
        reload=False,
        log_level="info",
    )
