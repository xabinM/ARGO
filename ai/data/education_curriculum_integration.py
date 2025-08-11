# education_curriculum_integration.py - 교육과정 연계 데이터 통합
"""
🎓 초등 교육과정 연계 RAG 시스템

목표:
1. 초등 1-6학년 교과별 성취기준 연계
2. 스팟별 교육과정 맵핑
3. 학년별 맞춤형 퀴즈 생성 향상

소요시간: 2-3일
포트폴리오 임팩트: 매우 높음 (교육 도메인 전문성)
"""

import json
import re
from typing import Dict, List, Optional, Tuple
from dataclasses import dataclass
from enum import Enum

# === 교육과정 데이터 구조 ===

class Subject(Enum):
    """교과목 분류"""
    KOREAN = "국어"
    MATH = "수학"
    SOCIAL = "사회"
    SCIENCE = "과학"
    MORAL = "도덕"
    ART = "미술"
    MUSIC = "음악"
    PE = "체육"

@dataclass
class AchievementStandard:
    """성취기준 데이터 클래스"""
    code: str                    # 성취기준 코드 (예: [4사02-03])
    subject: Subject            # 교과목
    grade: int                  # 학년
    semester: int               # 학기 (1 or 2)
    title: str                  # 성취기준 제목
    description: str            # 상세 설명
    keywords: List[str]         # 핵심 키워드
    difficulty_level: int       # 난이도 (1-5)
    related_concepts: List[str] # 관련 개념

@dataclass
class SpotCurriculumMapping:
    """스팟-교육과정 연계 데이터"""
    spot_name: str
    location: str
    primary_standards: List[str]    # 주요 성취기준 코드들
    secondary_standards: List[str]  # 부차적 성취기준 코드들
    grade_suitability: Dict[int, float]  # 학년별 적합도 점수 (1.0 = 완벽)
    educational_objectives: List[str]     # 교육 목표
    
# === 초등 교육과정 성취기준 데이터베이스 ===

ELEMENTARY_ACHIEVEMENT_STANDARDS = {
    # 사회과 (역사/문화재 관련)
    "[3사02-01]": AchievementStandard(
        code="[3사02-01]",
        subject=Subject.SOCIAL,
        grade=3,
        semester=2,
        title="우리 고장의 문화유산",
        description="우리 고장의 전통문화를 살펴보고, 이를 보존하려는 노력을 알아본다.",
        keywords=["문화유산", "전통문화", "보존", "고장"],
        difficulty_level=2,
        related_concepts=["문화재", "역사", "조상", "전통"]
    ),
    
    "[4사02-03]": AchievementStandard(
        code="[4사02-03]",
        subject=Subject.SOCIAL,
        grade=4,
        semester=2,
        title="지역의 문화유산과 인물",
        description="지역의 문화유산이나 인물 중에서 자랑거리를 찾아보고, 이를 소중히 여기는 마음을 갖는다.",
        keywords=["지역문화유산", "인물", "자랑거리", "소중히"],
        difficulty_level=3,
        related_concepts=["문화재", "역사인물", "지역사", "자부심"]
    ),
    
    "[5사02-02]": AchievementStandard(
        code="[5사02-02]",
        subject=Subject.SOCIAL,
        grade=5,
        semester=2,
        title="조선 시대 정치 운영",
        description="조선 시대 정치 운영의 특징과 주요 정책들을 탐구한다.",
        keywords=["조선시대", "정치운영", "정책", "특징"],
        difficulty_level=4,
        related_concepts=["궁궐", "임금", "신하", "정치제도"]
    ),
    
    "[5사02-03]": AchievementStandard(
        code="[5사02-03]",
        subject=Subject.SOCIAL,
        grade=5,
        semester=2,
        title="조선 시대 사회 모습",
        description="조선 시대 사회의 모습과 생활 문화를 살펴본다.",
        keywords=["조선시대", "사회모습", "생활문화", "계층"],
        difficulty_level=4,
        related_concepts=["궁궐생활", "왕실문화", "전통건축", "사회제도"]
    ),
    
    "[6사02-01]": AchievementStandard(
        code="[6사02-01]",
        subject=Subject.SOCIAL,
        grade=6,
        semester=2,
        title="일제강점기와 광복",
        description="일제강점기 우리 민족의 삶과 광복을 위한 노력을 탐구한다.",
        keywords=["일제강점기", "민족", "광복", "독립운동"],
        difficulty_level=5,
        related_concepts=["저항", "독립운동가", "민족정신", "해방"]
    ),
    
    # 과학과 (자연/동물 관련)
    "[3과02-01]": AchievementStandard(
        code="[3과02-01]",
        subject=Subject.SCIENCE,
        grade=3,
        semester=2,
        title="동물의 한살이",
        description="동물의 한살이 과정을 관찰하고 기록할 수 있다.",
        keywords=["동물", "한살이", "관찰", "기록"],
        difficulty_level=2,
        related_concepts=["생명체", "성장", "변화", "생태"]
    ),
    
    "[4과02-01]": AchievementStandard(
        code="[4과02-01]",
        subject=Subject.SCIENCE,
        grade=4,
        semester=2,
        title="동물의 생김새와 생활 방식",
        description="동물의 생김새와 생활 방식이 서로 어떤 관련이 있는지 설명할 수 있다.",
        keywords=["동물", "생김새", "생활방식", "관련성"],
        difficulty_level=3,
        related_concepts=["적응", "환경", "특징", "서식지"]
    ),
    
    "[5과01-01]": AchievementStandard(
        code="[5과01-01]",
        subject=Subject.SCIENCE,
        grade=5,
        semester=1,
        title="생물과 환경의 관계",
        description="생물과 환경이 서로 영향을 주고받으며 살아가고 있음을 설명할 수 있다.",
        keywords=["생물", "환경", "상호작용", "영향"],
        difficulty_level=4,
        related_concepts=["생태계", "먹이사슬", "균형", "보전"]
    ),
    
    # 도덕과 (가치관/인성 관련)
    "[3도02-02]": AchievementStandard(
        code="[3도02-02]",
        subject=Subject.MORAL,
        grade=3,
        semester=2,
        title="조상과 전통문화 존중",
        description="조상과 전통문화를 존중하는 마음을 갖고 이를 계승 발전시키려는 의지를 기른다.",
        keywords=["조상", "전통문화", "존중", "계승"],
        difficulty_level=2,
        related_concepts=["효도", "전통", "문화유산", "자긍심"]
    ),
    
    "[4도02-03]": AchievementStandard(
        code="[4도02-03]",
        subject=Subject.MORAL,
        grade=4,
        semester=2,
        title="나라 사랑하는 마음",
        description="나라를 사랑하는 마음을 갖고 나라 발전에 이바지하려는 태도를 기른다.",
        keywords=["나라사랑", "애국심", "발전", "이바지"],
        difficulty_level=3,
        related_concepts=["국가", "시민의식", "책임", "자부심"]
    ),
    
    "[6도02-01]": AchievementStandard(
        code="[6도02-01]",
        subject=Subject.MORAL,
        grade=6,
        semester=2,
        title="평화와 통일",
        description="평화의 소중함을 알고 통일에 대한 관심과 의지를 기른다.",
        keywords=["평화", "통일", "소중함", "의지"],
        difficulty_level=4,
        related_concepts=["화해", "협력", "희망", "미래"]
    )
}

# === 스팟별 교육과정 연계 맵핑 ===

SPOT_CURRICULUM_MAPPINGS = {
    "경복궁": {
        "근정전": SpotCurriculumMapping(
            spot_name="근정전",
            location="경복궁",
            primary_standards=["[5사02-02]", "[5사02-03]"],  # 조선시대 정치/사회
            secondary_standards=["[4사02-03]", "[4도02-03]"], # 지역문화유산, 나라사랑
            grade_suitability={
                1: 0.2, 2: 0.3, 3: 0.6, 4: 0.8, 5: 1.0, 6: 0.9
            },
            educational_objectives=[
                "조선시대 정치 제도의 이해",
                "궁궐 건축의 의미와 역할 학습",
                "전통 문화에 대한 자부심 형성"
            ]
        ),
        
        "경회루": SpotCurriculumMapping(
            spot_name="경회루",
            location="경복궁",
            primary_standards=["[5사02-03]", "[3도02-02]"],  # 조선시대 사회, 전통문화
            secondary_standards=["[3사02-01]", "[4사02-03]"], # 문화유산, 지역문화
            grade_suitability={
                1: 0.4, 2: 0.5, 3: 0.7, 4: 0.8, 5: 0.9, 6: 0.8
            },
            educational_objectives=[
                "조선시대 궁중 문화의 이해",
                "전통 건축의 아름다움 감상",
                "문화유산 보존의 중요성 인식"
            ]
        )
    },
    
    "서울대공원": {
        "사슴사": SpotCurriculumMapping(
            spot_name="사슴사",
            location="서울대공원",
            primary_standards=["[4과02-01]", "[3과02-01]"],  # 동물의 생김새, 한살이
            secondary_standards=["[5과01-01]"],              # 생물과 환경
            grade_suitability={
                1: 0.8, 2: 0.9, 3: 1.0, 4: 1.0, 5: 0.7, 6: 0.5
            },
            educational_objectives=[
                "초식동물의 특징 관찰",
                "동물과 환경의 관계 이해",
                "생명체에 대한 존중과 보호 의식 함양"
            ]
        ),
        
        "낙타사": SpotCurriculumMapping(
            spot_name="낙타사",
            location="서울대공원",
            primary_standards=["[4과02-01]", "[5과01-01]"],  # 동물의 생김새, 생물과 환경
            secondary_standards=["[3과02-01]"],              # 동물의 한살이
            grade_suitability={
                1: 0.7, 2: 0.8, 3: 0.9, 4: 1.0, 5: 0.8, 6: 0.6
            },
            educational_objectives=[
                "사막 환경에 적응한 동물의 특징",
                "환경과 생물의 상호작용 이해",
                "다양한 생물의 생존 전략 학습"
            ]
        )
    },
    
    "서대문형무소역사관": {
        "중앙사": SpotCurriculumMapping(
            spot_name="중앙사",
            location="서대문형무소역사관",
            primary_standards=["[6사02-01]", "[6도02-01]"],  # 일제강점기, 평화와 통일
            secondary_standards=["[4도02-03]"],              # 나라사랑
            grade_suitability={
                1: 0.1, 2: 0.2, 3: 0.3, 4: 0.5, 5: 0.8, 6: 1.0
            },
            educational_objectives=[
                "일제강점기 역사의 이해",
                "독립운동가들의 희생정신 학습",
                "평화와 자유의 소중함 인식"
            ]
        )
    }
}

# === 교육과정 연계 퀴즈 생성 시스템 ===

class EducationCurriculumIntegrator:
    """교육과정 연계 통합 시스템"""
    
    def __init__(self):
        self.standards = ELEMENTARY_ACHIEVEMENT_STANDARDS
        self.mappings = SPOT_CURRICULUM_MAPPINGS
    
    def get_suitable_standards(self, location: str, spot_name: str, grade: int) -> List[AchievementStandard]:
        """특정 스팟과 학년에 적합한 성취기준 반환"""
        if location not in self.mappings:
            return []
        
        if spot_name not in self.mappings[location]:
            return []
        
        mapping = self.mappings[location][spot_name]
        
        # 학년 적합도 확인
        suitability = mapping.grade_suitability.get(grade, 0.0)
        if suitability < 0.5:  # 적합도가 50% 미만이면 제외
            return []
        
        # 주요 성취기준과 부차적 성취기준 결합
        all_standard_codes = mapping.primary_standards + mapping.secondary_standards
        
        suitable_standards = []
        for code in all_standard_codes:
            if code in self.standards:
                standard = self.standards[code]
                if standard.grade <= grade + 1 and standard.grade >= grade - 1:  # 학년 ±1 범위
                    suitable_standards.append(standard)
        
        return suitable_standards
    
    def generate_curriculum_enhanced_prompt(self, location: str, spot_name: str, grade: int, 
                                          basic_description: str) -> str:
        """교육과정이 연계된 향상된 프롬프트 생성"""
        
        suitable_standards = self.get_suitable_standards(location, spot_name, grade)
        
        if not suitable_standards:
            # 기본 프롬프트 반환
            return self._generate_basic_prompt(spot_name, basic_description, grade)
        
        # 교육과정 연계 정보 구성
        curriculum_info = self._build_curriculum_context(suitable_standards, location, spot_name)
        
        prompt = f"""초등학교 {grade}학년용 교육과정 연계 삼지선다 퀴즈를 만드세요.

장소: {location} - {spot_name}
기본 설명: {basic_description}

🎓 교육과정 연계 정보:
{curriculum_info}

퀴즈 생성 조건:
- {grade}학년 수준에 맞는 어휘와 개념 사용
- 위 성취기준과 연계된 교육적 내용 포함
- 현장학습의 목적에 부합하는 관찰/체험 중심 문제
- 학습자의 흥미를 유발하는 방식

출력 형식:
문제: [교육과정과 연계된 질문]
1) [선택지1] 2) [선택지2] 3) [선택지3]
정답: [번호]
해설: [교육과정 연계 설명]
교육목표: [이 문제를 통해 달성하고자 하는 학습 목표]"""

        return prompt
    
    def _build_curriculum_context(self, standards: List[AchievementStandard], 
                                location: str, spot_name: str) -> str:
        """교육과정 맥락 정보 구성"""
        context_parts = []
        
        # 성취기준 정보
        for standard in standards[:2]:  # 주요 2개만 사용
            context_parts.append(
                f"• {standard.code} ({standard.subject.value} {standard.grade}학년): {standard.title}\n"
                f"  → {standard.description}\n"
                f"  핵심키워드: {', '.join(standard.keywords)}"
            )
        
        # 교육 목표
        if location in self.mappings and spot_name in self.mappings[location]:
            mapping = self.mappings[location][spot_name]
            context_parts.append(f"\n📚 교육 목표:")
            for obj in mapping.educational_objectives:
                context_parts.append(f"• {obj}")
        
        return "\n".join(context_parts)
    
    def _generate_basic_prompt(self, spot_name: str, description: str, grade: int) -> str:
        """기본 프롬프트 생성 (교육과정 연계 없는 경우)"""
        return f"""초등학교 {grade}학년용 삼지선다 퀴즈를 만드세요.

장소: {spot_name}
설명: {description}

조건:
- {grade}학년 수준에 맞는 어휘 사용
- 현장 관찰 중심의 교육적 내용

형식:
문제: [질문]
1) [선택지1] 2) [선택지2] 3) [선택지3]
정답: [번호]
해설: [설명]"""
    
    def get_educational_metadata(self, location: str, spot_name: str, grade: int) -> Dict:
        """퀴즈와 함께 제공할 교육 메타데이터 생성"""
        suitable_standards = self.get_suitable_standards(location, spot_name, grade)
        
        metadata = {
            "curriculum_standards": [],
            "educational_objectives": [],
            "subject_areas": [],
            "grade_suitability": 0.0,
            "learning_keywords": []
        }
        
        if suitable_standards:
            metadata["curriculum_standards"] = [
                {
                    "code": std.code,
                    "subject": std.subject.value,
                    "title": std.title,
                    "grade": std.grade
                }
                for std in suitable_standards
            ]
            
            metadata["subject_areas"] = list(set(std.subject.value for std in suitable_standards))
            metadata["learning_keywords"] = list(set(
                keyword for std in suitable_standards for keyword in std.keywords
            ))
        
        if location in self.mappings and spot_name in self.mappings[location]:
            mapping = self.mappings[location][spot_name]
            metadata["educational_objectives"] = mapping.educational_objectives
            metadata["grade_suitability"] = mapping.grade_suitability.get(grade, 0.0)
        
        return metadata

# === FastAPI 연동을 위한 개선된 함수들 ===

def integrate_curriculum_to_existing_api():
    """기존 FastAPI에 교육과정 연계 기능 통합"""
    
    integrator = EducationCurriculumIntegrator()
    
    def enhanced_create_optimized_prompt(spot_info: Dict, grade: int) -> str:
        """교육과정 연계가 강화된 프롬프트 생성"""
        location = spot_info.get("메인장소", "")
        spot_name = spot_info.get("세부스팟", "")
        description = spot_info.get("설명", "")
        
        return integrator.generate_curriculum_enhanced_prompt(
            location, spot_name, grade, description
        )
    
    def enhanced_quiz_response_with_curriculum(quiz_data: Dict, spot_info: Dict, grade: int) -> Dict:
        """교육과정 정보가 포함된 퀴즈 응답 생성"""
        location = spot_info.get("메인장소", "")
        spot_name = spot_info.get("세부스팟", "")
        
        # 기본 퀴즈 데이터에 교육과정 메타데이터 추가
        educational_metadata = integrator.get_educational_metadata(location, spot_name, grade)
        
        quiz_data["educational_metadata"] = educational_metadata
        quiz_data["curriculum_aligned"] = len(educational_metadata["curriculum_standards"]) > 0
        
        return quiz_data
    
    return enhanced_create_optimized_prompt, enhanced_quiz_response_with_curriculum

# === 사용 예시 및 테스트 ===

def test_curriculum_integration():
    """교육과정 연계 시스템 테스트"""
    integrator = EducationCurriculumIntegrator()
    
    print("🎓 교육과정 연계 시스템 테스트")
    print("=" * 50)
    
    # 테스트 케이스들
    test_cases = [
        {"location": "경복궁", "spot_name": "근정전", "grade": 5},
        {"location": "서울대공원", "spot_name": "사슴사", "grade": 3},
        {"location": "서대문형무소역사관", "spot_name": "중앙사", "grade": 6}
    ]
    
    for case in test_cases:
        print(f"\n📍 테스트: {case['location']} - {case['spot_name']} ({case['grade']}학년)")
        
        # 적합한 성취기준 조회
        standards = integrator.get_suitable_standards(
            case['location'], case['spot_name'], case['grade']
        )
        
        print(f"   연계 성취기준: {len(standards)}개")
        for std in standards:
            print(f"   • {std.code}: {std.title}")
        
        # 교육 메타데이터 생성
        metadata = integrator.get_educational_metadata(
            case['location'], case['spot_name'], case['grade']
        )
        
        print(f"   학년 적합도: {metadata['grade_suitability']:.1f}")
        print(f"   교과 영역: {', '.join(metadata['subject_areas'])}")
        print(f"   핵심 키워드: {', '.join(metadata['learning_keywords'][:5])}")
        
        # 향상된 프롬프트 생성
        enhanced_prompt = integrator.generate_curriculum_enhanced_prompt(
            case['location'], case['spot_name'], case['grade'],
            "테스트용 기본 설명"
        )
        
        print(f"   프롬프트 길이: {len(enhanced_prompt)}자")
        print(f"   교육과정 연계: {'예' if '🎓 교육과정 연계 정보:' in enhanced_prompt else '아니오'}")

if __name__ == "__main__":
    # 시스템 테스트 실행
    test_curriculum_integration()
    
    # FastAPI 연동 함수 생성
    enhanced_prompt_func, enhanced_response_func = integrate_curriculum_to_existing_api()
    
    print("\n✅ 교육과정 연계 시스템 준비 완료!")
    print("🔧 다음 단계: enhanced_gms_production_fastapi.py에 통합")