import os
from dotenv import load_dotenv
import logging
from typing import Dict
from datetime import datetime
from .main_pipeline import ARGOPipeline

logger = logging.getLogger(__name__)

class ARGOAPIHandler:
   """ARGO API 핸들러 - FastAPI 연동용"""
   
   def __init__(self):
       self.pipeline = ARGOPipeline()
       self.initialized = False
   
   async def initialize_api(self, data_file: str):
       """API 서버 초기화"""
       await self.pipeline.initialize(data_file)
       self.initialized = True
       logger.info("🌐 ARGO API 서버 준비 완료")
   
   async def handle_mission_request(self, request_data: Dict) -> Dict:
       """API 요청 처리 (Android 앱 → 서버)"""
       if not self.initialized:
           return {"error": "서버가 초기화되지 않았습니다"}
       
       try:
           # 1. 요청 유효성 검사
           required_fields = ['location', 'grade', 'group_size']
           for field in required_fields:
               if field not in request_data:
                   return {"error": f"필수 필드 누락: {field}"}
           
           # 2. 미션 타입 검증 (AR 미션 제외)
           allowed_mission_types = ['퀴즈', '관찰미션', '체험미션', '사진미션']
           mission_type = request_data.get('mission_type', None)
           if mission_type and mission_type not in allowed_mission_types:
               request_data['mission_type'] = None  # 자동 결정으로 변경
               logger.warning(f"지원하지 않는 미션 타입: {mission_type}, 자동 결정으로 변경")
           
           # 3. 미션 생성
           result = await self.pipeline.generate_argo_mission(request_data)
           
           # 4. API 응답 형태로 변환
           api_response = {
               "success": True,
               "data": {
                   "student_mission": result.student_version,
                   "teacher_info": result.teacher_version,
                   "metadata": result.metadata
               },
               "timestamp": datetime.now().isoformat()
           }
           
           return api_response
           
       except Exception as e:
           logger.error(f"API 요청 처리 실패: {e}")
           return {
               "success": False,
               "error": str(e),
               "timestamp": datetime.now().isoformat()
           }
   
   async def handle_mission_type_analysis(self, request_data: Dict) -> Dict:
       """미션 타입 분석 API 엔드포인트 (새로 추가)"""
       if not self.initialized:
           return {"error": "서버가 초기화되지 않았습니다"}
       
       try:
           required_fields = ['location']
           for field in required_fields:
               if field not in request_data:
                   return {"error": f"필수 필드 누락: {field}"}
           
           analysis = await self.pipeline.get_mission_type_analysis(
               location=request_data['location'],
               grade=request_data.get('grade', 5),
               group_size=request_data.get('group_size', 4),
               duration=request_data.get('duration_minutes', 30)
           )
           
           return {
               "success": True,
               "data": analysis,
               "timestamp": datetime.now().isoformat()
           }
           
       except Exception as e:
           logger.error(f"미션 타입 분석 실패: {e}")
           return {
               "success": False,
               "error": str(e),
               "timestamp": datetime.now().isoformat()
           }
   
   async def get_performance_metrics(self) -> Dict:
       """성능 메트릭 조회 API (새로 추가)"""
       if not self.initialized:
           return {"error": "서버가 초기화되지 않았습니다"}
       
       try:
           metrics = self.pipeline.get_performance_metrics()
           return {
               "success": True,
               "data": metrics,
               "timestamp": datetime.now().isoformat()
           }
       except Exception as e:
           logger.error(f"성능 메트릭 조회 실패: {e}")
           return {
               "success": False,
               "error": str(e),
               "timestamp": datetime.now().isoformat()
           }
   
   def get_supported_mission_types(self) -> Dict:
       """지원하는 미션 타입 목록 반환"""
       return {
           "supported_types": [
               {"id": "퀴즈", "name": "🎯 퀴즈 도전", "description": "문제를 풀며 학습하는 미션"},
               {"id": "관찰미션", "name": "👀 관찰 탐험", "description": "직접 관찰하며 특징을 찾는 미션"},
               {"id": "체험미션", "name": "🎭 역할 체험", "description": "역할놀이를 통한 체험 미션"},
               {"id": "사진미션", "name": "📸 포토 미션", "description": "창의적인 사진을 찍는 미션"}
           ],
           "auto_determination": {
               "enabled": True,
               "description": "미션 타입을 지정하지 않으면 장소 특성에 따라 자동으로 최적의 타입을 결정합니다"
           },
           "note": "AR 오브젝트는 각 스팟에서 자동으로 활성화됩니다"
       }