#!/usr/bin/env python3
"""
현장학습 미션 생성 AI 시스템 - 메인 실행 파일
"""

import asyncio
import logging
import sys
from pathlib import Path

# 프로젝트 루트를 Python 경로에 추가
sys.path.append(str(Path(__file__).parent))

from config.settings import settings
from rag_pipeline.rag_pipeline_ver0 import AdvancedRAGPipeline, MissionRequest, AutomatedTestSuite

# 로깅 설정
def setup_logging():
    """로깅 설정"""
    log_dir = Path(settings.logging.log_file).parent
    log_dir.mkdir(parents=True, exist_ok=True)
    
    logging.basicConfig(
        level=getattr(logging, settings.logging.log_level),
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler(settings.logging.log_file, encoding='utf-8'),
            logging.StreamHandler(sys.stdout)
        ]
    )

async def initialize_pipeline() -> AdvancedRAGPipeline:
    """파이프라인 초기화"""
    logger = logging.getLogger(__name__)
    logger.info("🚀 고도화된 RAG 파이프라인 초기화 시작")
    
    # API 키 유효성 검사
    if not settings.validate_api_keys():
        logger.error("❌ API 키가 설정되지 않았습니다. .env 파일을 확인해주세요.")
        sys.exit(1)
    
    # 파이프라인 초기화
    pipeline = AdvancedRAGPipeline()
    
    # 데이터 로드
    if not pipeline.load_data(settings.database.database_path):
        logger.error("❌ 데이터 로드 실패")
        return None
    
    # 문서 준비
    if not pipeline.prepare_documents():
        logger.error("❌ 문서 준비 실패")
        return None
    
    # 임베딩 생성
    if not await pipeline.create_optimized_embeddings():
        logger.error("❌ 임베딩 생성 실패")
        return None
    
    logger.info("✅ 파이프라인 초기화 완료")
    return pipeline

async def run_sample_missions(pipeline: AdvancedRAGPipeline):
    """샘플 미션 생성 테스트"""
    logger = logging.getLogger(__name__)
    logger.info("\n🎯 샘플 미션 생성 테스트")
    
    # 다양한 시나리오 테스트
    test_scenarios = [
        MissionRequest(
            location="경복궁",
            user_grade=5,
            group_size=4,
            duration_minutes=45,
            difficulty="medium",
            preferred_type="퀴즈",
            context="첫 현장학습이라 안전에 특히 주의"
        ),
        MissionRequest(
            location="서울대공원",
            user_grade=4,
            group_size=6,
            duration_minutes=30,
            difficulty="easy",
            preferred_type="관찰미션",
            context="동물 관찰에 흥미가 많은 학생들"
        ),
        MissionRequest(
            location="국립중앙박물관",
            user_grade=6,
            group_size=4,
            duration_minutes=60,
            difficulty="hard",
            preferred_type="체험미션",
            context="역사에 관심이 많은 우수 학생들"
        )
    ]
    
    for i, scenario in enumerate(test_scenarios, 1):
        logger.info(f"\n--- 테스트 {i}: {scenario.location} ({scenario.preferred_type}) ---")
        
        try:
            result = await pipeline.generate_smart_mission(scenario)
            
            logger.info(f"✅ 미션 ID: {result.mission_id}")
            logger.info(f"📝 미션 타입: {result.mission_type}")
            logger.info(f"⭐ 품질 점수: {result.quality_score:.2f}")
            logger.info(f"⏱️ 생성 시간: {result.generation_time:.2f}초")
            logger.info(f"📚 참조 문화재: {', '.join(result.source_heritage)}")
            logger.info(f"📖 미션 내용:\n{result.content}")
            
        except Exception as e:
            logger.error(f"❌ 미션 생성 실패: {e}")

async def run_comprehensive_test(pipeline: AdvancedRAGPipeline):
    """종합 테스트 실행"""
    logger = logging.getLogger(__name__)
    logger.info("\n🧪 종합 테스트 실행")
    
    test_suite = AutomatedTestSuite(pipeline)
    test_results = await test_suite.run_comprehensive_test()
    
    # 결과 출력
    logger.info("📊 테스트 결과:")
    for key, value in test_results.items():
        if isinstance(value, dict):
            logger.info(f"   {key}:")
            for sub_key, sub_value in value.items():
                logger.info(f"     {sub_key}: {sub_value}")
        else:
            logger.info(f"   {key}: {value}")

def show_performance_report(pipeline: AdvancedRAGPipeline):
    """성능 리포트 출력"""
    logger = logging.getLogger(__name__)
    logger.info("\n📈 성능 리포트")
    
    report = pipeline.get_performance_report()
    for key, value in report.items():
        logger.info(f"   {key}: {value}")

async def interactive_mode(pipeline: AdvancedRAGPipeline):
    """대화형 모드"""
    logger = logging.getLogger(__name__)
    logger.info("\n🎮 대화형 모드 시작 (종료하려면 'quit' 입력)")
    
    while True:
        try:
            print("\n" + "="*50)
            location = input("📍 장소를 입력하세요: ").strip()
            if location.lower() == 'quit':
                break
            
            grade = input("👦 학년을 입력하세요 (1-6): ").strip()
            if not grade.isdigit() or not (1 <= int(grade) <= 6):
                print("❌ 학년은 1-6 사이의 숫자여야 합니다.")
                continue
            
            mission_type = input("🎯 미션 타입 (퀴즈/관찰미션/체험미션/자동): ").strip()
            if mission_type == "자동":
                mission_type = None
            
            difficulty = input("🔥 난이도 (easy/medium/hard): ").strip() or "medium"
            if difficulty not in ['easy', 'medium', 'hard']:
                difficulty = "medium"
            
            duration = input("⏰ 소요시간(분, 기본 30분): ").strip() or "30"
            if not duration.isdigit():
                duration = 30
            else:
                duration = int(duration)
            
            context = input("💭 추가 요청사항 (선택): ").strip() or None
            
            # 미션 요청 생성
            request = MissionRequest(
                location=location,
                user_grade=int(grade),
                duration_minutes=duration,
                difficulty=difficulty,
                preferred_type=mission_type,
                context=context
            )
            
            print("\n⚡ 미션 생성 중...")
            result = await pipeline.generate_smart_mission(request)
            
            print(f"\n✅ 미션이 생성되었습니다!")
            print(f"🆔 미션 ID: {result.mission_id}")
            print(f"📝 타입: {result.mission_type}")
            print(f"⭐ 품질: {result.quality_score:.2f}/1.0")
            print(f"⏱️ 생성시간: {result.generation_time:.2f}초")
            print("\n📖 미션 내용:")
            print("-" * 40)
            print(result.content)
            print("-" * 40)
            
        except KeyboardInterrupt:
            break
        except Exception as e:
            logger.error(f"❌ 오류 발생: {e}")
            print(f"❌ 오류가 발생했습니다: {e}")
    
    logger.info("👋 대화형 모드 종료")

async def main():
    """메인 함수"""
    setup_logging()
    logger = logging.getLogger(__name__)
    
    try:
        # 파이프라인 초기화
        pipeline = await initialize_pipeline()
        if pipeline is None:
            logger.error("❌ 파이프라인 초기화 실패")
            sys.exit(1)
        
        # 실행 모드 선택
        if len(sys.argv) > 1:
            mode = sys.argv[1].lower()
        else:
            print("\n🤖 현장학습 미션 생성 AI 시스템")
            print("실행 모드를 선택하세요:")
            print("1. 샘플 테스트 (sample)")
            print("2. 종합 테스트 (test)")
            print("3. 대화형 모드 (interactive)")
            print("4. 성능 리포트만 (report)")
            
            mode = input("\n모드를 입력하세요 (기본: sample): ").strip().lower() or "sample"
        
        if mode == "sample":
            await run_sample_missions(pipeline)
            show_performance_report(pipeline)
            
        elif mode == "test":
            await run_comprehensive_test(pipeline)
            show_performance_report(pipeline)
            
        elif mode == "interactive":
            await interactive_mode(pipeline)
            show_performance_report(pipeline)
            
        elif mode == "report":
            show_performance_report(pipeline)
            
        else:
            logger.error(f"❌ 알 수 없는 모드: {mode}")
            sys.exit(1)
        
        logger.info("🎉 프로그램 정상 종료")
        
    except Exception as e:
        logger.error(f"❌ 치명적 오류: {e}")
        sys.exit(1)

if __name__ == "__main__":
    # 이벤트 루프 실행
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n👋 사용자에 의해 중단되었습니다.")
    except Exception as e:
        print(f"❌ 예상치 못한 오류: {e}")
        sys.exit(1)