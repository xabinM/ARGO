# rag_pipeline/llm_client.py - LLM API 클라이언트
import os
import asyncio
import logging
from typing import Dict, List, Optional
from openai import OpenAI
from dotenv import load_dotenv

logger = logging.getLogger(__name__)

class LLMClient:
    """GMS/OpenAI API 클라이언트"""
    
    def __init__(self):
        load_dotenv()
        
        # API 키 및 설정 로드
        self.api_key = os.getenv('GMS_API_KEY') or os.getenv('OPENAI_API_KEY')
        self.base_url = os.getenv('GMS_BASE_URL', 'https://gms.ssafy.io/gmsapi/api.openai.com/v1')
        
        if not self.api_key:
            raise ValueError("GMS_API_KEY 또는 OPENAI_API_KEY 환경변수가 설정되지 않았습니다")
        
        # OpenAI 클라이언트 초기화
        self.client = OpenAI(
            api_key=self.api_key,
            base_url=self.base_url
        )
        
        logger.info(f"LLM 클라이언트 초기화 완료: {self.base_url}")
    
    async def generate_mission(self, prompt: str, model: str = "gpt-4o-mini", max_tokens: int = 600) -> str:
        """LLM을 사용한 미션 생성"""
        try:
            response = self.client.chat.completions.create(
                model=model,
                messages=[
                    {"role": "system", "content": "당신은 창의적인 초등학생 교육 전문가입니다. 안전하고 교육적인 현장학습 미션을 만드는 것이 전문입니다."},
                    {"role": "user", "content": prompt}
                ],
                max_tokens=max_tokens,
                temperature=0.7
            )
            
            return response.choices[0].message.content.strip()
            
        except Exception as e:
            logger.error(f"LLM 미션 생성 실패: {e}")
            raise e
    
    async def evaluate_mission_quality(self, mission_content: str, mission_type: str) -> Dict:
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

JSON 형식으로 응답하세요:
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
            response = self.client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {"role": "system", "content": "교육 전문가로서 JSON 형식으로만 응답하세요."},
                    {"role": "user", "content": evaluation_prompt}
                ],
                max_tokens=500,
                temperature=0.3
            )
            
            import json
            eval_result = json.loads(response.choices[0].message.content.strip())
            return eval_result
            
        except Exception as e:
            logger.error(f"품질 평가 실패: {e}")
            return {"총점": 0, "등급": "F", "오류": str(e)}
    
    async def create_embeddings(self, texts: List[str]) -> List[List[float]]:
        """텍스트 임베딩 생성"""
        try:
            response = self.client.embeddings.create(
                model="text-embedding-3-large",
                input=texts
            )
            
            return [data.embedding for data in response.data]
            
        except Exception as e:
            logger.error(f"임베딩 생성 실패: {e}")
            raise e