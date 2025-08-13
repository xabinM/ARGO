from fastapi import APIRouter, UploadFile, File, HTTPException, Form
import numpy as np
import cv2
from src.service.AI_ObjectDetector import AI_ObjectDetector
from src.service.AI_Analyze import AI_Analyze

router = APIRouter()

# 모델 객체는 라우터 모듈 로딩 시 1회만 생성 (글로벌)
ai_model = AI_ObjectDetector("model/yolov8m.pt")

@router.post("/predict/")
async def predict(file: UploadFile = File(...), pose_select: str = Form(None), people_count: int = Form(None)):
    try:
        # 1. 이미지 읽기 및 디코딩
        contents = await file.read()
        np_arr = np.frombuffer(contents, np.uint8)
        image = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
        if image is None:
            raise HTTPException(status_code=400, detail="이미지 디코딩 실패")
        
        # 2. 추론 수행
        image, results, poses_info = ai_model.Load_image(image)
        if results is None:
            raise HTTPException(status_code=500, detail="추론 실패")
        elif not poses_info:
            raise HTTPException(status_code=404, detail="사람 감지 실패 / 포즈 탐색 실패")

        # 3. 후처리
        processor = AI_Analyze(image, results, poses_info)
        json_result = processor.print_keypoints(pose_select=pose_select,people_count=people_count)

        # 4. JSON 반환
        return json_result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"예상치 못한 오류 발생: {str(e)}")