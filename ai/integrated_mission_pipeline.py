# integrated_pipeline.py - 전체 통합 오케스트레이터
import logging
from typing import Dict, List, Optional
from dataclasses import dataclass
from datetime import datetime

# 각 모듈 import
from preprocessing.pipeline import EnhancedRAGPipeline as PreprocessingPipeline
from retrieval.rag_pipeline_ver0 import AdvancedRAGPipeline as RetrievalPipeline
from generation.mission_generators import EnhancedMissionGenerator
from time_management.time_allocators import SmartTimeAllocator

logger = logging.getLogger(__name__)

@dataclass
class MissionOutput:
    """미션 출력 데이터 클래스"""
    # 교사용 정보
    teacher_version: Dict
    # 학생용 정보 (깔끔한 버전)
    student_version: Dict
    # 메타데이터
    metadata: Dict

class IntegratedMissionPipeline:
    """전체 미션 생성 파이프라인 통합 오케스트레이터"""
    
    def __init__(self):
        # 각 모듈 초기화
        self.preprocessor = PreprocessingPipeline()
        self.retriever = RetrievalPipeline()
        self.generator = EnhancedMissionGenerator()
        self.time_allocator = SmartTimeAllocator()
        
        # 출력 포맷터
        self.output_formatter = MissionOutputFormatter()
        
    async def initialize(self, data_file: str):
        """파이프라인 초기화"""
        logger.info("🚀 통합 미션 파이프라인 초기화")
        
        # 1. 전처리 파이프라인 초기화
        await self.preprocessor.initialize_with_preprocessing(data_file)
        
        # 2. 검색 파이프라인 초기화
        if not self.retriever.load_data(data_file):
            raise Exception("데이터 로드 실패")
        
        if not self.retriever.prepare_documents():
            raise Exception("문서 준비 실패")
            
        await self.retriever.create_optimized_embeddings()
        
        logger.info("✅ 통합 파이프라인 초기화 완료")
    
    async def generate_complete_mission(self, request: Dict) -> MissionOutput:
        """완전한 미션 생성 (모듈별 역할 분담)"""
        
        mission_id = f"mission_{int(datetime.now().timestamp())}"
        logger.info(f"🎯 통합 미션 생성 시작: {mission_id}")
        
        try:
            # 1. 관련 문서 검색 (retrieval 모듈)
            search_results = await self.retriever.smart_search(
                request.get('location', ''), k=5
            )
            
            # 2. 시간 배분 계산 (time_management 모듈)
            time_allocation = self.time_allocator.allocate_time_intelligently(
                mission_types=[request.get('mission_type', '퀴즈')],
                total_minutes=request.get('duration_minutes', 30),
                grade=request.get('grade', 5),
                group_size=request.get('group_size', 4),
                difficulty=request.get('difficulty', 'medium')
            )
            
            # 3. 미션 생성 (generation 모듈)
            mission_request = self._create_mission_request(request, time_allocation[0])
            heritage_info = [r['metadata'] for r in search_results]
            
            missions = await self.generator.generate_multiple_missions(
                mission_request, heritage_info
            )
            
            # 4. 후처리 (preprocessing 모듈)
            processed_missions = []
            for mission in missions:
                processed = await self.preprocessor.postprocessor.postprocess_mission(
                    content=mission['content'],
                    mission_type=mission['mission_type'],
                    grade=request.get('grade', 5),
                    quality_score=mission.get('suitability_score', 0.8)
                )
                processed_missions.append(processed)
            
            # 5. 출력 포맷팅 (교사용/학생용 분리)
            output = self.output_formatter.format_mission_output(
                missions=processed_missions,
                time_allocation=time_allocation,
                search_results=search_results,
                request=request,
                mission_id=mission_id
            )
            
            logger.info(f"✅ 통합 미션 생성 완료: {mission_id}")
            return output
            
        except Exception as e:
            logger.error(f"통합 미션 생성 실패: {e}")
            return self._create_error_output(str(e), mission_id)
    
    def _create_mission_request(self, request: Dict, time_allocation) -> object:
        """미션 요청 객체 생성"""
        from generation.mission_generators import MultiMissionRequest
        
        return MultiMissionRequest(
            location=request.get('location', ''),
            user_grade=request.get('grade', 5),
            group_size=request.get('group_size', 4),
            duration_minutes=time_allocation.allocated_minutes,
            max_missions=request.get('max_missions', 1),
            difficulty=request.get('difficulty', 'medium'),
            context=request.get('context', '')
        )
    
    def _create_error_output(self, error_msg: str, mission_id: str) -> MissionOutput:
        """오류 시 기본 출력 생성"""
        return MissionOutput(
            teacher_version={"error": error_msg, "mission_id": mission_id},
            student_version={"message": "미션을 불러오는 중입니다. 잠시만 기다려주세요."},
            metadata={"error": error_msg, "mission_id": mission_id}
        )


class MissionOutputFormatter:
    """미션 출력 포맷터 - 교사용/학생용 분리"""
    
    def format_mission_output(self, missions: List, time_allocation: List, 
                            search_results: List, request: Dict, mission_id: str) -> MissionOutput:
        """미션 출력을 교사용/학생용으로 분리 포맷팅"""
        
        # 교사용 상세 정보
        teacher_version = self._create_teacher_version(
            missions, time_allocation, search_results, request, mission_id
        )
        
        # 학생용 깔끔한 버전
        student_version = self._create_student_version(missions, request)
        
        # 메타데이터
        metadata = self._create_metadata(missions, time_allocation, mission_id)
        
        return MissionOutput(
            teacher_version=teacher_version,
            student_version=student_version,
            metadata=metadata
        )
    
    def _create_teacher_version(self, missions: List, time_allocation: List, 
                              search_results: List, request: Dict, mission_id: str) -> Dict:
        """교사용 상세 버전"""
        return {
            "mission_id": mission_id,
            "overview": {
                "location": request.get('location', ''),
                "grade": request.get('grade', 5),
                "group_size": request.get('group_size', 4),
                "total_duration": request.get('duration_minutes', 30),
                "difficulty": request.get('difficulty', 'medium')
            },
            "missions": [
                {
                    "id": mission['metadata'].get('mission_id', f"mission_{i}"),
                    "type": mission['metadata'].get('mission_type', '알 수 없음'),
                    "content": mission['processed_content'],
                    "duration_minutes": mission['metadata'].get('duration_minutes', 0),
                    "quality_score": mission['quality_score'],
                    "teaching_guide": self._generate_teaching_guide(mission),
                    "safety_notes": self._extract_safety_notes(mission['processed_content']),
                    "learning_objectives": self._extract_learning_objectives(mission['processed_content'])
                }
                for i, mission in enumerate(missions)
            ],
            "time_management": {
                "allocations": [
                    {
                        "mission_type": alloc.mission_type,
                        "allocated_time": alloc.allocated_minutes,
                        "recommended_time": alloc.recommended_minutes,
                        "efficiency": f"{alloc.efficiency_score:.1%}"
                    }
                    for alloc in time_allocation
                ],
                "total_time": sum(alloc.allocated_minutes for alloc in time_allocation),
                "efficiency_report": self._generate_efficiency_report(time_allocation)
            },
            "source_information": {
                "heritage_sites": [
                    {
                        "name": result['metadata'].get('name', ''),
                        "location": result['metadata'].get('location', ''),
                        "relevance_score": f"{result.get('relevance_score', 0):.2f}"
                    }
                    for result in search_results[:3]
                ]
            },
            "preparation_checklist": self._generate_preparation_checklist(missions, request),
            "assessment_rubric": self._generate_assessment_rubric(missions)
        }
    
    def _create_student_version(self, missions: List, request: Dict) -> Dict:
        """학생용 깔끔한 버전"""
        student_missions = []
        
        for i, mission in enumerate(missions):
            # 교사용 정보 제거하고 학생용만 추출
            clean_content = self._extract_student_content(mission['processed_content'])
            
            student_missions.append({
                "mission_number": i + 1,
                "content": clean_content,
                "type": self._get_simple_mission_type(mission['metadata'].get('mission_type', '')),
                "estimated_time": f"{mission['metadata'].get('duration_minutes', 0)}분"
            })
        
        return {
            "location": request.get('location', ''),
            "welcome_message": f"안녕하세요! {request.get('location', '')}에서 재미있는 미션을 수행해보세요! 🎯",
            "missions": student_missions,
            "total_missions": len(student_missions),
            "good_luck_message": "모든 미션을 완료하면 멋진 역사 탐험가가 될 수 있어요! 화이팅! 🌟"
        }
    
    def _extract_student_content(self, full_content: str) -> str:
        """교사용 내용에서 학생용만 추출"""
        lines = full_content.split('\n')
        student_lines = []
        
        skip_patterns = ['⚠️', '안전 수칙', '💡 권장사항', '교사 가이드', '준비물']
        
        for line in lines:
            # 교사용 정보 제외
            if any(pattern in line for pattern in skip_patterns):
                break
            
            # 학생에게 필요한 내용만 포함
            if any(emoji in line for emoji in ['🎯', '📝', '👀', '🎭', '📸', '🔍']):
                student_lines.append(line)
            elif line.strip() and not line.startswith('⚠️') and not line.startswith('💡'):
                student_lines.append(line)
        
        return '\n'.join(student_lines).strip()
    
    def _get_simple_mission_type(self, mission_type: str) -> str:
        """미션 타입을 학생 친화적으로 변환"""
        type_map = {
            "퀴즈": "🎯 문제 풀기",
            "관찰미션": "👀 관찰하기", 
            "체험미션": "🎭 체험하기",
            "사진미션": "📸 사진 찍기",
            "탐색미션": "🔍 찾기 게임"
        }
        return type_map.get(mission_type, "🎯 미션")
    
    def _generate_teaching_guide(self, mission: Dict) -> Dict:
        """교사 가이드 생성"""
        return {
            "preparation": "미션 시작 전 안전 수칙을 설명해주세요.",
            "facilitation": "학생들이 협력할 수 있도록 도와주세요.",
            "assessment": "활동 과정과 결과를 모두 평가해주세요.",
            "extension": "시간이 남으면 추가 질문을 통해 심화 학습을 유도하세요."
        }
    
    def _extract_safety_notes(self, content: str) -> List[str]:
        """안전 수칙 추출"""
        safety_notes = []
        lines = content.split('\n')
        
        in_safety_section = False
        for line in lines:
            if '안전 수칙' in line or '⚠️' in line:
                in_safety_section = True
                continue
            
            if in_safety_section:
                if line.strip() and line.startswith('-'):
                    safety_notes.append(line.strip('- ').strip())
                elif line.strip() and not line.startswith(' '):
                    break
        
        return safety_notes or ["선생님과 함께 활동하세요", "문화재에 손대지 마세요"]
    
    def _extract_learning_objectives(self, content: str) -> List[str]:
        """학습 목표 추출"""
        objectives = []
        if "학습" in content or "배울" in content:
            objectives.append("역사와 문화에 대한 이해 증진")
            objectives.append("협력과 소통 능력 향상")
            objectives.append("관찰력과 사고력 발달")
        
        return objectives
    
    def _generate_efficiency_report(self, time_allocation: List) -> str:
        """효율성 리포트 생성"""
        avg_efficiency = sum(alloc.efficiency_score for alloc in time_allocation) / len(time_allocation)
        
        if avg_efficiency >= 0.9:
            return "✅ 시간 배분이 매우 효율적입니다."
        elif avg_efficiency >= 0.7:
            return "⚠️ 시간이 다소 부족할 수 있습니다."
        else:
            return "❌ 시간이 부족합니다. 미션 수를 줄이거나 시간을 늘려주세요."
    
    def _generate_preparation_checklist(self, missions: List, request: Dict) -> List[str]:
        """준비물 체크리스트 생성"""
        checklist = [
            "학생 명단 및 그룹 편성",
            "응급처치 용품",
            "활동 기록지"
        ]
        
        # 미션 타입별 추가 준비물
        for mission in missions:
            mission_type = mission['metadata'].get('mission_type', '')
            if mission_type == "사진미션":
                checklist.append("카메라 또는 스마트폰")
            elif mission_type == "체험미션":
                checklist.append("역할놀이 소품")
            elif mission_type == "관찰미션":
                checklist.append("관찰 기록지 및 연필")
        
        return list(set(checklist))  # 중복 제거
    
    def _generate_assessment_rubric(self, missions: List) -> Dict:
        """평가 루브릭 생성"""
        return {
            "participation": {
                "excellent": "적극적으로 참여하고 그룹 활동을 이끔",
                "good": "성실하게 참여하고 협력함", 
                "needs_improvement": "소극적이지만 활동에 참여함"
            },
            "understanding": {
                "excellent": "문화재의 의미를 깊이 이해하고 설명할 수 있음",
                "good": "기본적인 내용을 이해하고 답할 수 있음",
                "needs_improvement": "부분적으로 이해하고 있음"
            },
            "collaboration": {
                "excellent": "그룹원들과 효과적으로 소통하고 협력함",
                "good": "그룹 활동에 잘 참여함",
                "needs_improvement": "협력에 어려움을 보임"
            }
        }
    
    def _create_metadata(self, missions: List, time_allocation: List, mission_id: str) -> Dict:
        """메타데이터 생성"""
        return {
            "mission_id": mission_id,
            "generation_timestamp": datetime.now().isoformat(),
            "total_missions": len(missions),
            "average_quality_score": sum(m['quality_score'] for m in missions) / len(missions),
            "total_estimated_time": sum(alloc.allocated_minutes for alloc in time_allocation),
            "pipeline_version": "1.0.0",
            "modules_used": [
                "preprocessing.pipeline",
                "retrieval.rag_pipeline_ver0", 
                "generation.mission_generators",
                "time_management.time_allocators"
            ]
        }


# 사용 예시
async def main():
    """통합 파이프라인 테스트"""
    
    # 파이프라인 초기화
    pipeline = IntegratedMissionPipeline()
    await pipeline.initialize("heritage_complete_database.json")
    
    # 미션 생성 요청
    request = {
        "location": "경복궁",
        "grade": 5,
        "group_size": 4,
        "duration_minutes": 45,
        "max_missions": 2,
        "difficulty": "medium",
        "context": "첫 현장학습"
    }
    
    # 통합 미션 생성
    result = await pipeline.generate_complete_mission(request)
    
    # 결과 출력
    print("📋 교사용 미션 정보:")
    print(f"미션 ID: {result.teacher_version['mission_id']}")
    print(f"총 미션 수: {len(result.teacher_version['missions'])}")
    
    print("\n🎯 학생용 미션:")
    for mission in result.student_version['missions']:
        print(f"{mission['mission_number']}. {mission['type']}")
        print(f"   {mission['content'][:100]}...")
        print(f"   예상 시간: {mission['estimated_time']}")

if __name__ == "__main__":
    import asyncio
    asyncio.run(main())