import os
import json
import time
import hashlib
import numpy as np
import faiss
import openai
from dataclasses import dataclass
from typing import List, Dict, Optional, Tuple
from datetime import datetime, timedelta
import asyncio
import aiohttp
from concurrent.futures import ThreadPoolExecutor
import logging
from dotenv import load_dotenv

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@dataclass
class MissionRequest:
    """미션 요청 데이터 클래스"""
    location: str
    user_grade: int = 5  # 학년
    group_size: int = 4  # 그룹 크기
    duration_minutes: int = 30  # 활동 시간
    preferred_type: Optional[str] = None  # 선호 미션 타입
    difficulty: str = "medium"  # easy, medium, hard
    context: Optional[str] = None  # 추가 컨텍스트

@dataclass
class MissionResult:
    """미션 결과 데이터 클래스"""
    mission_id: str
    mission_type: str
    content: str
    quality_score: float
    generation_time: float
    source_heritage: List[str]
    metadata: Dict

class CacheManager:
    """캐싱 시스템"""
    
    def __init__(self, cache_dir="cache"):
        self.cache_dir = cache_dir
        os.makedirs(cache_dir, exist_ok=True)
        self.memory_cache = {}
        self.cache_ttl = timedelta(hours=24)
    
    def _get_cache_key(self, query: str, mission_type: str, **kwargs) -> str:
        """캐시 키 생성"""
        cache_data = f"{query}_{mission_type}_{str(sorted(kwargs.items()))}"
        return hashlib.md5(cache_data.encode()).hexdigest()
    
    def get(self, cache_key: str) -> Optional[Dict]:
        """캐시에서 데이터 조회"""
        # 메모리 캐시 확인
        if cache_key in self.memory_cache:
            cached_data, timestamp = self.memory_cache[cache_key]
            if datetime.now() - timestamp < self.cache_ttl:
                logger.info(f"메모리 캐시 히트: {cache_key[:8]}")
                return cached_data
        
        # 파일 캐시 확인
        cache_file = os.path.join(self.cache_dir, f"{cache_key}.json")
        if os.path.exists(cache_file):
            try:
                with open(cache_file, 'r', encoding='utf-8') as f:
                    cached_data = json.load(f)
                    
                timestamp = datetime.fromisoformat(cached_data['timestamp'])
                if datetime.now() - timestamp < self.cache_ttl:
                    logger.info(f"파일 캐시 히트: {cache_key[:8]}")
                    return cached_data['data']
            except:
                pass
        
        return None
    
    def set(self, cache_key: str, data: Dict):
        """캐시에 데이터 저장"""
        # 메모리 캐시
        self.memory_cache[cache_key] = (data, datetime.now())
        
        # 파일 캐시
        cache_file = os.path.join(self.cache_dir, f"{cache_key}.json")
        cache_data = {
            'data': data,
            'timestamp': datetime.now().isoformat()
        }
        
        try:
            with open(cache_file, 'w', encoding='utf-8') as f:
                json.dump(cache_data, f, ensure_ascii=False)
        except:
            logger.warning(f"캐시 저장 실패: {cache_key[:8]}")

class OptimizedFAISS:
    """최적화된 FAISS 인덱스"""
    
    def __init__(self, dimension: int):
        self.dimension = dimension
        self.index = None
        self.index_type = "flat"  # flat, ivf, hnsw
        
    def build_optimized_index(self, embeddings: np.ndarray, index_type: str = "auto"):
        """데이터 크기에 따른 최적 인덱스 선택"""
        n_vectors = embeddings.shape[0]
        
        if index_type == "auto":
            if n_vectors < 1000:
                index_type = "flat"
            elif n_vectors < 100000:
                index_type = "ivf"
            else:
                index_type = "hnsw"
        
        if index_type == "flat":
            self.index = faiss.IndexFlatL2(self.dimension)
            
        elif index_type == "ivf":
            # IVF 인덱스 (중간 규모)
            nlist = min(int(np.sqrt(n_vectors)), 1000)
            quantizer = faiss.IndexFlatL2(self.dimension)
            self.index = faiss.IndexIVFFlat(quantizer, self.dimension, nlist)
            self.index.train(embeddings.astype(np.float32))
            
        elif index_type == "hnsw":
            # HNSW 인덱스 (대규모)
            self.index = faiss.IndexHNSWFlat(self.dimension, 32)
            self.index.hnsw.efConstruction = 200
            self.index.hnsw.efSearch = 50
        
        self.index.add(embeddings.astype(np.float32))
        self.index_type = index_type
        
        logger.info(f"최적화된 {index_type.upper()} 인덱스 구축 완료: {n_vectors}개 벡터")

class MissionQualityEvaluator:
    """미션 품질 평가기"""
    
    def __init__(self, gms_client):
        self.client = gms_client
        
    async def evaluate_mission(self, mission_content: str, mission_type: str) -> Dict:
        """미션 품질 자동 평가"""
        
        evaluation_prompt = f"""다음 초등학생용 {mission_type}의 품질을 평가해주세요.

미션 내용:
{mission_content}

평가 기준:
1. 교육적 가치 (1-10점)
2. 흥미도 (1-10점) 
3. 실행 가능성 (1-10점)
4. 안전성 (1-10점)
5. 명확성 (1-10점)

각 항목별 점수와 간단한 이유, 개선 제안을 JSON 형식으로 반환하세요.

형식:
{{
    "교육적_가치": {{"점수": 8, "이유": "역사적 사실 학습 가능"}},
    "흥미도": {{"점수": 7, "이유": "게임 요소 포함"}},
    "실행_가능성": {{"점수": 9, "이유": "현장에서 쉽게 실행"}},
    "안전성": {{"점수": 10, "이유": "위험 요소 없음"}},
    "명확성": {{"점수": 8, "이유": "지시사항 명확"}},
    "총점": 42,
    "등급": "B+",
    "개선_제안": "더 구체적인 관찰 포인트 추가"
}}"""

        try:
            response = await self._async_gpt_call(evaluation_prompt)
            # JSON 파싱
            eval_result = json.loads(response)
            return eval_result
        except Exception as e:
            logger.error(f"품질 평가 실패: {e}")
            return {"총점": 0, "등급": "F", "오류": str(e)}
    
    async def _async_gpt_call(self, prompt: str) -> str:
        """비동기 GPT 호출"""
        try:
            response = self.client.chat.completions.create(
                model="gpt-4o",
                messages=[
                    {"role": "system", "content": "당신은 교육 전문가입니다. JSON 형식으로만 응답하세요."},
                    {"role": "user", "content": prompt}
                ],
                max_tokens=500,
                temperature=0.3
            )
            return response.choices[0].message.content.strip()
        except Exception as e:
            raise e

class SmartMissionGenerator:
    """스마트 미션 생성기"""
    
    def __init__(self, gms_client):
        self.client = gms_client
        
    def auto_determine_mission_type(self, location: str, heritage_info: List[Dict]) -> str:
        """위치와 문화재 정보 기반 최적 미션 타입 자동 결정"""
        
        # 문화재 특성 분석
        has_architecture = any("궁" in h.get("name", "") or "전" in h.get("name", "") for h in heritage_info)
        has_artifacts = any("상" in h.get("name", "") or "탑" in h.get("name", "") for h in heritage_info)
        has_nature = any("천연" in h.get("category", "") for h in heritage_info)
        
        # 규칙 기반 미션 타입 결정
        if has_architecture:
            return "체험미션"  # 건축물은 체험 위주
        elif has_artifacts:
            return "퀴즈"      # 유물은 지식 퀴즈
        elif has_nature:
            return "관찰미션"  # 자연유산은 관찰
        else:
            return "퀴즈"      # 기본값
    
    def generate_contextual_prompt(self, request: MissionRequest, heritage_info: List[Dict]) -> str:
        """컨텍스트 기반 고급 프롬프트 생성"""
        
        difficulty_map = {
            "easy": "매우 쉬운",
            "medium": "보통",
            "hard": "조금 어려운"
        }
        
        mission_type = request.preferred_type or self.auto_determine_mission_type(request.location, heritage_info)
        
        heritage_context = "\n".join([
            f"- {h.get('name', '')}: {h.get('description', '')[:100]}..."
            for h in heritage_info
        ])
        
        base_prompt = f"""초등학교 {request.user_grade}학년 {request.group_size}명이 {request.location}에서 {request.duration_minutes}분간 수행할 {mission_type}을 만들어주세요.

관련 문화재 정보:
{heritage_context}

요구사항:
- 난이도: {difficulty_map[request.difficulty]}
- 그룹 크기: {request.group_size}명
- 소요 시간: {request.duration_minutes}분
- 학년 수준: {request.user_grade}학년에 적합"""

        if request.context:
            base_prompt += f"\n- 추가 고려사항: {request.context}"
        
        # 미션 타입별 세부 지침
        type_specific = {
            "퀴즈": """
형식:
🎯 퀴즈 제목: [창의적 제목]
📝 문제: [문제 내용]
① [선택지 1]
② [선택지 2]
③ [선택지 3]
✅ 정답: [번호] - [상세 해설]
💡 추가 학습: [관련 지식]
⏰ 예상 시간: {request.duration_minutes}분""",
            
            "관찰미션": """
형식:
🎯 미션 제목: [관찰 미션명]
👀 관찰 대상: [구체적 관찰 포인트]
📝 활동 순서:
1. [1단계]
2. [2단계]
3. [3단계]
📊 기록 방법: [관찰 결과 기록 방식]
🤝 그룹 활동: [협력 방법]
⏰ 소요 시간: {request.duration_minutes}분""",
            
            "체험미션": """
형식:
🎯 미션 제목: [체험 활동명]
🎭 역할 분담: [그룹별 역할]
🎮 체험 활동:
1. [준비 단계]
2. [실행 단계]
3. [마무리 단계]
🏆 성과 목표: [달성해야 할 목표]
📚 학습 효과: [교육적 의미]
⏰ 소요 시간: {request.duration_minutes}분"""
        }
        
        return base_prompt + type_specific.get(mission_type, type_specific["퀴즈"])

class AdvancedRAGPipeline:
    """고도화된 RAG 파이프라인"""
    
    def __init__(self):
        load_dotenv()
        
        # GMS 클라이언트
        self.client = openai.OpenAI(
            api_key=os.getenv('GMS_API_KEY'),
            base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1"
        )
        
        # 컴포넌트 초기화
        self.cache_manager = CacheManager()
        self.quality_evaluator = MissionQualityEvaluator(self.client)
        self.mission_generator = SmartMissionGenerator(self.client)
        
        # 데이터 및 인덱스
        self.heritage_data = None
        self.documents = []
        self.metadata = []
        self.optimized_index = None
        
        # 성능 추적
        self.performance_metrics = {
            "search_times": [],
            "generation_times": [],
            "cache_hits": 0,
            "total_requests": 0
        }
    
    def load_data(self, data_file: str = "heritage_complete_database.json"):
        """데이터 로드"""
        try:
            with open(data_file, "r", encoding="utf-8") as f:
                self.heritage_data = json.load(f)
            
            logger.info(f"데이터 로드 완료: {self.heritage_data.get('총_개수', 0)}개")
            return True
        except Exception as e:
            logger.error(f"데이터 로드 실패: {e}")
            return False
    
    def prepare_documents(self):
        """문서 준비"""
        if not self.heritage_data:
            return False
        
        for spot in self.heritage_data['스팟']:
            doc = f"""장소명: {spot.get('이름', '')}
위치: {spot.get('위치', '')}
분류: {spot.get('지정종목', '')} {spot.get('지정번호', '')}
시대: {spot.get('설명', '')}
상세분류: {spot.get('상세분류', '')}
교육연계: {spot.get('교육과정_연계', '')}"""
            
            self.documents.append(doc)
            self.metadata.append({
                'name': spot.get('이름', ''),
                'location': spot.get('위치', ''),
                'category': spot.get('지정종목', ''),
                'description': spot.get('설명', ''),
                'classification': spot.get('상세분류', '')
            })
        
        logger.info(f"문서 준비 완료: {len(self.documents)}개")
        return True
    
    async def create_optimized_embeddings(self):
        """최적화된 임베딩 생성"""
        try:
            embeddings = np.load("optimized_embeddings.npy")
            self.optimized_index = OptimizedFAISS(embeddings.shape[1])
            self.optimized_index.build_optimized_index(embeddings)
            logger.info("기존 최적화된 임베딩 로드")
            return True
        except:
            pass
        
        logger.info("새로운 최적화된 임베딩 생성 중...")
        embeddings = []
        batch_size = 100
        
        # 배치 처리로 임베딩 생성
        for i in range(0, len(self.documents), batch_size):
            batch = self.documents[i:i+batch_size]
            
            try:
                response = self.client.embeddings.create(
                    model="text-embedding-3-large",
                    input=batch
                )
                batch_embeddings = [data.embedding for data in response.data]
                embeddings.extend(batch_embeddings)
                
                logger.info(f"배치 {i//batch_size + 1} 완료")
                
            except Exception as e:
                logger.error(f"임베딩 생성 실패: {e}")
                return False
        
        embeddings = np.array(embeddings)
        
        # 최적화된 인덱스 구축
        self.optimized_index = OptimizedFAISS(embeddings.shape[1])
        self.optimized_index.build_optimized_index(embeddings)
        
        # 저장
        np.save("optimized_embeddings.npy", embeddings)
        
        return True
    
    async def smart_search(self, query: str, k: int = 5) -> List[Dict]:
        """스마트 검색 (캐싱 + 성능 최적화)"""
        start_time = time.time()
        
        # 캐시 확인
        cache_key = self.cache_manager._get_cache_key(query, "search", k=k)
        cached_result = self.cache_manager.get(cache_key)
        
        if cached_result:
            self.performance_metrics["cache_hits"] += 1
            return cached_result
        
        try:
            # 쿼리 임베딩
            response = self.client.embeddings.create(
                model="text-embedding-3-large",
                input=[query]
            )
            query_embedding = np.array([response.data[0].embedding])
            
            # 최적화된 검색
            distances, indices = self.optimized_index.index.search(
                query_embedding.astype(np.float32), k
            )
            
            results = []
            for i, idx in enumerate(indices[0]):
                if idx < len(self.documents):
                    results.append({
                        'document': self.documents[idx],
                        'metadata': self.metadata[idx],
                        'distance': float(distances[0][i]),
                        'relevance_score': 1.0 / (1.0 + distances[0][i])
                    })
            
            # 캐시 저장
            self.cache_manager.set(cache_key, results)
            
            search_time = time.time() - start_time
            self.performance_metrics["search_times"].append(search_time)
            
            return results
            
        except Exception as e:
            logger.error(f"검색 실패: {e}")
            return []
    
    async def generate_smart_mission(self, request: MissionRequest) -> MissionResult:
        """스마트 미션 생성 (자동화 + 품질 평가)"""
        start_time = time.time()
        mission_id = hashlib.md5(f"{request.location}_{int(time.time())}".encode()).hexdigest()[:8]
        
        self.performance_metrics["total_requests"] += 1
        
        try:
            # 1. 스마트 검색
            search_results = await self.smart_search(request.location, k=5)
            
            if not search_results:
                return MissionResult(
                    mission_id=mission_id,
                    mission_type="error",
                    content="관련 정보를 찾을 수 없습니다.",
                    quality_score=0.0,
                    generation_time=time.time() - start_time,
                    source_heritage=[],
                    metadata={"error": "no_results"}
                )
            
            # 2. 자동 미션 타입 결정
            heritage_info = [r['metadata'] for r in search_results]
            mission_type = request.preferred_type or self.mission_generator.auto_determine_mission_type(
                request.location, heritage_info
            )
            
            # 3. 컨텍스트 기반 프롬프트 생성
            prompt = self.mission_generator.generate_contextual_prompt(request, heritage_info)
            
            # 4. 캐시된 미션 확인
            cache_key = self.cache_manager._get_cache_key(
                request.location, mission_type, 
                grade=request.user_grade, 
                duration=request.duration_minutes
            )
            cached_mission = self.cache_manager.get(cache_key)
            
            if cached_mission:
                self.performance_metrics["cache_hits"] += 1
                return MissionResult(**cached_mission)
            
            # 5. 미션 생성
            response = self.client.chat.completions.create(
                model="gpt-4o",
                messages=[
                    {"role": "system", "content": "당신은 창의적인 초등학생 교육 전문가입니다."},
                    {"role": "user", "content": prompt}
                ],
                max_tokens=800,
                temperature=0.8
            )
            
            mission_content = response.choices[0].message.content.strip()
            
            # 6. 품질 평가
            quality_eval = await self.quality_evaluator.evaluate_mission(mission_content, mission_type)
            quality_score = quality_eval.get("총점", 0) / 50.0  # 0-1 스케일로 정규화
            
            generation_time = time.time() - start_time
            self.performance_metrics["generation_times"].append(generation_time)
            
            # 7. 결과 객체 생성
            result = MissionResult(
                mission_id=mission_id,
                mission_type=mission_type,
                content=mission_content,
                quality_score=quality_score,
                generation_time=generation_time,
                source_heritage=[r['metadata']['name'] for r in search_results[:3]],
                metadata={
                    "quality_eval": quality_eval,
                    "search_results_count": len(search_results),
                    "auto_determined_type": mission_type == request.preferred_type
                }
            )
            
            # 8. 결과 캐싱
            self.cache_manager.set(cache_key, result.__dict__)
            
            return result
            
        except Exception as e:
            logger.error(f"미션 생성 실패: {e}")
            return MissionResult(
                mission_id=mission_id,
                mission_type="error",
                content=f"미션 생성 중 오류 발생: {e}",
                quality_score=0.0,
                generation_time=time.time() - start_time,
                source_heritage=[],
                metadata={"error": str(e)}
            )
    
    def get_performance_report(self) -> Dict:
        """성능 리포트 생성"""
        search_times = self.performance_metrics["search_times"]
        generation_times = self.performance_metrics["generation_times"]
        
        return {
            "총_요청수": self.performance_metrics["total_requests"],
            "캐시_히트율": self.performance_metrics["cache_hits"] / max(1, self.performance_metrics["total_requests"]),
            "평균_검색시간": np.mean(search_times) if search_times else 0,
            "평균_생성시간": np.mean(generation_times) if generation_times else 0,
            "인덱스_타입": self.optimized_index.index_type if self.optimized_index else "none",
            "문서_수": len(self.documents)
        }

# 자동화된 테스트 시스템
class AutomatedTestSuite:
    """자동화된 테스트 및 최적화"""
    
    def __init__(self, pipeline: AdvancedRAGPipeline):
        self.pipeline = pipeline
        
    def generate_test_scenarios(self) -> List[MissionRequest]:
        """다양한 테스트 시나리오 자동 생성"""
        locations = ["경복궁", "서울대공원", "국립중앙박물관", "불국사", "제주도"]
        grades = [3, 4, 5, 6]
        difficulties = ["easy", "medium", "hard"]
        mission_types = ["퀴즈", "관찰미션", "체험미션"]
        
        scenarios = []
        for location in locations:
            for grade in grades:
                for difficulty in difficulties:
                    for mission_type in mission_types:
                        scenarios.append(MissionRequest(
                            location=location,
                            user_grade=grade,
                            difficulty=difficulty,
                            preferred_type=mission_type,
                            duration_minutes=30
                        ))
        
        return scenarios
    
    async def run_comprehensive_test(self) -> Dict:
        """종합 테스트 실행"""
        scenarios = self.generate_test_scenarios()
        results = []
        
        logger.info(f"종합 테스트 시작: {len(scenarios)}개 시나리오")
        
        for i, scenario in enumerate(scenarios[:20]):  # 샘플 20개만
            logger.info(f"테스트 {i+1}/20: {scenario.location} - {scenario.preferred_type}")
            
            result = await self.pipeline.generate_smart_mission(scenario)
            results.append(result)
            
            # 진행률 표시
            if (i + 1) % 5 == 0:
                logger.info(f"테스트 진행률: {i+1}/20 완료")
        
        # 결과 분석
        quality_scores = [r.quality_score for r in results if r.quality_score > 0]
        generation_times = [r.generation_time for r in results]
        
        return {
            "테스트_시나리오수": len(results),
            "성공률": len(quality_scores) / len(results),
            "평균_품질점수": np.mean(quality_scores) if quality_scores else 0,
            "품질_표준편차": np.std(quality_scores) if quality_scores else 0,
            "평균_생성시간": np.mean(generation_times),
            "최고_품질점수": max(quality_scores) if quality_scores else 0,
            "최저_품질점수": min(quality_scores) if quality_scores else 0,
            "성능_리포트": self.pipeline.get_performance_report()
        }

# 메인 실행 함수
async def main():
    """고도화된 파이프라인 테스트"""
    logger.info("🚀 고도화된 RAG 파이프라인 시작")
    
    # 파이프라인 초기화
    pipeline = AdvancedRAGPipeline()
    
    if not pipeline.load_data():
        return
    
    if not pipeline.prepare_documents():
        return
    
    if not await pipeline.create_optimized_embeddings():
        return
    
    logger.info("✅ 파이프라인 준비 완료")
    
    # 자동화된 테스트
    test_suite = AutomatedTestSuite(pipeline)
    
    # 종합 테스트 실행
    test_results = await test_suite.run_comprehensive_test()
    
    # 결과 출력
    logger.info("📊 테스트 결과:")
    for key, value in test_results.items():
        logger.info(f"   {key}: {value}")
    
    # 개별 미션 생성 테스트
    sample_request = MissionRequest(
        location="경복궁",
        user_grade=5,
        group_size=4,
        duration_minutes=45,
        difficulty="medium",
        context="첫 현장학습이라 안전에 특히 주의"
    )
    
    logger.info("\n🎯 샘플 미션 생성 테스트")
    result = await pipeline.generate_smart_mission(sample_request)
    
    logger.info(f"미션 ID: {result.mission_id}")
    logger.info(f"미션 타입: {result.mission_type}")
    logger.info(f"품질 점수: {result.quality_score:.2f}")
    logger.info(f"생성 시간: {result.generation_time:.2f}초")
    logger.info(f"참조 문화재: {', '.join(result.source_heritage)}")
    logger.info(f"미션 내용:\n{result.content}")

if __name__ == "__main__":
    asyncio.run(main())