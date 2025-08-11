# services/pose_service.py - 포즈 분석 서비스
"""
🎯 포즈 분석 서비스 (래퍼)

주요 기능:
- 기존 포즈 분석 모듈 래핑
- 에러 처리 및 안정성 강화
- 성능 모니터링
"""

import logging
import numpy as np
import cv2
from typing import Dict, Optional
from fastapi import UploadFile

logger = logging.getLogger(__name__)

class PoseService:
    """포즈 분석 서비스"""
    
    def __init__(self, settings):
        self.settings = settings
        self.ai_model = None
        self.is_ready = False
        self.is_model_loaded = False
        
        # 성능 통계
        self.stats = {
            "total_analyzed": 0,
            "successful_detections": 0,
            "failed_detections": 0,
            "average_processing_time": 0.0
        }
    
    async def initialize(self):
        """서비스 초기화"""
        logger.info("📸 포즈 분석 서비스 초기화 중...")
        
        try:
            # 포즈 분석 모듈 import 확인
            from service.AI_ObjectDetector import AI_ObjectDetector
            from service.AI_Analyze import AI_Analyze
            
            # YOLO 모델 경로 확인
            model_path = self.settings.YOLO_MODEL_PATH
            if not model_path.startswith('/'):  # 상대 경로인 경우
                import os
                model_path = os.path.join(os.getcwd(), model_path)
            
            if not os.path.exists(model_path):
                logger.warning(f"⚠️ YOLO 모델 파일 없음: {model_path}")
                self.is_ready = False
                return
            
            # AI 모델 초기화
            self.ai_model = AI_ObjectDetector(model_path)
            self.AI_Analyze = AI_Analyze  # 클래스 참조 저장
            
            self.is_ready = True
            self.is_model_loaded = True
            logger.info("✅ 포즈 분석 서비스 초기화 완료")
            
        except ImportError as e:
            logger.warning(f"⚠️ 포즈 분석 모듈 import 실패: {e}")
            self.is_ready = False
        except Exception as e:
            logger.error(f"❌ 포즈 분석 서비스 초기화 실패: {e}")
            self.is_ready = False
    
    async def analyze_pose(self, file: UploadFile, pose_select: str) -> Dict:
        """포즈 분석 수행"""
        if not self.is_ready:
            raise RuntimeError("포즈 분석 서비스가 준비되지 않았습니다")
        
        import time
        start_time = time.time()
        
        try:
            # 이미지 읽기 및 디코딩
            contents = await file.read()
            np_arr = np.frombuffer(contents, np.uint8)
            image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
            
            if image is None:
                raise ValueError("이미지 디코딩 실패")
            
            # 이미지 크기 제한 (성능 최적화)
            max_size = self.settings.IMAGE_RESIZE_MAX
            height, width = image.shape[:2]
            if max(height, width) > max_size:
                scale = max_size / max(height, width)
                new_width = int(width * scale)
                new_height = int(height * scale)
                image = cv2.resize(image, (new_width, new_height))
                logger.info(f"🔧 이미지 리사이즈: {width}x{height} → {new_width}x{new_height}")
            
            # YOLO + 포즈 분석
            image, results, poses_info = self.ai_model.Load_image(image)
            
            if results is None:
                self.stats["failed_detections"] += 1
                raise RuntimeError("추론 실패")
            
            if not poses_info:
                self.stats["failed_detections"] += 1
                raise RuntimeError("사람 감지 실패")
            
            # 포즈 분석
            analyzer = self.AI_Analyze(image, results, poses_info)
            analysis_result = analyzer.print_keypoints(pose_select=pose_select)
            
            # 성능 통계 업데이트
            processing_time = time.time() - start_time
            self.stats["total_analyzed"] += 1
            self.stats["successful_detections"] += 1
            self.stats["average_processing_time"] = (
                (self.stats["average_processing_time"] * (self.stats["total_analyzed"] - 1) + processing_time)
                / self.stats["total_analyzed"]
            )
            
            # 신뢰도 계산 (간단한 휴리스틱)
            confidence = self._calculate_confidence(poses_info, analysis_result)
            
            return {
                "detected_people": len(poses_info),
                "pose_result": str(analysis_result),
                "confidence": confidence,
                "processing_time": processing_time,
                "image_size": f"{image.shape[1]}x{image.shape[0]}"
            }
            
        except Exception as e:
            self.stats["total_analyzed"] += 1
            self.stats["failed_detections"] += 1
            logger.error(f"❌ 포즈 분석 실패: {e}")
            raise
    
    def _calculate_confidence(self, poses_info: list, analysis_result: str) -> float:
        """신뢰도 계산"""
        if not poses_info:
            return 0.0
        
        # 기본 신뢰도 (사람 감지 성공)
        base_confidence = 0.7
        
        # 포즈 분석 결과에 따른 추가 점수
        if isinstance(analysis_result, str):
            if "Success" in analysis_result:
                return min(base_confidence + 0.2, 1.0)
            elif "실패" in analysis_result or "없습니다" in analysis_result:
                return max(base_confidence - 0.3, 0.1)
        
        # 감지된 사람 수에 따른 보정
        people_count = len(poses_info)
        if people_count == 1:
            confidence_bonus = 0.1
        elif people_count <= 3:
            confidence_bonus = 0.05
        else:
            confidence_bonus = -0.1  # 너무 많으면 복잡해서 신뢰도 하락
        
        return max(min(base_confidence + confidence_bonus, 1.0), 0.0)
    
    def get_supported_poses(self) -> list:
        """지원하는 포즈 목록 반환"""
        return [
            "sitting_pose",      # 앉은 자세
            "ear_pose",          # 귀 포즈
            "handsup_pose",      # 손들기
            "armscrossed_pose",  # 팔짱
            "akimbo_pose",       # 허리에 손
            "heart_pose"         # 하트 포즈
        ]
    
    def get_stats(self) -> Dict:
        """서비스 통계 반환"""
        success_rate = 0.0
        if self.stats["total_analyzed"] > 0:
            success_rate = self.stats["successful_detections"] / self.stats["total_analyzed"]
        
        return {
            **self.stats,
            "success_rate": round(success_rate, 3),
            "is_ready": self.is_ready,
            "is_model_loaded": self.is_model_loaded
        }
    
    async def cleanup(self):
        """서비스 정리"""
        logger.info("🔄 포즈 분석 서비스 정리 중...")
        
        # 통계 출력
        if self.stats["total_analyzed"] > 0:
            logger.info(f"📊 포즈 분석 통계:")
            logger.info(f"   총 분석: {self.stats['total_analyzed']}개")
            logger.info(f"   성공: {self.stats['successful_detections']}개")
            logger.info(f"   실패: {self.stats['failed_detections']}개")
            logger.info(f"   평균 처리시간: {self.stats['average_processing_time']:.2f}초")
        
        self.ai_model = None
        self.is_ready = False