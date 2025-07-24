# rag_pipeline/retriever.py - RAG 검색기
import json
import logging
from typing import List, Dict
from .preprocessor import ARGODataPreprocessor

logger = logging.getLogger(__name__)

class ARGORAGRetriever:
    """ARGO 전용 RAG 검색기 - Ko-SBERT + FAISS 구현"""
    
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
        """검색기 초기화"""
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
            logger.warning("⚠️ sentence-transformers 라이브러리가 없습니다.")
            logger.info("설치 명령: pip install sentence-transformers")
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
                doc_text = f"{spot['name']} {spot['location']} {spot['description']} {spot.get('education_link', '')}"
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
            logger.warning("⚠️ faiss 라이브러리가 없습니다.")
            logger.info("설치 명령: pip install faiss-cpu")
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
        """관련 스팟 검색"""
        
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
                min(max_results * 2, len(self.processed_spots))
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
                final_score *= safety_weight
                
                results.append({
                    'spot': spot,
                    'relevance_score': float(final_score),
                    'vector_similarity': float(similarity),
                    'grade_bonus': grade_bonus,
                    'safety_weight': safety_weight
                })
            
            # 4. 최종 점수 기준 정렬
            results.sort(key=lambda x: x['relevance_score'], reverse=True)
            
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