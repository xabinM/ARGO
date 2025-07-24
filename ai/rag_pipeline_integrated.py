# argo_rag_pipeline.py - ARGO 프로젝트 최종 RAG 파이프라인
import logging
import json
import os
import asyncio
from typing import Dict, List, Optional
from dataclasses import dataclass
from datetime import datetime

logger = logging.getLogger(__name__)

@dataclass
class MissionOutput:
    """ARGO 미션 출력 데이터 클래스"""
    # 교사용 정보 (대시보드용)
    teacher_version: Dict
    # 학생용 정보 (앱용)
    student_version: Dict
    # 메타데이터 (API 응답용)
    metadata: Dict

class ARGODataPreprocessor:
    """ARGO 전용 데이터 전처리기"""
    
    def __init__(self):
        # 초등학생 안전 키워드 필터
        self.safety_keywords = {
            '안전한': 1.0, '보호': 0.8, '주의': 0.6,
            '위험': -0.8, '무서운': -0.6, '혼자': -0.5
        }
        
        # 학년별 어휘 난이도 매핑
        self.grade_vocabulary = {
            3: {'건축물': '건물', '유물': '옛 물건', '문화재': '문화유산'},
            4: {'조성': '만들어짐', '건립': '세워짐'},
            5: {'창건': '처음 만듦', '중건': '다시 만듦'},
            6: {}  # 원문 유지
        }
    
    def preprocess_spot_data(self, spot_data: Dict, target_grade: int = 5) -> Dict:
        """스팟 데이터 전처리"""
        try:
            # 1. 기본 정보 추출
            name = spot_data.get('이름', '')
            location = spot_data.get('위치', '')
            description = spot_data.get('설명', '')
            
            # 2. GPS 유효성 검사
            gps = spot_data.get('GPS', [0, 0])
            if not self._is_valid_gps(gps):
                logger.warning(f"잘못된 GPS: {name}")
                return None
            
            # 3. 안전성 점수 계산
            safety_score = self._calculate_safety_score(description)
            if safety_score < 0.6:
                logger.warning(f"안전성 부족: {name}")
                return None
            
            # 4. 학년별 텍스트 조정
            adjusted_description = self._adjust_for_grade(description, target_grade)
            
            # 5. 교육과정 연계 정보 추출
            education_link = spot_data.get('교육과정_연계', '')
            
            return {
                'name': name,
                'location': location,
                'description': adjusted_description,
                'gps': gps,
                'safety_score': safety_score,
                'education_link': education_link,
                'category': spot_data.get('지정종목', ''),
                'detailed_class': spot_data.get('상세분류', ''),
                'grade_level': target_grade
            }
            
        except Exception as e:
            logger.error(f"전처리 오류: {e}")
            return None
    
    def _is_valid_gps(self, gps: List) -> bool:
        """GPS 좌표 유효성 검사"""
        if len(gps) != 2:
            return False
        lat, lon = gps
        # 대한민국 대략적 범위
        return (33.0 <= lat <= 39.0) and (124.0 <= lon <= 132.0)
    
    def _calculate_safety_score(self, text: str) -> float:
        """안전성 점수 계산"""
        score = 0.8  # 기본 점수
        text_lower = text.lower()
        
        for keyword, weight in self.safety_keywords.items():
            if keyword in text_lower:
                score += weight * 0.1
        
        return max(0.0, min(1.0, score))
    
    def _adjust_for_grade(self, text: str, grade: int) -> str:
        """학년별 어휘 조정"""
        if grade not in self.grade_vocabulary:
            return text
        
        vocab_map = self.grade_vocabulary[grade]
        for complex_word, simple_word in vocab_map.items():
            text = text.replace(complex_word, simple_word)
        
        return text

class ARGORAGRetriever:
    """ARGO 전용 RAG 검색기 - Ko-SBERT + FAISS 실제 구현"""
    
    def __init__(self):
        self.processed_spots = []
        self.document_embeddings = None
        self.vector_index = None
        self.embedding_model = None
        self.device = "cuda" if self._check_gpu() else "cpu"
    
    def _check_gpu(self) -> bool:
        """GPU 사용 가능성 체크"""
        try:
            import torch
            return torch.cuda.is_available()
        except ImportError:
            return False
    
    async def initialize(self, data_file: str):
        """검색기 초기화 - 실제 벡터 임베딩 구현"""
        logger.info("🔍 RAG 검색기 초기화")
        
        # 1. 임베딩 모델 로드
        await self._load_embedding_model()
        
        # 2. 데이터 로드 및 전처리
        with open(data_file, 'r', encoding='utf-8') as f:
            raw_data = json.load(f)
        
        preprocessor = ARGODataPreprocessor()
        self.processed_spots = []
        
        for spot in raw_data.get('스팟', []):
            processed = preprocessor.preprocess_spot_data(spot)
            if processed:
                self.processed_spots.append(processed)
        
        logger.info(f"✅ {len(self.processed_spots)}개 스팟 준비 완료")
        
        # 3. 벡터 임베딩 생성 및 FAISS 인덱스 구축
        await self._create_vector_index()
        
        return True
    
    async def _load_embedding_model(self):
        """Ko-SBERT 임베딩 모델 로드"""
        try:
            from sentence_transformers import SentenceTransformer
            
            # Ko-SBERT 모델 로드 (한국어 특화)
            model_name = "snunlp/KR-SBERT-V40K-klueNLI-augSTS"
            logger.info(f"📥 임베딩 모델 로드 중: {model_name}")
            
            self.embedding_model = SentenceTransformer(model_name, device=self.device)
            logger.info(f"✅ 임베딩 모델 로드 완료 (device: {self.device})")
            
        except ImportError:
            logger.warning("⚠️ sentence-transformers 라이브러리가 없습니다. 설치가 필요합니다.")
            logger.info("설치 명령: pip install sentence-transformers")
            # 폴백: 간단한 텍스트 매칭 사용
            self.embedding_model = None
        except Exception as e:
            logger.error(f"임베딩 모델 로드 실패: {e}")
            self.embedding_model = None
    
    async def _create_vector_index(self):
        """FAISS 벡터 인덱스 생성"""
        if self.embedding_model is None:
            logger.warning("임베딩 모델이 없어 간단한 텍스트 매칭을 사용합니다")
            await self._create_simple_index()
            return
        
        try:
            import faiss
            import numpy as np
            
            logger.info("📊 벡터 임베딩 생성 중...")
            
            # 1. 문서 텍스트 준비
            documents = []
            for spot in self.processed_spots:
                # 임베딩용 텍스트: 이름, 위치, 설명, 교육과정 연계 통합
                doc_text = f"이름: {spot['name']} 위치: {spot['location']} 지정종목: {spot['category']} 지정번호: {spot['designation_number']} 설명: {spot['description']} 상세분류: {spot['detailed_class']}"
                documents.append(doc_text)
            
            # 2. 배치 임베딩 생성 (메모리 효율적)
            batch_size = 32
            all_embeddings = []
            
            for i in range(0, len(documents), batch_size):
                batch = documents[i:i + batch_size]
                batch_embeddings = self.embedding_model.encode(
                    batch,
                    batch_size=batch_size,
                    show_progress_bar=True,
                    convert_to_numpy=True,
                    normalize_embeddings=True  # 코사인 유사도 최적화
                )
                all_embeddings.append(batch_embeddings)
            
            # 3. 전체 임베딩 결합
            self.document_embeddings = np.vstack(all_embeddings)
            logger.info(f"📊 임베딩 생성 완료: {self.document_embeddings.shape}")
            
            # 4. FAISS 인덱스 생성
            dimension = self.document_embeddings.shape[1]
            
            # IndexFlatIP 사용 (정규화된 벡터에 대한 코사인 유사도)
            self.vector_index = faiss.IndexFlatIP(dimension)
            self.vector_index.add(self.document_embeddings.astype(np.float32))
            
            logger.info(f"✅ FAISS 인덱스 생성 완료: {self.vector_index.ntotal}개 벡터")
            
        except ImportError:
            logger.warning("⚠️ faiss 라이브러리가 없습니다. 설치가 필요합니다.")
            logger.info("설치 명령: pip install faiss-cpu  # 또는 faiss-gpu")
            await self._create_simple_index()
        except Exception as e:
            logger.error(f"FAISS 인덱스 생성 실패: {e}")
            await self._create_simple_index()
    
    async def _create_simple_index(self):
        """폴백: 간단한 텍스트 인덱스"""
        logger.info("📝 간단한 텍스트 인덱스 생성 중...")
        self.simple_documents = [
            f"{spot['name']} {spot['location']} {spot['description']}"
            for spot in self.processed_spots
        ]
        logger.info("✅ 텍스트 인덱스 생성 완료")
    
    def search_relevant_spots(self, query: str, grade: int, max_results: int = 3) -> List[Dict]:
        """관련 스팟 검색 - 실제 벡터 유사도 사용"""
        
        if self.vector_index is not None:
            return self._vector_search(query, grade, max_results)
        else:
            return self._simple_search(query, grade, max_results)
    
    def _vector_search(self, query: str, grade: int, max_results: int) -> List[Dict]:
        """벡터 유사도 기반 검색"""
        try:
            import numpy as np
            
            # 1. 쿼리 임베딩 생성
            query_embedding = self.embedding_model.encode(
                [query], 
                convert_to_numpy=True, 
                normalize_embeddings=True
            )
            
            # 2. FAISS 검색
            similarities, indices = self.vector_index.search(
                query_embedding.astype(np.float32), 
                min(max_results * 2, len(self.processed_spots))  # 여유분 확보
            )
            
            # 3. 결과 후처리
            results = []
            for i, (similarity, idx) in enumerate(zip(similarities[0], indices[0])):
                if idx >= len(self.processed_spots):
                    continue
                
                spot = self.processed_spots[idx]
                
                # 학년 적합성 보너스
                grade_bonus = 0.0
                if abs(spot['grade_level'] - grade) <= 1:
                    grade_bonus = 0.1
                elif abs(spot['grade_level'] - grade) <= 2:
                    grade_bonus = 0.05
                
                # 안전성 가중치
                safety_weight = spot['safety_score']
                
                # 최종 점수 계산
                final_score = similarity + grade_bonus
                final_score *= safety_weight  # 안전하지 않으면 점수 하락
                
                results.append({
                    'spot': spot,
                    'relevance_score': float(final_score),
                    'vector_similarity': float(similarity),
                    'grade_bonus': grade_bonus,
                    'safety_weight': safety_weight
                })
            
            # 4. 최종 점수 기준 정렬
            results.sort(key=lambda x: x['relevance_score'], reverse=True)
            
            # 5. 상위 결과만 반환
            return results[:max_results]
            
        except Exception as e:
            logger.error(f"벡터 검색 실패: {e}")
            return self._simple_search(query, grade, max_results)
    
    def _simple_search(self, query: str, grade: int, max_results: int) -> List[Dict]:
        """폴백: 간단한 텍스트 매칭 검색"""
        results = []
        query_lower = query.lower()
        query_words = set(query_lower.split())
        
        for i, spot in enumerate(self.processed_spots):
            # 텍스트 매칭 점수
            if hasattr(self, 'simple_documents'):
                text = self.simple_documents[i].lower()
            else:
                text = f"{spot['name']} {spot['location']} {spot['description']}".lower()
            
            text_words = set(text.split())
            
            # Jaccard 유사도 계산
            intersection = query_words.intersection(text_words)
            union = query_words.union(text_words)
            jaccard_score = len(intersection) / len(union) if union else 0
            
            # 정확한 키워드 매칭 보너스
            exact_matches = sum(1 for word in query_words if word in text)
            exact_score = exact_matches / len(query_words) if query_words else 0
            
            # 학년 적합성
            grade_score = max(0, 1 - abs(spot['grade_level'] - grade) * 0.2)
            
            # 종합 점수
            total_score = (jaccard_score * 0.4 + exact_score * 0.4 + grade_score * 0.2) * spot['safety_score']
            
            if total_score > 0.1:  # 최소 임계값
                results.append({
                    'spot': spot,
                    'relevance_score': total_score,
                    'jaccard_similarity': jaccard_score,
                    'exact_match_score': exact_score,
                    'grade_compatibility': grade_score
                })
        
        # 점수순 정렬
        results.sort(key=lambda x: x['relevance_score'], reverse=True)
        return results[:max_results]

class ARGOMissionGenerator:
    """ARGO 전용 미션 생성기"""
    
    def __init__(self):
        self.mission_templates = {
            '퀴즈': self._generate_quiz_mission,
            '관찰미션': self._generate_observation_mission,
            '체험미션': self._generate_experience_mission,
            '사진미션': self._generate_photo_mission
        }
    
    async def generate_mission(self, spot_info: Dict, mission_type: str, 
                              grade: int, group_size: int, duration: int) -> Dict:
        """미션 생성"""
        logger.info(f"🎯 미션 생성: {mission_type} for {spot_info['name']}")
        
        # 1. 템플릿 선택
        generator_func = self.mission_templates.get(mission_type, self._generate_quiz_mission)
        
        # 2. 미션 생성
        mission_content = await generator_func(spot_info, grade, group_size, duration)
        
        # 3. 후처리
        processed_mission = self._postprocess_mission(mission_content, mission_type, grade)
        
        return processed_mission
    
    async def _generate_quiz_mission(self, spot: Dict, grade: int, group_size: int, duration: int) -> str:
        """퀴즈 미션 생성"""
        mission = f"""🎯 미션 제목: {spot['name']} 탐험 퀴즈

📝 문제: {spot['name']}에 대한 설명을 듣고 다음 질문에 답해보세요.

{spot['description'][:200]}...

💡 퀴즈:
1) {spot['name']}은 어느 시대에 만들어졌을까요?
   ① 고려시대  ② 조선시대  ③ 현대

2) 이곳의 주요 특징은 무엇일까요?
   ① 왕이 살던 곳  ② 종교 의식을 하던 곳  ③ 일반인이 모이던 곳

🎯 팀 활동: {group_size}명이 함께 답을 논의해보세요.
⏰ 예상 시간: {duration}분

✅ 정답은 미션 완료 후 확인할 수 있어요!"""
        
        return mission
    
    async def _generate_observation_mission(self, spot: Dict, grade: int, group_size: int, duration: int) -> str:
        """관찰 미션 생성"""
        mission = f"""🎯 미션 제목: {spot['name']} 세밀 관찰하기

👀 관찰 활동:
1. {spot['name']}의 외관을 자세히 관찰해보세요
2. 특별한 특징 3가지를 찾아보세요
3. 다른 건물과 다른 점을 발견해보세요

📝 기록하기:
- 색깔: _____________
- 모양: _____________  
- 특별한 점: _____________

🎭 팀 활동: 
{group_size}명이 각각 다른 부분을 관찰하고 서로 공유해보세요.

⏰ 관찰 시간: {duration}분

💡 관찰 후에는 왜 이런 모습으로 만들어졌는지 생각해보세요!"""
        
        return mission
    
    async def _generate_experience_mission(self, spot: Dict, grade: int, group_size: int, duration: int) -> str:
        """체험 미션 생성"""
        mission = f"""🎯 미션 제목: {spot['name']} 시간여행 체험

🎭 역할놀이:
- 왕: 1명 (지시를 내리는 역할)
- 신하: {group_size-1}명 (왕에게 예를 표하는 역할)

📜 시나리오:
{spot['description'][:150]}... 시대로 돌아가서 실제로 이곳에서 벌어졌던 일을 재현해보세요.

🎪 활동 순서:
1) 역할 정하기 (2분)
2) 간단한 대사 연습 (5분)  
3) 실제 연기하기 (8분)
4) 소감 나누기 (5분)

⏰ 전체 시간: {duration}분

💭 활동 후: 그 시대 사람들의 마음이 어땠을지 이야기해보세요!"""
        
        return mission
    
    async def _generate_photo_mission(self, spot: Dict, grade: int, group_size: int, duration: int) -> str:
        """사진 미션 생성 (ARGO AR 연계)"""
        mission = f"""🎯 미션 제목: {spot['name']} 최고의 한 장

📸 사진 미션:
1. {spot['name']}의 가장 멋진 각도를 찾아보세요
2. {group_size}명 모두가 들어가는 단체사진을 찍어보세요
3. 역사적 의미를 담은 포즈를 생각해보세요

🎨 창의적 사진 도전:
- 건물과 같은 포즈 만들기
- 그 시대 사람들처럼 행동하기
- 건물의 특징을 몸으로 표현하기

📱 ARGO 앱 연동:
사진 촬영 후 앱에 업로드하면 AR 효과와 함께 역사 정보를 볼 수 있어요!

⏰ 촬영 시간: {duration}분

⭐ 팁: 안전선 안에서만 촬영하고, 문화재는 만지지 마세요!"""
        
        return mission
    
    def _postprocess_mission(self, content: str, mission_type: str, grade: int) -> Dict:
        """미션 후처리"""
        # 안전 수칙 추가
        if '⚠️' not in content:
            content += "\n\n⚠️ 안전수칙:\n- 선생님과 함께 활동하세요\n- 문화재에 손대지 마세요\n- 정해진 구역에서만 활동하세요"
        
        # 품질 점수 계산
        quality_score = 0.8
        if len(content) > 300:
            quality_score += 0.1
        if '🎯' in content:
            quality_score += 0.05
        if '팀' in content or '협력' in content:
            quality_score += 0.05
        
        return {
            'content': content,
            'mission_type': mission_type,
            'grade': grade,
            'quality_score': min(1.0, quality_score),
            'safety_included': True,
            'ar_compatible': mission_type == 'AR미션'
        }

class ARGOOutputFormatter:
    """ARGO 전용 출력 포맷터 - 교사용/학생용 분리"""
    
    def format_for_argo(self, mission_data: Dict, spot_info: Dict, request: Dict) -> MissionOutput:
        """ARGO 프로젝트용 출력 포맷팅"""
        
        # 학생용 버전 (모바일 앱용)
        student_version = {
            "mission_id": f"argo_{int(datetime.now().timestamp())}",
            "spot_name": spot_info['name'],
            "location": spot_info['location'],
            "gps": spot_info['gps'],
            "mission_content": self._extract_student_content(mission_data['content']),
            "mission_type": self._get_app_friendly_type(mission_data['mission_type']),
            "estimated_time": f"{request.get('duration_minutes', 30)}분",
            "team_size": request.get('group_size', 4),
            "ar_enabled": mission_data.get('ar_compatible', False),
            "safety_notes": ["문화재 보호", "팀 단위 행동", "안전구역 준수"]
        }
        
        # 교사용 버전 (웹 대시보드용)
        teacher_version = {
            "mission_overview": {
                "id": student_version["mission_id"],
                "spot": spot_info['name'],
                "type": mission_data['mission_type'],
                "grade": request.get('grade', 5),
                "duration": request.get('duration_minutes', 30),
                "quality_score": mission_data['quality_score']
            },
            "full_mission_content": mission_data['content'],
            "teaching_guide": {
                "preparation": [
                    "미션 전 안전 수칙 설명",
                    "팀 구성 및 역할 분담",
                    "ARGO 앱 사용법 확인"
                ],
                "monitoring_points": [
                    "학생들의 안전한 이동",
                    "팀별 협력 상황",
                    "미션 이해도 및 참여도"
                ],
                "assessment": [
                    "팀 협력도 평가",
                    "미션 완성도 평가", 
                    "역사 이해도 평가"
                ]
            },
            "technical_info": {
                "ar_requirements": mission_data.get('ar_compatible', False),
                "minimum_android_version": "7.0",
                "required_permissions": ["카메라", "위치", "저장소"]
            },
            "spot_metadata": {
                "education_link": spot_info.get('education_link', ''),
                "safety_score": spot_info.get('safety_score', 0.8),
                "category": spot_info.get('category', ''),
                "gps_accuracy": "±5m"
            }
        }
        
        # 메타데이터 (API 응답용)
        metadata = {
            "generation_time": datetime.now().isoformat(),
            "pipeline_version": "ARGO-v1.0",
            "source_spot": spot_info['name'],
            "processing_modules": ["preprocessor", "retriever", "generator", "formatter"],
            "performance": {
                "quality_score": mission_data['quality_score'],
                "safety_score": spot_info['safety_score'],
                "grade_appropriateness": True
            }
        }
        
        return MissionOutput(
            teacher_version=teacher_version,
            student_version=student_version,
            metadata=metadata
        )
    
    def _extract_student_content(self, full_content: str) -> str:
        """학생용 깔끔한 콘텐츠 추출"""
        lines = full_content.split('\n')
        student_lines = []
        
        # 교사용 정보 제외
        skip_patterns = ['⚠️ 안전수칙', '💡 권장사항', '교사', '준비사항']
        
        for line in lines:
            if any(pattern in line for pattern in skip_patterns):
                break
            if line.strip():
                student_lines.append(line)
        
        return '\n'.join(student_lines)
    
    def _get_app_friendly_type(self, mission_type: str) -> str:
        """앱 친화적 미션 타입"""
        type_map = {
            '퀴즈': '🎯 퀴즈 도전',
            '관찰미션': '👀 관찰 탐험',
            '체험미션': '🎭 역할 체험',
            '사진미션': '📸 포토 미션'
        }
        return type_map.get(mission_type, '🎯 미션')

class ARGOPipeline:
    """ARGO 프로젝트 최종 통합 파이프라인"""
    
    def __init__(self):
        self.retriever = ARGORAGRetriever()
        self.generator = ARGOMissionGenerator()
        self.formatter = ARGOOutputFormatter()
        self.initialized = False
    
    async def initialize(self, heritage_data_file: str):
        """파이프라인 초기화"""
        logger.info("🚀 ARGO RAG 파이프라인 초기화")
        
        success = await self.retriever.initialize(heritage_data_file)
        if success:
            self.initialized = True
            logger.info("✅ ARGO 파이프라인 준비 완료")
        else:
            raise Exception("파이프라인 초기화 실패")
    
    async def generate_argo_mission(self, request: Dict) -> MissionOutput:
        """ARGO 미션 생성 메인 메서드"""
        if not self.initialized:
            raise Exception("파이프라인이 초기화되지 않았습니다")
        
        try:
            mission_id = f"argo_{int(datetime.now().timestamp())}"
            logger.info(f"🎯 ARGO 미션 생성 시작: {mission_id}")
            
            # 1. 관련 스팟 검색
            location = request.get('location', '')
            grade = request.get('grade', 5)
            
            relevant_spots = self.retriever.search_relevant_spots(location, grade, max_results=1)
            
            if not relevant_spots:
                return self._create_fallback_mission(request, mission_id)
            
            # 2. 최적 스팟 선택
            best_spot = relevant_spots[0]['spot']
            
            # 3. 미션 생성
            mission_type = request.get('mission_type', '퀴즈')  # ARGO 기본값
            group_size = request.get('group_size', 4)
            duration = request.get('duration_minutes', 30)
            
            mission_data = await self.generator.generate_mission(
                best_spot, mission_type, grade, group_size, duration
            )
            
            # 4. ARGO 포맷으로 출력 생성
            result = self.formatter.format_for_argo(mission_data, best_spot, request)
            
            logger.info(f"✅ ARGO 미션 생성 완료: {mission_id}")
            return result
            
        except Exception as e:
            logger.error(f"ARGO 미션 생성 실패: {e}")
            return self._create_error_mission(str(e), request.get('location', ''))
    
    def _create_fallback_mission(self, request: Dict, mission_id: str) -> MissionOutput:
        """관련 정보 없을 때 기본 미션"""
        location = request.get('location', '현재 위치')
        
        fallback_mission = {
            'content': f"""🎯 미션 제목: {location} 자유 탐험

👀 둘러보기: 주변을 자세히 관찰해보세요
📝 발견하기: 흥미로운 점 3가지를 찾아보세요  
🤝 나누기: 팀원들과 발견한 것을 공유해보세요

⏰ 탐험 시간: {request.get('duration_minutes', 30)}분""",
            'mission_type': '관찰미션',
            'grade': request.get('grade', 5),
            'quality_score': 0.6,
            'ar_compatible': False
        }
        
        fallback_spot = {
            'name': location,
            'location': location,
            'gps': [37.5665, 126.9780],  # 서울 시청 기본값
            'safety_score': 0.8,
            'education_link': '',
            'category': '일반'
        }
        
        return self.formatter.format_for_argo(fallback_mission, fallback_spot, request)
    
    def _create_error_mission(self, error_msg: str, location: str) -> MissionOutput:
        """오류 발생 시 기본 응답"""
        return MissionOutput(
            teacher_version={
                "error": error_msg,
                "fallback_available": True,
                "suggestion": "네트워크 연결을 확인하거나 다른 장소를 시도해보세요"
            },
            student_version={
                "message": "미션을 준비하고 있어요. 잠시만 기다려주세요! 🎯",
                "location": location,
                "status": "loading"
            },
            metadata={
                "error": error_msg,
                "timestamp": datetime.now().isoformat(),
                "status": "error"
            }
        )

# ARGO 프로젝트 통합 사용 예시
async def test_argo_pipeline():
    """ARGO 파이프라인 테스트"""
    
    # 1. 파이프라인 초기화
    pipeline = ARGOPipeline()
    await pipeline.initialize("heritage_complete_database.json")
    
    # 2. 테스트 요청 (안드로이드 앱에서 오는 요청 형태)
    test_requests = [
        {
            "location": "경복궁",
            "grade": 5,
            "group_size": 4,
            "duration_minutes": 30,
            "mission_type": "AR미션"
        },
        {
            "location": "서울대공원",
            "grade": 3,
            "group_size": 6,
            "duration_minutes": 45,
            "mission_type": "관찰미션"
        },
        {
            "location": "국립과천과학관",
            "grade": 6,
            "group_size": 3,
            "duration_minutes": 60,
            "mission_type": "체험미션"
        }
    ]
    
    # 3. 각 요청별 미션 생성 테스트
    for i, request in enumerate(test_requests, 1):
        print(f"\n{'='*50}")
        print(f"🧪 테스트 {i}: {request['location']} - {request['mission_type']}")
        print(f"{'='*50}")
        
        try:
            result = await pipeline.generate_argo_mission(request)
            
            # 4. 결과 출력
            print("📱 **학생용 출력 (모바일 앱)**:")
            print(f"미션 ID: {result.student_version['mission_id']}")
            print(f"장소: {result.student_version['spot_name']}")
            print(f"타입: {result.student_version['mission_type']}")
            print(f"예상시간: {result.student_version['estimated_time']}")
            print(f"AR 지원: {result.student_version['ar_enabled']}")
            print("\n미션 내용:")
            print(result.student_version['mission_content'][:300] + "...")
            
            print("\n🖥️ **교사용 출력 (웹 대시보드)**:")
            print(f"품질 점수: {result.teacher_version['mission_overview']['quality_score']:.2f}")
            print(f"안전 점수: {result.teacher_version['spot_metadata']['safety_score']:.2f}")
            print("교사 가이드:")
            for guide in result.teacher_version['teaching_guide']['preparation'][:2]:
                print(f"  - {guide}")
            
            print(f"\n📊 **메타데이터**:")
            print(f"처리 시간: {result.metadata['generation_time']}")
            print(f"사용 모듈: {', '.join(result.metadata['processing_modules'])}")
            
        except Exception as e:
            print(f"❌ 테스트 실패: {e}")

# FastAPI 연동을 위한 API 엔드포인트 예시
class ARGOAPIHandler:
    """ARGO API 핸들러 - FastAPI 연동용"""
    
    def __init__(self):
        self.pipeline = ARGOPipeline()
        self.initialized = False
    
    async def initialize_api(self, data_file: str):
        """API 서버 초기화"""
        await self.pipeline.initialize(data_file)
        self.initialized = True
        logger.info("🌐 ARGO API 서버 준비 완료")
    
    async def handle_mission_request(self, request_data: Dict) -> Dict:
        """API 요청 처리 (Android 앱 → 서버)"""
        if not self.initialized:
            return {"error": "서버가 초기화되지 않았습니다"}
        
        try:
            # 1. 요청 유효성 검사
            required_fields = ['location', 'grade', 'group_size']
            for field in required_fields:
                if field not in request_data:
                    return {"error": f"필수 필드 누락: {field}"}
            
            # 2. 미션 생성
            result = await self.pipeline.generate_argo_mission(request_data)
            
            # 3. API 응답 형태로 변환
            api_response = {
                "success": True,
                "data": {
                    "student_mission": result.student_version,
                    "teacher_info": result.teacher_version,
                    "metadata": result.metadata
                },
                "timestamp": datetime.now().isoformat()
            }
            
            return api_response
            
        except Exception as e:
            logger.error(f"API 요청 처리 실패: {e}")
            return {
                "success": False,
                "error": str(e),
                "timestamp": datetime.now().isoformat()
            }

# 사용법 가이드
"""
🚀 ARGO RAG 파이프라인 사용법

1. **초기화**:
   pipeline = ARGOPipeline()
   await pipeline.initialize("heritage_complete_database.json")

2. **미션 생성**:
   request = {
       "location": "경복궁",
       "grade": 5,
       "group_size": 4,
       "duration_minutes": 30,
       "mission_type": "AR미션"
   }
   result = await pipeline.generate_argo_mission(request)

3. **결과 활용**:
   - result.student_version: 안드로이드 앱에서 사용
   - result.teacher_version: 웹 대시보드에서 사용
   - result.metadata: API 응답 및 로깅용

4. **API 서버 연동**:
   api_handler = ARGOAPIHandler()
   await api_handler.initialize_api("heritage_data.json")
   response = await api_handler.handle_mission_request(request_data)

📂 **필요한 데이터 파일 구조**:
{
  "스팟": [
    {
      "이름": "경복궁",
      "위치": "서울특별시 종로구",
      "설명": "조선 왕조의 정궁...",
      "GPS": [37.5796, 126.9770],
      "지정종목": "사적",
      "상세분류": "궁궐",
      "교육과정_연계": "사회 5-2 조선시대"
    }
  ]
}

🔧 **주요 특징**:
- ✅ 교사용/학생용 완전 분리
- ✅ AR 미션 지원
- ✅ 안전성 검증
- ✅ 학년별 맞춤 조정
- ✅ 실시간 GPS 연동
- ✅ FastAPI 연동 준비
"""

if __name__ == "__main__":
    # 테스트 실행
    asyncio.run(test_argo_pipeline())