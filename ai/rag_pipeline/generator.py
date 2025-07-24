# rag_pipeline/smart_generator.py - 업그레이드된 스마트 미션 생성기
import logging
from typing import Dict, List, Tuple
from dataclasses import dataclass
from .llm_client import LLMClient

logger = logging.getLogger(__name__)

@dataclass
class MissionTypeScore:
   """미션 타입별 적합도 점수"""
   mission_type: str
   score: float  # 0.0 ~ 1.0
   reasoning: str  # 점수 산출 근거

class SmartMissionGenerator:
   """업그레이드된 LLM 기반 스마트 미션 생성기"""
   
   def __init__(self):
       self.llm_client = LLMClient()
       
       # enhanced_generator.py의 장소 특성 분석 로직 추가
       self.location_weights = {
           # 건축물 특성 (궁궐, 전각 등)
           "architecture": {
               "체험미션": 0.9,    # 건축 공간 체험
               "퀴즈": 0.8,        # 건축 양식, 역사 퀴즈
               "관찰미션": 0.7,    # 장식, 구조 관찰
               "사진미션": 0.8     # 건물 각도별 촬영
           },
           # 유물/유적 특성
           "artifacts": {
               "퀴즈": 0.9,        # 지식 기반 문제
               "관찰미션": 0.8,    # 세부 관찰
               "체험미션": 0.6,    # 역할놀이
               "사진미션": 0.7     # 각도별 촬영
           },
           # 자연 유산 특성
           "nature": {
               "관찰미션": 0.9,    # 생태 관찰
               "사진미션": 0.8,    # 자연 촬영
               "퀴즈": 0.7,        # 생태 지식
               "체험미션": 0.7     # 자연 체험
           },
           # 동물원/생태원 특성
           "animals": {
               "관찰미션": 0.9,    # 동물 행동 관찰
               "퀴즈": 0.8,        # 동물 지식
               "체험미션": 0.8,    # 먹이주기 체험
               "사진미션": 0.7     # 동물 촬영
           },
           # 박물관 특성
           "museum": {
               "퀴즈": 0.9,        # 전시품 퀴즈
               "관찰미션": 0.8,    # 전시품 관찰
               "체험미션": 0.6,    # 체험 전시
               "사진미션": 0.5     # 촬영 제한
           }
       }
       
       # enhanced_generator.py의 학년별 선호도 추가
       self.grade_preferences = {
           3: {  # 3학년 - 체험 중심
               "체험미션": 1.0,
               "사진미션": 0.9,
               "관찰미션": 0.8,
               "퀴즈": 0.6
           },
           4: {  # 4학년 - 균형
               "체험미션": 0.9,
               "관찰미션": 0.9,
               "퀴즈": 0.7,
               "사진미션": 0.8
           },
           5: {  # 5학년 - 학습 증가
               "퀴즈": 0.9,
               "관찰미션": 0.9,
               "체험미션": 0.8,
               "사진미션": 0.7
           },
           6: {  # 6학년 - 고차원 사고
               "퀴즈": 1.0,
               "관찰미션": 0.8,
               "체험미션": 0.7,
               "사진미션": 0.6
           }
       }
       
       # 미션 타입별 프롬프트 템플릿 (기존 유지)
       self.prompt_templates = {
           "퀴즈": """
초등학교 {grade}학년 {group_size}명이 {spot_name}에서 {duration}분간 수행할 퀴즈 미션을 만들어주세요.

장소 정보:
- 이름: {spot_name}
- 위치: {location}
- 설명: {description}
- 교육과정: {education_link}

요구사항:
- {grade}학년 수준에 맞는 난이도
- {duration}분 내에 완료 가능한 분량
- 객관식 3지선다 문제
- 현장에서 관찰 가능한 내용 기반
- 안전하고 교육적인 내용

출력 형식:
🎯 미션 제목: [창의적 제목]
📝 문제: [문제 내용]
① [선택지1] ② [선택지2] ③ [선택지3]
✅ 정답: [번호] - [간단한 해설]
💡 추가 학습: [관련 지식이나 관찰 포인트]
⏰ 예상 시간: {duration}분

⚠️ 안전 수칙:
- 선생님과 함께 활동하세요
- 문화재에 손대지 마세요
- 정해진 구역에서만 활동하세요
""",

           "관찰미션": """
초등학교 {grade}학년 {group_size}명이 {spot_name}에서 {duration}분간 수행할 관찰 미션을 만들어주세요.

장소 정보:
- 이름: {spot_name}
- 위치: {location}
- 설명: {description}
- 교육과정: {education_link}

요구사항:
- {grade}학년이 흥미롭게 관찰할 수 있는 활동
- {group_size}명이 협력하여 수행
- {duration}분 내에 완료 가능
- 구체적이고 실행 가능한 관찰 포인트

출력 형식:
🎯 미션 제목: [관찰 미션명]
👀 관찰 대상: [구체적 관찰 포인트들]
📝 관찰 활동:
1. [1단계 - 구체적 관찰 활동]
2. [2단계 - 팀 협력 활동]
3. [3단계 - 기록 및 공유]
📊 기록 방법: [관찰 결과를 어떻게 기록할지]
🤝 팀 활동: [{group_size}명의 역할 분담]
⏰ 예상 시간: {duration}분

⚠️ 안전 수칙:
- 선생님과 함께 활동하세요
- 안전한 거리에서 관찰하세요
- 위험한 곳에는 가지 마세요
""",

           "체험미션": """
초등학교 {grade}학년 {group_size}명이 {spot_name}에서 {duration}분간 수행할 체험 미션을 만들어주세요.

장소 정보:
- 이름: {spot_name}
- 위치: {location}
- 설명: {description}
- 교육과정: {education_link}

요구사항:
- {grade}학년이 안전하게 체험할 수 있는 활동
- {group_size}명의 역할 분담 포함
- {duration}분 내에 완료 가능
- 역사적/교육적 의미가 있는 체험
- 현실적으로 실행 가능한 활동

출력 형식:
🎯 미션 제목: [체험 활동명]
🎭 역할 분담: [{group_size}명의 각자 역할]
🎮 체험 순서:
1. [준비 단계 - 구체적 준비사항]
2. [실행 단계 - 체험 활동]
3. [마무리 단계 - 소감 나누기]
🏆 목표: [이 체험을 통해 달성하고자 하는 교육 목표]
📚 학습 효과: [예상되는 교육적 효과]
⏰ 예상 시간: {duration}분

⚠️ 안전 수칙:
- 선생님의 지시에 따라 활동하세요
- 안전한 체험만 진행하세요
- 문화재 보호 수칙을 지켜주세요
""",

           "사진미션": """
초등학교 {grade}학년 {group_size}명이 {spot_name}에서 {duration}분간 수행할 사진 미션을 만들어주세요.

장소 정보:
- 이름: {spot_name}
- 위치: {location}
- 설명: {description}
- 교육과정: {education_link}

요구사항:
- {grade}학년이 안전하게 촬영할 수 있는 활동
- 교육적 의미가 있는 촬영 미션
- {group_size}명이 협력하여 수행
- {duration}분 내에 완료 가능
- 창의적이고 재미있는 촬영 아이디어

출력 형식:
🎯 미션 제목: [사진 미션명]
📸 촬영 대상: [무엇을 촬영할지]
📐 촬영 요구사항:
1. [촬영 각도나 구도 1]
2. [촬영 포즈나 표현 2]
3. [창의적 아이디어 3]
🎨 창의 포인트: [독창적인 촬영 아이디어]
📱 촬영 후 활동: [사진을 찍은 후 할 활동]
⏰ 예상 시간: {duration}분

⚠️ 안전 수칙:
- 안전한 장소에서만 촬영하세요
- 문화재 촬영 규칙을 지켜주세요
- 다른 관람객에게 방해되지 않게 하세요
"""
       }
   
   def analyze_location_characteristics(self, spot_info: Dict) -> Dict[str, float]:
       """enhanced_generator.py의 장소 특성 분석 로직"""
       characteristics = {
           "architecture": 0.0,
           "artifacts": 0.0,
           "nature": 0.0,
           "animals": 0.0,
           "museum": 0.0
       }
       
       name = spot_info.get('name', '').lower()
       category = spot_info.get('category', '').lower()
       description = spot_info.get('description', '').lower()
       
       # 키워드 기반 특성 점수 계산
       if any(keyword in name + category for keyword in ['궁', '전', '루', '당', '문', '성']):
           characteristics["architecture"] += 1.0
           
       if any(keyword in name + category for keyword in ['상', '탑', '비', '석', '유물', '유적']):
           characteristics["artifacts"] += 1.0
           
       if any(keyword in category for keyword in ['천연', '명승', '자연', '숲', '강', '산']):
           characteristics["nature"] += 1.0
           
       if any(keyword in name + description for keyword in ['동물', '사슴', '낙타', '원숭이', '새', '생태']):
           characteristics["animals"] += 1.0
           
       if any(keyword in name + category for keyword in ['박물관', '기념관', '전시', '자료관']):
           characteristics["museum"] += 1.0
       
       logger.info(f"장소 특성 분석: {characteristics}")
       return characteristics
   
   def calculate_mission_type_scores(self, spot_info: Dict, grade: int, group_size: int, duration: int) -> List[MissionTypeScore]:
       """enhanced_generator.py의 점수 계산 로직 적용"""
       scores = []
       
       # 1. 장소 특성 분석
       location_chars = self.analyze_location_characteristics(spot_info)
       
       # 2. 각 미션 타입별 점수 계산
       for mission_type in ["퀴즈", "관찰미션", "체험미션", "사진미션"]:
           total_score = 0.0
           reasoning_parts = []
           
           # 장소 특성 기반 점수
           location_score = 0.0
           for char_type, char_weight in location_chars.items():
               if char_weight > 0:
                   type_weight = self.location_weights[char_type].get(mission_type, 0.5)
                   location_score += char_weight * type_weight
                   reasoning_parts.append(f"{char_type}({type_weight:.1f})")
           
           # 학년별 선호도
           grade_score = self.grade_preferences.get(grade, {}).get(mission_type, 0.5)
           reasoning_parts.append(f"학년적합({grade_score:.1f})")
           
           # 그룹 크기 고려
           group_bonus = 0.0
           if mission_type in ["체험미션", "관찰미션"] and group_size >= 4:
               group_bonus = 0.1  # 체험/관찰은 그룹 활동에 적합
               reasoning_parts.append("그룹활동(+0.1)")
           elif mission_type == "퀴즈" and group_size <= 2:
               group_bonus = 0.1  # 퀴즈는 소규모에 적합
               reasoning_parts.append("소규모(+0.1)")
           
           # 시간 제약 고려
           time_bonus = 0.0
           if duration <= 20:  # 짧은 시간
               if mission_type in ["퀴즈", "사진미션"]:
                   time_bonus = 0.1
                   reasoning_parts.append("단시간(+0.1)")
           elif duration >= 45:  # 긴 시간
               if mission_type in ["체험미션", "관찰미션"]:
                   time_bonus = 0.1
                   reasoning_parts.append("장시간(+0.1)")
           
           # 최종 점수 계산
           total_score = (location_score * 0.6 + grade_score * 0.3 + group_bonus + time_bonus)
           total_score = min(1.0, total_score)  # 최대 1.0으로 제한
           
           reasoning = f"장소특성({location_score:.2f}) + " + " + ".join(reasoning_parts)
           
           scores.append(MissionTypeScore(
               mission_type=mission_type,
               score=total_score,
               reasoning=reasoning
           ))
       
       # 점수 순으로 정렬
       scores.sort(key=lambda x: x.score, reverse=True)
       logger.info(f"미션 타입 점수: {[(s.mission_type, f'{s.score:.2f}') for s in scores]}")
       return scores
   
   def auto_determine_mission_type(self, spot_info: Dict, grade: int = 5, group_size: int = 4, duration: int = 30) -> Tuple[str, str]:
       """enhanced_generator.py의 점수 계산 로직을 적용한 자동 미션 타입 결정"""
       
       # 점수 기반 결정
       scores = self.calculate_mission_type_scores(spot_info, grade, group_size, duration)
       
       if scores:
           best_mission = scores[0]
           logger.info(f"자동 결정된 미션 타입: {best_mission.mission_type} (점수: {best_mission.score:.2f})")
           logger.info(f"결정 근거: {best_mission.reasoning}")
           return best_mission.mission_type, best_mission.reasoning
       
       # 폴백: 기존 규칙 기반
       name = spot_info.get('name', '').lower()
       category = spot_info.get('category', '').lower()
       
       if any(keyword in name + category for keyword in ['궁', '전', '루', '당', '문']):
           return "체험미션", "궁궐/건축물 특성"
       elif any(keyword in name + category for keyword in ['상', '탑', '비', '석']):
           return "퀴즈", "유물/유적 특성"
       elif any(keyword in category for keyword in ['천연', '명승', '동물', '생태']):
           return "관찰미션", "자연/생태 특성"
       elif any(keyword in name + category for keyword in ['박물관', '기념관', '전시']):
           return "퀴즈", "박물관/전시 특성"
       else:
           return "퀴즈", "기본값"
   
   async def generate_smart_mission(self, spot_info: Dict, mission_type: str = None, 
                                  grade: int = 5, group_size: int = 4, duration: int = 30) -> Dict:
       """업그레이드된 LLM 스마트 미션 생성"""
       
       # 미션 타입 자동 결정 (지정되지 않은 경우)
       reasoning = ""
       if not mission_type:
           mission_type, reasoning = self.auto_determine_mission_type(spot_info, grade, group_size, duration)
       
       logger.info(f"🎯 업그레이드된 스마트 미션 생성: {spot_info.get('name')} - {mission_type}")
       if reasoning:
           logger.info(f"📊 자동 결정 근거: {reasoning}")
       
       try:
           # 프롬프트 생성
           template = self.prompt_templates.get(mission_type, self.prompt_templates["퀴즈"])
           
           prompt = template.format(
               grade=grade,
               group_size=group_size,
               duration=duration,
               spot_name=spot_info.get('name', ''),
               location=spot_info.get('location', ''),
               description=spot_info.get('description', '')[:300],  # 너무 길면 잘라냄
               education_link=spot_info.get('education_link', '')
           )
           
           # LLM으로 미션 생성
           mission_content = await self.llm_client.generate_mission(prompt)
           
           # 품질 평가
           quality_eval = await self.llm_client.evaluate_mission_quality(mission_content, mission_type)
           quality_score = quality_eval.get("총점", 0) / 50.0  # 0-1 스케일로 정규화
           
           return {
               'content': mission_content,
               'mission_type': mission_type,
               'grade': grade,
               'quality_score': quality_score,
               'quality_eval': quality_eval,
               'safety_included': True,
               'ai_generated': True,
               'auto_determination_reasoning': reasoning,
               'mission_type_scores': self.calculate_mission_type_scores(spot_info, grade, group_size, duration)
           }
           
       except Exception as e:
           logger.error(f"스마트 미션 생성 실패: {e}")
           # 폴백: 기본 템플릿 사용
           return self._create_fallback_mission(spot_info, mission_type, grade, duration)
   
   def _create_fallback_mission(self, spot_info: Dict, mission_type: str, grade: int, duration: int) -> Dict:
       """LLM 실패 시 폴백 미션"""
       logger.warning("LLM 실패로 폴백 미션 생성")
       
       content = f"""🎯 미션: {spot_info.get('name', '')} 탐험

📍 위치: {spot_info.get('location', '')}
📖 설명: {spot_info.get('description', '')[:200]}

🎮 활동: 팀원들과 함께 이곳의 특징을 찾아보세요!
⏰ 시간: {duration}분

⚠️ 안전 수칙:
- 선생님과 함께 활동하세요
- 문화재에 손대지 마세요
- 정해진 구역에서만 활동하세요"""
       
       return {
           'content': content,
           'mission_type': mission_type,
           'grade': grade,
           'quality_score': 0.6,  # 폴백은 낮은 점수
           'safety_included': True,
           'ai_generated': False,
           'fallback_used': True
       }