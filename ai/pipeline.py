import re
import json
import logging
from typing import List, Dict, Optional, Tuple, Set
from dataclasses import dataclass
import asyncio
from datetime import datetime
import difflib

logger = logging.getLogger(__name__)

@dataclass
class ProcessedDocument:
    """전처리된 문서 데이터 클래스"""
    original_text: str
    cleaned_text: str
    keywords: List[str]
    educational_level: int
    content_type: str
    safety_score: float
    metadata: Dict

class DataPreprocessor:
    """데이터 전처리 클래스"""
    
    def __init__(self):
        # 교육 부적절 키워드 (안전성 검증용)
        self.inappropriate_keywords = {
            '위험', '폭력', '무기', '혈액', '죽음', '공포', 
            '귀신', '괴물', '무서운', '끔찍한'
        }
        
        # 초등학생 적합 어휘 사전
        self.elementary_vocabulary = {
            '건축물': '건물', '유물': '옛날 물건', '문화재': '문화유산',
            '조성': '만들어짐', '건립': '세워짐', '창건': '처음 지음'
        }
        
        # 지역별 GPS 범위 (서울 기준)
        self.seoul_bounds = {
            'lat_min': 37.4, 'lat_max': 37.7,
            'lon_min': 126.8, 'lon_max': 127.2
        }
    
    def validate_gps_coordinates(self, lat: float, lon: float, region: str = "서울") -> bool:
        """GPS 좌표 유효성 검증"""
        if region == "서울":
            bounds = self.seoul_bounds
            return (bounds['lat_min'] <= lat <= bounds['lat_max'] and 
                   bounds['lon_min'] <= lon <= bounds['lon_max'])
        return True  # 다른 지역은 일단 통과
    
    def clean_text(self, text: str) -> str:
        """텍스트 정규화 및 정제"""
        if not text:
            return ""
        
        # 1. 기본 정규화
        text = text.strip()
        text = re.sub(r'\s+', ' ', text)  # 중복 공백 제거
        text = re.sub(r'[^\w\s가-힣.,()[\]-]', '', text)  # 특수문자 제거
        
        # 2. 불필요한 접두사/접미사 제거
        prefixes_to_remove = ['국가지정', '시도지정', '등록문화재']
        for prefix in prefixes_to_remove:
            text = text.replace(prefix, '').strip()
        
        # 3. 초등학생 친화적 어휘로 변경
        for complex_word, simple_word in self.elementary_vocabulary.items():
            text = text.replace(complex_word, simple_word)
        
        return text
    
    def extract_keywords(self, text: str) -> List[str]:
        """핵심 키워드 추출"""
        # 간단한 키워드 추출 (명사 위주)
        keywords = []
        
        # 장소 관련 키워드
        location_patterns = [
            r'(\w+궁)', r'(\w+사)', r'(\w+원)', r'(\w+관)', 
            r'(\w+당)', r'(\w+루)', r'(\w+전)'
        ]
        
        for pattern in location_patterns:
            matches = re.findall(pattern, text)
            keywords.extend(matches)
        
        # 시대 키워드
        period_keywords = ['조선', '고려', '신라', '백제', '가야', '통일신라']
        for period in period_keywords:
            if period in text:
                keywords.append(period)
        
        return list(set(keywords))  # 중복 제거
    
    def calculate_safety_score(self, text: str) -> float:
        """안전성 점수 계산"""
        text_lower = text.lower()
        inappropriate_count = sum(1 for keyword in self.inappropriate_keywords 
                                if keyword in text_lower)
        
        # 부적절 키워드가 많을수록 낮은 점수
        safety_score = max(0.0, 1.0 - (inappropriate_count * 0.2))
        return safety_score
    
    def determine_educational_level(self, text: str) -> int:
        """교육 수준 판정 (1-6학년)"""
        # 텍스트 복잡도 기반 판정
        sentence_count = len(text.split('.'))
        avg_sentence_length = len(text) / max(1, sentence_count)
        
        # 어려운 한자어 비율
        hanja_pattern = r'[一-龯]+'
        hanja_count = len(re.findall(hanja_pattern, text))
        hanja_ratio = hanja_count / max(1, len(text))
        
        # 복잡도 계산
        complexity_score = (avg_sentence_length / 50) + (hanja_ratio * 10)
        
        if complexity_score < 1.0:
            return 1  # 1-2학년
        elif complexity_score < 2.0:
            return 3  # 3-4학년
        else:
            return 5  # 5-6학년
    
    async def preprocess_heritage_data(self, raw_data: Dict) -> List[ProcessedDocument]:
        """문화재 데이터 전체 전처리"""
        processed_docs = []
        
        logger.info("🔄 데이터 전처리 시작...")
        
        for i, spot in enumerate(raw_data.get('스팟', [])):
            try:
                # 1. 기본 정보 추출
                name = spot.get('이름', '')
                location = spot.get('위치', '')
                description = spot.get('설명', '')
                
                # 2. GPS 좌표 검증
                gps = spot.get('GPS', [0, 0])
                if len(gps) == 2 and not self.validate_gps_coordinates(gps[0], gps[1]):
                    logger.warning(f"잘못된 GPS 좌표: {name} - {gps}")
                    continue
                
                # 3. 텍스트 정제
                original_text = f"{name} {location} {description}"
                cleaned_text = self.clean_text(original_text)
                
                if len(cleaned_text) < 10:  # 너무 짧은 텍스트 제외
                    continue
                
                # 4. 키워드 추출
                keywords = self.extract_keywords(cleaned_text)
                
                # 5. 안전성 및 교육 수준 평가
                safety_score = self.calculate_safety_score(cleaned_text)
                educational_level = self.determine_educational_level(cleaned_text)
                
                # 6. 안전성 필터링
                if safety_score < 0.7:
                    logger.warning(f"안전성 부족으로 제외: {name} (점수: {safety_score})")
                    continue
                
                # 7. 처리된 문서 생성
                processed_doc = ProcessedDocument(
                    original_text=original_text,
                    cleaned_text=cleaned_text,
                    keywords=keywords,
                    educational_level=educational_level,
                    content_type=spot.get('지정종목', '기타'),
                    safety_score=safety_score,
                    metadata={
                        'name': name,
                        'location': location,
                        'gps': gps,
                        'category': spot.get('지정종목', ''),
                        'detailed_class': spot.get('상세분류', ''),
                        'education_link': spot.get('교육과정_연계', '')
                    }
                )
                
                processed_docs.append(processed_doc)
                
                if (i + 1) % 100 == 0:
                    logger.info(f"전처리 진행: {i + 1}개 완료")
                    
            except Exception as e:
                logger.error(f"전처리 오류 - {spot.get('이름', 'Unknown')}: {e}")
                continue
        
        logger.info(f"✅ 전처리 완료: {len(processed_docs)}개 문서")
        return processed_docs


class MissionPostprocessor:
    """미션 후처리 클래스"""
    
    def __init__(self):
        # 필수 포함 요소
        self.required_elements = {
            '퀴즈': ['문제', '선택지', '정답'],
            '관찰미션': ['관찰 대상', '활동 순서'],
            '체험미션': ['체험 활동', '역할 분담']
        }
        
        # 금지 단어
        self.forbidden_words = {
            '위험한', '무서운', '혼자서', '어둠', '밤'
        }
        
        # 표준 형식 템플릿
        self.format_templates = {
            '퀴즈': {
                'title_pattern': r'🎯\s*퀴즈\s*제목\s*:',
                'question_pattern': r'📝\s*문제\s*:',
                'answer_pattern': r'✅\s*정답\s*:'
            }
        }
    
    def validate_mission_format(self, content: str, mission_type: str) -> Tuple[bool, List[str]]:
        """미션 형식 검증"""
        issues = []
        
        # 1. 필수 요소 확인
        required = self.required_elements.get(mission_type, [])
        for element in required:
            if element not in content:
                issues.append(f"필수 요소 누락: {element}")
        
        # 2. 금지 단어 확인
        content_lower = content.lower()
        found_forbidden = [word for word in self.forbidden_words 
                          if word in content_lower]
        if found_forbidden:
            issues.append(f"부적절한 단어 발견: {', '.join(found_forbidden)}")
        
        # 3. 길이 검증
        if len(content) < 100:
            issues.append("내용이 너무 짧음")
        elif len(content) > 2000:
            issues.append("내용이 너무 김")
        
        return len(issues) == 0, issues
    
    def standardize_format(self, content: str, mission_type: str) -> str:
        """형식 표준화"""
        if mission_type not in self.format_templates:
            return content
        
        # 이모지 및 형식 통일
        template = self.format_templates[mission_type]
        
        for key, pattern in template.items():
            if not re.search(pattern, content):
                continue
            
            # 형식 교정
            if 'title' in key:
                content = re.sub(r'(퀴즈\s*제목\s*:)', '🎯 퀴즈 제목:', content)
            elif 'question' in key:
                content = re.sub(r'(문제\s*:)', '📝 문제:', content)
            elif 'answer' in key:
                content = re.sub(r'(정답\s*:)', '✅정답:', content)
        
        return content
    
    def enhance_educational_value(self, content: str, grade: int) -> str:
        """교육적 가치 향상"""
        # 학년별 어휘 조정
        grade_vocabulary = {
            3: {'건축물': '건물', '유물': '옛날 물건'},
            4: {'문화재': '문화유산', '조성': '만들어짐'},
            5: {'건립': '세워짐', '창건': '처음 지음'},
            6: {}  # 6학년은 원문 유지
        }
        
        vocab = grade_vocabulary.get(grade, {})
        for complex_word, simple_word in vocab.items():
            content = content.replace(complex_word, simple_word)
        
        # 학습 목표 추가
        if '학습 목표' not in content and '💡' not in content:
            content += f"\n\n💡 이 활동을 통해 {grade}학년 수준의 역사와 문화를 배울 수 있어요!"
        
        return content
    
    def add_safety_guidelines(self, content: str) -> str:
        """안전 수칙 추가"""
        safety_note = """
⚠️ 안전 수칙:
- 선생님과 함께 행동하세요
- 문화재에 손대지 마세요  
- 정해진 장소에서만 활동하세요
- 위험한 곳에는 가지 마세요"""
        
        if '안전' not in content:
            content += safety_note
        
        return content
    
    async def postprocess_mission(self, content: str, mission_type: str, 
                                 grade: int, quality_score: float) -> Dict:
        """종합적인 미션 후처리"""
        
        logger.info(f"🔧 미션 후처리 시작: {mission_type}")
        
        # 1. 형식 검증
        is_valid, issues = self.validate_mission_format(content, mission_type)
        
        if not is_valid:
            logger.warning(f"형식 문제 발견: {issues}")
            # 자동 교정 시도
            content = self.auto_fix_format(content, mission_type, issues)
        
        # 2. 형식 표준화
        content = self.standardize_format(content, mission_type)
        
        # 3. 교육적 가치 향상
        content = self.enhance_educational_value(content, grade)
        
        # 4. 안전 수칙 추가
        content = self.add_safety_guidelines(content)
        
        # 5. 최종 품질 점수 재계산
        final_score = self.calculate_final_quality_score(content, quality_score)
        
        # 6. 메타데이터 생성
        metadata = {
            'format_valid': is_valid,
            'format_issues': issues,
            'content_length': len(content),
            'safety_enhanced': True,
            'grade_appropriate': True,
            'final_quality_score': final_score
        }
        
        logger.info(f"✅ 후처리 완료 - 최종 점수: {final_score:.2f}")
        
        return {
            'processed_content': content,
            'metadata': metadata,
            'quality_score': final_score
        }
    
    def auto_fix_format(self, content: str, mission_type: str, issues: List[str]) -> str:
        """자동 형식 교정"""
        for issue in issues:
            if '필수 요소 누락' in issue:
                if '문제' in issue and mission_type == '퀴즈':
                    content = "📝 문제: " + content
                elif '정답' in issue and mission_type == '퀴즈':
                    content += "\n✅ 정답: 1번"
            
            elif '부적절한 단어' in issue:
                for word in self.forbidden_words:
                    content = content.replace(word, '')
        
        return content
    
    def calculate_final_quality_score(self, content: str, initial_score: float) -> float:
        """최종 품질 점수 계산"""
        bonus_points = 0.0
        
        # 보너스 포인트 계산
        if '🎯' in content:  # 이모지 사용
            bonus_points += 0.05
        if '안전' in content:  # 안전 요소
            bonus_points += 0.1
        if '학습' in content or '배울' in content:  # 학습 목표
            bonus_points += 0.1
        if 100 <= len(content) <= 800:  # 적절한 길이
            bonus_points += 0.05
        
        return min(1.0, initial_score + bonus_points)


# 통합된 고도화 파이프라인
class EnhancedRAGPipeline:
    """전처리/후처리가 통합된 고도화 RAG 파이프라인"""
    
    def __init__(self):
        self.preprocessor = DataPreprocessor()
        self.postprocessor = MissionPostprocessor()
        self.processed_documents = []
        
    async def initialize_with_preprocessing(self, raw_data_file: str):
        """전처리 포함 초기화"""
        logger.info("🚀 전처리 통합 파이프라인 초기화")
        
        # 1. 원본 데이터 로드
        with open(raw_data_file, 'r', encoding='utf-8') as f:
            raw_data = json.load(f)
        
        # 2. 데이터 전처리
        self.processed_documents = await self.preprocessor.preprocess_heritage_data(raw_data)
        
        logger.info(f"✅ 초기화 완료: {len(self.processed_documents)}개 문서 준비")
        
        return True
    
    async def generate_mission_with_full_processing(self, request) -> Dict:
        """전체 전처리/후처리 포함 미션 생성"""
        
        # 1. 기본 미션 생성 (기존 로직)
        # ... (검색, LLM 호출 등)
        
        # 2. 후처리 적용
        result = await self.postprocessor.postprocess_mission(
            content="샘플 미션 내용",  # 실제로는 LLM 출력
            mission_type="퀴즈",
            grade=5,
            quality_score=0.8
        )
        
        return result

# 사용 예시
async def test_enhanced_pipeline():
    """향상된 파이프라인 테스트"""
    pipeline = EnhancedRAGPipeline()
    
    # 전처리 포함 초기화
    await pipeline.initialize_with_preprocessing("heritage_complete_database.json")
    
    # 전체 처리 테스트
    result = await pipeline.generate_mission_with_full_processing(None)
    
    print("처리된 미션:")
    print(result['processed_content'])
    print(f"품질 점수: {result['quality_score']}")

if __name__ == "__main__":
    asyncio.run(test_enhanced_pipeline())