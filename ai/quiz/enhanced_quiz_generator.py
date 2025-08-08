# enhanced_quiz_generator.py - 품질 대폭 개선 버전

def create_dynamic_prompt(spot_info: Dict, grade: int, quiz_number: int = 1) -> str:
    """동적 다각도 프롬프트 생성 - 품질 개선의 핵심"""
    
    spot_name = spot_info.get("세부스팟", "이곳")
    location = spot_info.get("메인장소", "")
    description = spot_info.get("설명", "")
    keywords = spot_info.get("교육키워드", [])
    
    # 1. 학년별 언어 수준 정밀 조정
    if grade <= 2:
        vocab_level = "유치원~2학년 수준의 한글 기초 어휘"
        complexity = "한 번에 이해할 수 있는 단순한"
        examples = "색깔, 모양, 크기, 위치"
    elif grade <= 4:
        vocab_level = "3-4학년 교과서 핵심 어휘"
        complexity = "생각해보면 알 수 있는"
        examples = "용도, 특징, 비교, 관계"
    else:
        vocab_level = "5-6학년 사회/과학 교과서 용어"
        complexity = "분석과 추론이 필요한"
        examples = "원인, 결과, 의미, 가치"

    # 2. 다각도 관점 순환 시스템 (질문 다양성 보장)
    perspectives = [
        f"{spot_name}의 시각적 특징과 외관",
        f"{spot_name}에서 할 수 있는 체험과 활동", 
        f"{spot_name}의 역사적 배경과 의미",
        f"{spot_name}의 문화적 가치와 교육적 의미",
        f"{spot_name}과 관련된 인물이나 사건",
        f"{spot_name}의 건축적 특징이나 구조",
        f"{spot_name}이 현재 우리에게 주는 교훈"
    ]
    
    current_perspective = perspectives[(quiz_number - 1) % len(perspectives)]
    
    # 3. 키워드 기반 맥락 강화
    context_boost = ""
    if keywords:
        main_keywords = keywords[:3]
        context_boost = f"핵심 연관어: {', '.join(main_keywords)}"
    
    # 4. 품질 강화 지시사항 (구체적 금지사항 명시)
    quality_guidelines = f"""
🚫 절대 금지사항:
- '놀이동산', '롤러코스터', '편의점', '카페' 등 부적절한 선택지
- '{grade}학년이 모르는 어려운 한자어나 전문용어' 
- '아마도', '추정컨대' 등 불확실한 표현
- 정답이 애매하거나 논란의 여지가 있는 내용

✅ 필수 포함사항:
- {vocab_level}만 사용
- 현장에서 직접 관찰 가능한 내용 위주
- 정답 근거가 명확하고 객관적인 사실
- 학년 수준에 맞는 {complexity} 난이도
"""

    # 5. 출력 형식 엄격 지정
    format_instruction = """
📋 **정확한 출력 형식 (한 글자도 틀리면 안됨):**
문제: [질문 내용]
1) [선택지1]
2) [선택지2] 
3) [선택지3]
정답: [1, 2, 3 중 번호만]
해설: [한 문장으로 간단명료하게]
"""

    return f"""당신은 초등학교 현장학습 전문 교육자입니다. {grade}학년 학생들을 위한 최고 품질의 삼지선다 퀴즈를 만들어주세요.

📍 **장소 정보:**
- 위치: {location} 
- 세부 장소: {spot_name}
- 설명: {description}
- {context_boost}

🎯 **이번 문제 관점:** {current_perspective}

{quality_guidelines}

{format_instruction}

위 형식과 지침을 정확히 지켜서 {grade}학년이 현장에서 즐겁게 풀 수 있는 퀴즈 1개를 만들어주세요."""


def enhanced_quiz_parser(llm_response: str, spot_info: Dict, grade: int) -> Optional[Dict]:
    """강화된 퀴즈 파싱 + 품질 검증"""
    
    try:
        # 기본 파싱
        parsed = basic_parse_quiz(llm_response)
        if not parsed:
            return None
            
        # 고급 품질 검증
        quality_score = calculate_advanced_quality(parsed, spot_info, grade)
        
        if quality_score < 0.7:  # 품질 기준 상향
            logger.warning(f"품질 기준 미달: {quality_score:.2f}")
            return None
            
        parsed["quality_score"] = quality_score
        parsed["generation_method"] = "gpt_enhanced"
        
        return parsed
        
    except Exception as e:
        logger.error(f"파싱 실패: {e}")
        return None


def calculate_advanced_quality(quiz_data: Dict, spot_info: Dict, grade: int) -> float:
    """다층 품질 평가 시스템"""
    
    score = 0.0
    question = quiz_data.get("question", "")
    choices = quiz_data.get("choices", [])
    explanation = quiz_data.get("explanation", "")
    
    # 1. 기본 구조 검증 (30%)
    if len(question) >= 15 and len(choices) == 3 and len(explanation) >= 20:
        score += 0.3
    
    # 2. 학년별 어휘 적합성 검증 (25%)
    grade_words = get_grade_appropriate_words(grade)
    difficult_words = get_difficult_words_for_grade(grade)
    
    total_text = f"{question} {' '.join(choices)} {explanation}"
    
    # 적절한 어휘 사용률
    appropriate_word_count = sum(1 for word in grade_words if word in total_text)
    inappropriate_word_count = sum(1 for word in difficult_words if word in total_text)
    
    vocab_score = min(appropriate_word_count * 0.05, 0.2) - inappropriate_word_count * 0.1
    score += max(0, vocab_score) + 0.05  # 기본 점수
    
    # 3. 교육적 연관성 (20%)
    keywords = spot_info.get("교육키워드", [])
    spot_name = spot_info.get("세부스팟", "")
    
    if spot_name in total_text:
        score += 0.1
    
    keyword_matches = sum(1 for kw in keywords if kw in total_text)
    score += min(keyword_matches * 0.05, 0.1)
    
    # 4. 부적절한 내용 감점 (25%)
    inappropriate_terms = [
        "놀이동산", "롤러코스터", "편의점", "카페", "쇼핑몰", 
        "게임", "스마트폰", "맥도날드", "피자"
    ]
    
    penalty = sum(0.15 for term in inappropriate_terms if term in total_text)
    score -= penalty
    
    # 5. 현장학습 적합성 보너스
    field_study_terms = [
        "관찰", "견학", "체험", "학습", "역사", "문화", "전통", 
        "건축", "예술", "교육", "의미"
    ]
    
    bonus = sum(0.02 for term in field_study_terms if term in total_text)
    score += min(bonus, 0.1)
    
    return max(0.0, min(1.0, score))


def get_grade_appropriate_words(grade: int) -> List[str]:
    """학년별 적절한 어휘 리스트"""
    if grade <= 2:
        return ["색깔", "크기", "모양", "위치", "이름", "보기", "찾기", "같은", "다른"]
    elif grade <= 4:
        return ["특징", "역할", "중요한", "역사", "문화", "전통", "건물", "활동", "체험"]
    else:
        return ["의미", "가치", "배경", "목적", "교육적", "문화유산", "보존", "계승"]


def get_difficult_words_for_grade(grade: int) -> List[str]:
    """학년별 어려운 어휘 (사용 금지)"""
    if grade <= 2:
        return ["건축양식", "문화재", "유산", "보존", "계승", "전승", "의의"]
    elif grade <= 4:
        return ["양식", "기법", "철학", "사상", "이념", "체계", "구조적"]
    else:
        return ["형이상학적", "인식론적", "존재론적"]  # 5-6학년도 이 정도는 어려움


# 사용 예시
def generate_enhanced_quiz(openai_client, spot_info: Dict, grade: int, quiz_number: int = 1):
    """품질 개선된 퀴즈 생성"""
    
    prompt = create_dynamic_prompt(spot_info, grade, quiz_number)
    
    try:
        response = openai_client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {
                    "role": "system", 
                    "content": f"당신은 초등학교 {grade}학년 현장학습 전문가입니다. 절대 정확한 형식으로만 답변하세요."
                },
                {"role": "user", "content": prompt}
            ],
            max_tokens=400,
            temperature=0.4,  # 적절한 창의성 + 일관성
            timeout=25
        )
        
        quiz_result = enhanced_quiz_parser(
            response.choices[0].message.content, 
            spot_info, 
            grade
        )
        
        return quiz_result
        
    except Exception as e:
        logger.error(f"Enhanced 퀴즈 생성 실패: {e}")
        return None