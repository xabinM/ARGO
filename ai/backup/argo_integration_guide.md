
# 🚀 ARGO RAG 파이프라인 통합 가이드

## 📋 1. 데이터베이스 파일 확인
- ✅ verified_heritage_complete_database.json (메인 데이터베이스)
- ✅ sample_quiz_database.json (테스트용 샘플 퀴즈)

## 🔧 2. RAG 파이프라인 초기화 코드

```python
from rag_pipeline import ARGOPipeline, ARGOAPIHandler

# 파이프라인 초기화
pipeline = ARGOPipeline()
await pipeline.initialize("verified_heritage_complete_database.json")

# API 핸들러 초기화 (FastAPI 연동용)
api_handler = ARGOAPIHandler()
await api_handler.initialize_api("verified_heritage_complete_database.json")
```

## 🎯 3. 퀴즈 생성 테스트

```python
# 단일 퀴즈 생성
request = {
    "location": "경복궁",
    "spot_name": "근정전",
    "user_grade": 5,
    "problems_count": 1
}

quiz_result = await pipeline.generate_quiz(request)
print(f"문제: {quiz_result.question}")
print(f"선택지: {quiz_result.choices}")
print(f"정답: {quiz_result.correct_index}")
```

## 🏭 4. 배치 퀴즈 생성 (기본 퀴즈 DB 구축용)

```python
# 전체 스팟에 대해 학년별 퀴즈 생성
batch_request = {
    "location": "서울",
    "grades": [1, 2, 3, 4, 5, 6],
    "quizzes_per_spot": 5,  # 스팟당 5개 문제
    "max_spots": 10
}

batch_result = await api_handler.handle_batch_quiz_request(batch_request)
```

## ⚡ 5. FastAPI 서버 실행

```bash
# 서버 실행
python fastapi_main_server.py

# API 테스트
curl -X POST "http://localhost:8000/generate-quiz" \
  -H "Content-Type: application/json" \
  -d '{
    "location": "경복궁",
    "spot_name": "근정전", 
    "user_grade": 5
  }'
```

## 📊 6. 데이터베이스 구조

### verified_heritage_complete_database.json
```json
{
  "메타데이터": {
    "총_스팟수": 26,
    "GPS_검증": true,
    "AR_호환성": true
  },
  "스팟": [
    {
      "이름": "경복궁",
      "설명": "조선 왕조의 법궁...",
      "위치": "서울특별시 종로구...",
      "GPS": [37.579617, 126.977041],
      "지정종목": "사적",
      "상세분류": "궁궐/조선왕궁"
    }
  ]
}
```

## 🎓 7. 학년별 퀴즈 생성 특징

### 1-2학년 (저학년)
- 시각적이고 단순한 문제
- 기본적인 인식과 구별 중심
- 안전과 예의 중심 내용

### 3-4학년 (중학년)  
- 지역사회와 연관된 문제
- 기본적인 역사 개념 도입
- 비교와 분석 시작

### 5-6학년 (고학년)
- 조선시대 역사 중심
- 복합적 사고 요구
- 종합적 이해와 분석

## 🔄 8. 프로젝트 워크플로우

1. **기본 퀴즈 DB 생성**: 전체 스팟 × 학년별 배치 생성
2. **반 미션 DB**: 교사 요청 시 기본 DB에서 필터링
3. **추가 생성**: 교사 요청 시 RAG로 추가 문제 생성
4. **최종 등록**: 교사 승인 후 반 미션 DB에 등록

## 🧪 9. 테스트 방법

```python
# 전체 테스트 실행
python tests/run_all_tests.py

# 개별 테스트
python tests/test_argo_pipeline.py
python tests/test_fastapi_server.py
```

## 📱 10. 모바일 어플리케이션 연동

### API 엔드포인트
- `POST /generate-quiz`: 단일 퀴즈 생성
- `POST /batch-quiz`: 배치 퀴즈 생성  
- `GET /health`: 서버 상태 확인
- `GET /spots`: 스팟 목록 조회

### 응답 형식
```json
{
  "success": true,
  "data": {
    "backend_format": {
      "problem_type": "QUIZ",
      "question": "경복궁에서 임금이 신하들과 회의하던 건물은?",
      "choices": ["근정전", "경회루", "향원정"],
      "correct_index": 0,
      "explanation": "근정전은 임금이 신하들과 나랏일을 의논하던 정전이에요."
    }
  }
}
```

## 🎯 11. 다음 단계

1. ✅ 검증된 데이터베이스 완성
2. ✅ RAG 파이프라인 테스트  
3. 🔄 대량 퀴즈 DB 생성
4. 🔄 FastAPI 서버 배포
5. 🔄 모바일 앱 연동 테스트
