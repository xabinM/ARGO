from typing import List, Dict, Tuple
from dataclasses import dataclass
import logging

logger = logging.getLogger(__name__)

@dataclass
class TimeAllocation:
    """시간 배분 결과"""
    mission_type: str
    allocated_minutes: int
    recommended_minutes: int  # 권장 시간
    efficiency_score: float   # 시간 효율성 (0-1)

class SmartTimeAllocator:
    """지능형 시간 배분 시스템"""
    
    def __init__(self):
        # 미션 타입별 기본 소요 시간 (분)
        self.base_durations = {
            "퀴즈": {
                "min": 5,      # 최소 시간
                "optimal": 12, # 최적 시간  
                "max": 20      # 최대 시간
            },
            "관찰미션": {
                "min": 10,
                "optimal": 18,
                "max": 30
            },
            "체험미션": {
                "min": 15,
                "optimal": 25,
                "max": 40
            },
            "사진미션": {
                "min": 8,
                "optimal": 15,
                "max": 25
            },
            "탐색미션": {
                "min": 12,
                "optimal": 20,
                "max": 35
            }
        }
        
        # 학년별 시간 조정 계수
        self.grade_factors = {
            3: 0.8,  # 3학년은 집중시간이 짧음
            4: 0.9,
            5: 1.0,  # 기준
            6: 1.1   # 6학년은 더 깊이 있게
        }
        
        # 그룹 크기별 시간 조정
        self.group_factors = {
            2: 0.9,   # 소규모는 빠름
            3: 0.95,
            4: 1.0,   # 기준
            5: 1.1,   # 큰 그룹은 시간 더 필요
            6: 1.2
        }
    
    def calculate_recommended_time(self, 
                                 mission_type: str, 
                                 grade: int, 
                                 group_size: int,
                                 difficulty: str = "medium") -> int:
        """미션별 권장 시간 계산"""
        
        # 기본 최적 시간
        base_time = self.base_durations[mission_type]["optimal"]
        
        # 학년 조정
        grade_factor = self.grade_factors.get(grade, 1.0)
        
        # 그룹 크기 조정
        group_factor = self.group_factors.get(min(6, group_size), 1.0)
        
        # 난이도 조정
        difficulty_factor = {
            "easy": 0.8,
            "medium": 1.0,
            "hard": 1.3
        }.get(difficulty, 1.0)
        
        # 최종 권장 시간 계산
        recommended = int(base_time * grade_factor * group_factor * difficulty_factor)
        
        # 최소/최대 범위 내로 제한
        min_time = self.base_durations[mission_type]["min"]
        max_time = self.base_durations[mission_type]["max"]
        
        return max(min_time, min(max_time, recommended))
    
    def allocate_time_intelligently(self, 
                                   mission_types: List[str],
                                   total_minutes: int,
                                   grade: int,
                                   group_size: int,
                                   difficulty: str = "medium") -> List[TimeAllocation]:
        """지능형 시간 배분"""
        
        logger.info(f"지능형 시간 배분 시작: {total_minutes}분, {len(mission_types)}개 미션")
        
        allocations = []
        
        # 1. 각 미션의 권장 시간 계산
        recommended_times = {}
        total_recommended = 0
        
        for mission_type in mission_types:
            recommended = self.calculate_recommended_time(
                mission_type, grade, group_size, difficulty
            )
            recommended_times[mission_type] = recommended
            total_recommended += recommended
        
        logger.info(f"총 권장 시간: {total_recommended}분 (가용: {total_minutes}분)")
        
        # 2. 시간 배분 전략 결정
        if total_recommended <= total_minutes:
            # 충분한 시간이 있는 경우
            allocations = self._allocate_with_surplus(
                mission_types, recommended_times, total_minutes
            )
        else:
            # 시간이 부족한 경우
            allocations = self._allocate_with_deficit(
                mission_types, recommended_times, total_minutes, grade, group_size
            )
        
        return allocations
    
    def _allocate_with_surplus(self, 
                              mission_types: List[str],
                              recommended_times: Dict[str, int],
                              total_minutes: int) -> List[TimeAllocation]:
        """여유 시간이 있을 때의 배분"""
        
        allocations = []
        surplus_time = total_minutes - sum(recommended_times.values())
        
        logger.info(f"여유 시간: {surplus_time}분")
        
        # 권장 시간을 기본으로 하고, 여유 시간을 우선순위에 따라 배분
        priority_order = ["체험미션", "관찰미션", "탐색미션", "퀴즈", "사진미션"]
        
        for mission_type in mission_types:
            base_time = recommended_times[mission_type]
            
            # 우선순위가 높은 미션에 여유 시간 추가 배분
            priority_index = priority_order.index(mission_type) if mission_type in priority_order else 999
            bonus_time = 0
            
            if surplus_time > 0 and priority_index < 3:  # 상위 3개 타입에만
                bonus_time = min(5, surplus_time // len(mission_types))
                surplus_time -= bonus_time
            
            allocated_time = base_time + bonus_time
            efficiency_score = min(1.0, allocated_time / recommended_times[mission_type])
            
            allocations.append(TimeAllocation(
                mission_type=mission_type,
                allocated_minutes=allocated_time,
                recommended_minutes=recommended_times[mission_type],
                efficiency_score=efficiency_score
            ))
        
        return allocations
    
    def _allocate_with_deficit(self, 
                              mission_types: List[str],
                              recommended_times: Dict[str, int],
                              total_minutes: int,
                              grade: int,
                              group_size: int) -> List[TimeAllocation]:
        """시간이 부족할 때의 배분"""
        
        logger.warning(f"시간 부족: 권장 {sum(recommended_times.values())}분 > 가용 {total_minutes}분")
        
        allocations = []
        
        # 미션별 최소 시간 확보
        min_times = {}
        total_min_time = 0
        
        for mission_type in mission_types:
            min_time = self.base_durations[mission_type]["min"]
            min_times[mission_type] = min_time
            total_min_time += min_time
        
        if total_min_time > total_minutes:
            # 최소 시간도 부족한 극단적 상황
            logger.error("최소 시간도 부족 - 미션 수를 줄여야 함")
            return self._emergency_allocation(mission_types, total_minutes)
        
        # 비례 축소 방식으로 배분
        remaining_time = total_minutes - total_min_time
        total_extra_need = sum(recommended_times.values()) - total_min_time
        
        for mission_type in mission_types:
            min_time = min_times[mission_type]
            extra_need = recommended_times[mission_type] - min_time
            
            if total_extra_need > 0:
                extra_allocation = int(remaining_time * (extra_need / total_extra_need))
            else:
                extra_allocation = 0
            
            allocated_time = min_time + extra_allocation
            efficiency_score = allocated_time / recommended_times[mission_type]
            
            allocations.append(TimeAllocation(
                mission_type=mission_type,
                allocated_minutes=allocated_time,
                recommended_minutes=recommended_times[mission_type],
                efficiency_score=efficiency_score
            ))
        
        return allocations
    
    def _emergency_allocation(self, mission_types: List[str], total_minutes: int) -> List[TimeAllocation]:
        """긴급 상황 - 균등 분할"""
        logger.warning("긴급 상황: 균등 분할 적용")
        
        allocations = []
        time_per_mission = total_minutes // len(mission_types)
        extra_time = total_minutes % len(mission_types)
        
        for i, mission_type in enumerate(mission_types):
            allocated_time = time_per_mission + (1 if i < extra_time else 0)
            recommended = self.base_durations[mission_type]["optimal"]
            
            allocations.append(TimeAllocation(
                mission_type=mission_type,
                allocated_minutes=allocated_time,
                recommended_minutes=recommended,
                efficiency_score=allocated_time / recommended
            ))
        
        return allocations
    
    def generate_time_report(self, allocations: List[TimeAllocation]) -> str:
        """시간 배분 리포트 생성"""
        
        total_allocated = sum(a.allocated_minutes for a in allocations)
        avg_efficiency = sum(a.efficiency_score for a in allocations) / len(allocations)
        
        report = f"""
📊 시간 배분 리포트
==================
총 할당 시간: {total_allocated}분
평균 효율성: {avg_efficiency:.1%}

미션별 상세:
"""
        
        for allocation in allocations:
            status_icon = "✅" if allocation.efficiency_score >= 0.8 else "⚠️" if allocation.efficiency_score >= 0.6 else "❌"
            
            report += f"""
{status_icon} {allocation.mission_type}
   할당: {allocation.allocated_minutes}분 | 권장: {allocation.recommended_minutes}분
   효율성: {allocation.efficiency_score:.1%}
"""
        
        # 권장사항 추가
        if avg_efficiency < 0.7:
            report += "\n💡 권장사항: 시간이 부족합니다. 미션 수를 줄이거나 전체 시간을 늘려주세요."
        elif avg_efficiency > 0.95:
            report += "\n💡 권장사항: 시간 여유가 있습니다. 미션을 더 추가하거나 심화 활동을 고려해보세요."
        
        return report

# 통합된 미션 생성 요청 클래스
@dataclass 
class SmartMissionRequest:
    """지능형 시간 배분이 포함된 미션 요청"""
    location: str
    user_grade: int = 5
    group_size: int = 4
    total_duration_minutes: int = 30  # 교사 입력 - 전체 체험 시간
    max_missions: int = 3
    preferred_types: List[str] = None
    difficulty: str = "medium"
    context: str = None
    
    # 시간 배분 관련 설정
    allow_time_adjustment: bool = True    # 시간 자동 조정 허용
    strict_time_limit: bool = False       # 엄격한 시간 제한
    min_mission_time: int = 5            # 미션당 최소 시간

# 사용 예시
def test_smart_time_allocation():
    """지능형 시간 배분 테스트"""
    
    allocator = SmartTimeAllocator()
    
    # 테스트 시나리오 1: 충분한 시간
    print("=== 시나리오 1: 충분한 시간 (60분, 3개 미션) ===")
    allocations1 = allocator.allocate_time_intelligently(
        mission_types=["체험미션", "퀴즈", "관찰미션"],
        total_minutes=60,
        grade=5,
        group_size=4
    )
    print(allocator.generate_time_report(allocations1))
    
    # 테스트 시나리오 2: 시간 부족
    print("\n=== 시나리오 2: 시간 부족 (30분, 3개 미션) ===")
    allocations2 = allocator.allocate_time_intelligently(
        mission_types=["체험미션", "퀴즈", "관찰미션"],
        total_minutes=30,
        grade=5,
        group_size=4
    )
    print(allocator.generate_time_report(allocations2))
    
    # 테스트 시나리오 3: 3학년, 소규모 그룹
    print("\n=== 시나리오 3: 3학년, 소규모 (45분, 2개 미션) ===")
    allocations3 = allocator.allocate_time_intelligently(
        mission_types=["사진미션", "체험미션"],
        total_minutes=45,
        grade=3,
        group_size=2,
        difficulty="easy"
    )
    print(allocator.generate_time_report(allocations3))

if __name__ == "__main__":
    test_smart_time_allocation()