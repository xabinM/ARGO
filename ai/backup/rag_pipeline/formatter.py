# rag_pipeline/formatter.py - QuizProblem 엔티티 대응 출력 포맷터
import logging
from datetime import datetime
from typing import Dict, List, Any, Optional
import json
import re

# 업데이트된 스키마 임포트
try:
    from config.database_schema import QuizProblemOutput, Spot, Coordinates
except ImportError:
    from ..config.database_schema import QuizProblemOutput, Spot, Coordinates

logger = logging.getLogger(__name__)

class ARGOOutputFormatter:
    """ARGO 전용 출력 포맷터 - QuizProblem 엔티티 대응"""
    
    def __init__(self):
        self.problem_type = "QUIZ"  # 항상 QUIZ로 고정
        self.choices_count = 3      # 항상 3개 선택지
        
        # 학년별 난이도 조정 기준
        self.grade_difficulty_map = {
            1: {"vocab_level": "초급", "sentence_length": 20},
            2: {"vocab_level": "초급", "sentence_length": 25},
            3: {"vocab_level": "중급", "sentence_length": 30},
            4: {"vocab_level": "중급", "sentence_length": 35},
            5: {"vocab_level": "고급", "sentence_length": 40},
            6: {"vocab_level": "고급", "sentence_length": 45}
        }
    
    def format_quiz_problem(self, 
                           llm_response: str, 
                           spot_info: Dict, 
                           request: Dict) -> QuizProblemOutput:
        """LLM 응답을 QuizProblem 엔티티 형식으로 변환"""
        
        start_time = datetime.now()
        grade = request.get('user_grade', 5)
        
        logger.info(f"🎯 QuizProblem 포맷팅 시작 - 학년: {grade}")
        
        try:
            # 1. LLM 응답 파싱
            parsed_quiz = self._parse_llm_response(llm_response)
            
            # 2. 학년별 적합성 검증 및 조정
            adjusted_quiz = self._adjust_for_grade(parsed_quiz, grade)
            
            # 3. 선택지 정리 (정확히 3개)
            choices = self._normalize_choices(adjusted_quiz.get('choices', []))
            
            # 4. 정답 인덱스 검증
            correct_index = self._validate_correct_index(
                adjusted_quiz.get('correct_index', 0), choices
            )
            
            # 5. 해설 생성/정리
            explanation = self._format_explanation(
                adjusted_quiz.get('explanation', ''), grade
            )
            
            # 6. 품질 점수 계산
            quality_score = self._calculate_quality_score(
                adjusted_quiz.get('question', ''), choices, explanation, grade
            )
            
            generation_time = (datetime.now() - start_time).total_seconds()
            
            # 7. QuizProblemOutput 생성
            quiz_problem = QuizProblemOutput(
                problem_type=self.problem_type,
                question=adjusted_quiz.get('question', ''),
                choices=choices,
                correct_index=correct_index,
                explanation=explanation,
                spot_id=spot_info.get('id'),
                spot_name=spot_info.get('name', ''),
                grade=grade,
                quality_score=quality_score,
                generation_time=generation_time,
                created_at=datetime.now()
            )
            
            logger.info(f"✅ QuizProblem 포맷팅 완료 - 품질점수: {quality_score:.2f}")
            return quiz_problem
            
        except Exception as e:
            logger.error(f"❌ QuizProblem 포맷팅 실패: {e}")
            return self._create_fallback_quiz(spot_info, request)
    
    def _parse_llm_response(self, response: str) -> Dict:
        """LLM 응답 파싱"""
        
        # JSON 형식 시도
        if response.strip().startswith('{'):
            try:
                return json.loads(response)
            except json.JSONDecodeError:
                pass
        
        # 구조화된 텍스트 파싱
        parsed = {}
        
        # 문제 지문 추출
        question_match = re.search(r'(?:문제|질문|Q)[:\s]*(.+?)(?=\n|선택지|보기|1\)|①)', response, re.DOTALL)
        if question_match:
            parsed['question'] = question_match.group(1).strip()
        
        # 선택지 추출
        choices = []
        choice_patterns = [
            r'(\d+)\)\s*(.+?)(?=\n\d+\)|$)',  # 1) 선택지
            r'([①②③④⑤])\s*(.+?)(?=\n[①②③④⑤]|$)',  # ① 선택지
            r'([가나다라마])\)\s*(.+?)(?=\n[가나다라마]\)|$)'  # 가) 선택지
        ]
        
        for pattern in choice_patterns:
            matches = re.findall(pattern, response, re.MULTILINE)
            if matches:
                choices = [match[1].strip() for match in matches[:3]]  # 최대 3개
                break
        
        if not choices:
            # 단순 나열된 선택지 찾기
            lines = response.split('\n')
            potential_choices = [line.strip() for line in lines if line.strip() and len(line.strip()) < 100]
            choices = potential_choices[:3] if potential_choices else []
        
        parsed['choices'] = choices
        
        # 정답 추출
        answer_patterns = [
            r'정답[:\s]*(\d+)',
            r'답[:\s]*(\d+)',
            r'correct[:\s]*(\d+)',
            r'answer[:\s]*(\d+)'
        ]
        
        for pattern in answer_patterns:
            match = re.search(pattern, response, re.IGNORECASE)
            if match:
                try:
                    parsed['correct_index'] = int(match.group(1)) - 1  # 1-based → 0-based
                    break
                except ValueError:
                    continue
        
        if 'correct_index' not in parsed:
            parsed['correct_index'] = 0  # 기본값
        
        # 해설 추출
        explanation_match = re.search(r'(?:해설|설명|explanation)[:\s]*(.+)', response, re.DOTALL | re.IGNORECASE)
        if explanation_match:
            parsed['explanation'] = explanation_match.group(1).strip()
        
        return parsed
    
    def _adjust_for_grade(self, quiz: Dict, grade: int) -> Dict:
        """학년별 퀴즈 내용 조정"""
        
        grade_config = self.grade_difficulty_map.get(grade, self.grade_difficulty_map[5])
        
        # 문제 지문 길이 조정
        question = quiz.get('question', '')
        if len(question) > grade_config['sentence_length']:
            # 문장을 줄이되 의미는 유지
            sentences = question.split('.')
            if len(sentences) > 1:
                question = sentences[0] + '.'
        
        quiz['question'] = question
        
        # 선택지 단순화 (저학년용)
        if grade <= 3:
            choices = quiz.get('choices', [])
            simplified_choices = []
            for choice in choices:
                # 복잡한 문장 단순화
                if len(choice) > 20:
                    choice = choice.split(',')[0]  # 첫 번째 구문만 사용
                simplified_choices.append(choice)
            quiz['choices'] = simplified_choices
        
        return quiz
    
    def _normalize_choices(self, choices: List[str]) -> List[str]:
        """선택지를 정확히 3개로 정규화"""
        
        # 빈 문자열 제거
        clean_choices = [choice.strip() for choice in choices if choice.strip()]
        
        # 3개 미만인 경우 기본 선택지 추가
        if len(clean_choices) < 3:
            default_choices = ["기타", "모름", "해당없음"]
            while len(clean_choices) < 3:
                for default in default_choices:
                    if default not in clean_choices:
                        clean_choices.append(default)
                        break
                if len(clean_choices) >= 3:
                    break
        
        # 3개 초과인 경우 앞의 3개만 사용
        return clean_choices[:3]
    
    def _validate_correct_index(self, index: int, choices: List[str]) -> int:
        """정답 인덱스 유효성 검증"""
        if 0 <= index < len(choices):
            return index
        return 0  # 기본값: 첫 번째 선택지
    
    def _format_explanation(self, explanation: str, grade: int) -> Optional[str]:
        """해설 포맷팅"""
        if not explanation:
            return None
        
        # 학년별 해설 길이 조정
        max_length = 50 if grade <= 3 else 100 if grade <= 5 else 150
        
        if len(explanation) > max_length:
            # 첫 번째 문장만 사용
            sentences = explanation.split('.')
            if sentences:
                explanation = sentences[0] + '.'
        
        return explanation.strip() if explanation.strip() else None
    
    def _calculate_quality_score(self, question: str, choices: List[str], 
                                explanation: Optional[str], grade: int) -> float:
        """품질 점수 계산"""
        score = 0.0
        
        # 기본 점수 (0.5)
        score += 0.5
        
        # 문제 지문 품질 (0.2)
        if question and len(question.strip()) > 10:
            score += 0.1
        if '?' in question or '무엇' in question or '어떤' in question:
            score += 0.1
        
        # 선택지 품질 (0.2)
        if len(choices) == 3:
            score += 0.1
        if all(len(choice.strip()) > 2 for choice in choices):
            score += 0.1
        
        # 해설 존재 (0.1)
        if explanation and len(explanation.strip()) > 5:
            score += 0.1
        
        return min(1.0, score)
    
    def _create_fallback_quiz(self, spot_info: Dict, request: Dict) -> QuizProblemOutput:
        """오류 시 기본 퀴즈 생성"""
        
        spot_name = spot_info.get('name', '알 수 없는 장소')
        grade = request.get('user_grade', 5)
        
        return QuizProblemOutput(
            problem_type=self.problem_type,
            question=f"{spot_name}에 대한 설명으로 옳은 것은?",
            choices=["역사적 의미가 있다", "문화재로 지정되어 있다", "많은 사람들이 방문한다"],
            correct_index=0,
            explanation=f"{spot_name}은 우리나라의 소중한 문화유산입니다.",
            spot_id=spot_info.get('id'),
            spot_name=spot_name,
            grade=grade,
            quality_score=0.6,  # 기본 품질 점수
            generation_time=0.1,
            created_at=datetime.now()
        )
    
    def format_batch_response(self, quiz_problems: List[QuizProblemOutput]) -> Dict:
        """배치 생성 결과 포맷팅"""
        
        total_count = len(quiz_problems)
        quality_scores = [q.quality_score for q in quiz_problems]
        avg_quality = sum(quality_scores) / total_count if total_count > 0 else 0.0
        
        # 학년별 분포
        grade_distribution = {}
        for quiz in quiz_problems:
            grade = quiz.grade
            grade_distribution[grade] = grade_distribution.get(grade, 0) + 1
        
        # 스팟별 분포
        spot_distribution = {}
        for quiz in quiz_problems:
            spot = quiz.spot_name or "알 수 없는 스팟"
            spot_distribution[spot] = spot_distribution.get(spot, 0) + 1
        
        return {
            "batch_summary": {
                "total_problems": total_count,
                "average_quality": round(avg_quality, 3),
                "grade_distribution": grade_distribution,
                "spot_distribution": spot_distribution
            },
            "problems": [quiz.to_dict() for quiz in quiz_problems],
            "metadata": {
                "generation_time": datetime.now().isoformat(),
                "format_version": "QuizProblem-v2.0",
                "compliance": {
                    "backend_entity": "QuizProblem",
                    "choices_count": 3,
                    "problem_type": "QUIZ"
                }
            }
        }
    
    def format_api_response(self, quiz_problem: QuizProblemOutput) -> Dict:
        """FastAPI 응답 포맷팅"""
        
        return {
            "success": True,
            "data": {
                "problem": quiz_problem.to_dict(),
                "backend_format": {
                    "problem_type": quiz_problem.problem_type,
                    "question": quiz_problem.question,
                    "choices": quiz_problem.choices,
                    "correct_index": quiz_problem.correct_index,
                    "explanation": quiz_problem.explanation
                }
            },
            "metadata": {
                "generation_time": quiz_problem.generation_time,
                "quality_score": quiz_problem.quality_score,
                "grade": quiz_problem.grade,
                "timestamp": quiz_problem.created_at.isoformat() if quiz_problem.created_at else None
            }
        }
    
    def format_error_response(self, error_message: str, spot_info: Dict = None) -> Dict:
        """에러 응답 포맷팅"""
        
        return {
            "success": False,
            "error": {
                "message": error_message,
                "timestamp": datetime.now().isoformat(),
                "spot_info": spot_info
            },
            "fallback": self._create_fallback_quiz(
                spot_info or {}, {"user_grade": 5}
            ).to_dict() if spot_info else None
        }