import torch
from ultralytics import YOLO
import cv2
import mediapipe as mp
import numpy as np

class AI_ObjectDetector: #모델/이미지 로드 -> 객체 + 포즈 추론 -> 이미지, YOLO 추론 결과, 포즈 추론 결과
    def __init__(self, model_path="yolov8m.pt", visibility=0.5):

        # 모델 경로 설정
        self.model_path = model_path
        self.model = None

        # 모델 로딩
        self.Load_model()
        self.pose_detector = mp.solutions.pose.Pose(static_image_mode=True, min_detection_confidence=visibility)

        #리사이즈
        self.scale_size = 256

    def Load_model(self):
        """모델 로드"""
        try:
            self.model = YOLO(self.model_path)
            print(f"모델 로드 성공: {self.model_path}")
        except Exception as e:
            print(f"모델 로드 실패: {e}")
                
    def Load_image(self, image):
        """
        이미지 로드
        image: OpenCV 이미지 배열 (BGR, numpy.ndarray)
        """
        try:
            if self.model:
                results = self.model(image)
                poses_info = self.Pose_Estimator(image, results)
                return image, results, poses_info
            else:
                raise ValueError("모델 로드 실패.")
        except Exception as e:
            print(f"에러 발생: {e}")
            return None, None, None
    
    def Pose_person_crop(self, image, box, img_shape, min_size=5):
        """
        사람 크기가 min_size 이상인지 확인 -> 사람 바운딩 박스 반환
        """
        #원본 크기
        height, width = img_shape
        #사람 좌표
        x1, y1, x2, y2 = map(int, box)
        # 이미지 경계 내 좌표 클램핑
        x1 = max(0, min(x1, width - 1))
        x2 = max(0, min(x2, width - 1))
        y1 = max(0, min(y1, height - 1))
        y2 = max(0, min(y2, height - 1))

        # 박스 크기 최소 기준 확인
        if (x2 - x1 >= min_size) and (y2 - y1 >= min_size):
            return image[y1:y2, x1:x2], x1, y1

        return None, None, None

    def Pose_person_square(self, crop):
        """
        이미지 패딩을 적용 후 size로 변환 -> 변환된 이미지, 원본 높이, 원본 너비, 정사각형 한 변 길이
        """
        h, w, c = crop.shape
        max_side = max(h, w)
        # 정사각형 캔버스 (검정색) 생성
        square_img = np.zeros((max_side, max_side, 3), dtype=np.uint8)
        # 원본 이미지를 좌상단에 붙임
        square_img[:h, :w] = crop
        # 지정 크기로 리사이즈
        resized = cv2.resize(square_img, (self.scale_size, self.scale_size))
        scale = max_side / self.scale_size  # 원래 픽셀 1개가 resize 후에는 1/scale

        return resized, h, w, scale

    def Pose_person(self, image, box, img_shape):
        """
        이미지에서 사람 바운딩 박스를 자르고 전처리 (패딩+리사이즈) -> 전처리된 이미지, 박스 정보 튜플 (x1, y1, crop_w, crop_h, max_dim)
        """
        try:
            # 사람 바운딩 박스
            person_crop, x1, y1 = self.Pose_person_crop(image, box, img_shape)
            if person_crop is None:
                raise ValueError("사람의 크기가 조건을 만족하지 않음")
            # 정사각형 패딩 + 리사이즈
            padded, crop_h, crop_w, scale = self.Pose_person_square(person_crop)
            return padded, (x1, y1, crop_w, crop_h, scale)
        except Exception as e:
            print(f"에러 발생: {e}")
            return None, None

    def Pose_keypoints(self, rgb_image, box_info):
        """
        MediaPipe Pose를 사용해 RGB 이미지에서 포즈 키포인트 추출 후 원본 좌표계로 보정
        :param rgb_image: 전처리된 정사각형 RGB 이미지 (numpy array)
        :param box_info: (x1, y1, crop_w, crop_h, max_dim) 튜플
        :return: (x, y, visibility) 리스트 혹은 None
        """    
        try:
            x1, y1, crop_w, crop_h, scale = box_info

            # 포즈 추론 수행
            pose_results = self.pose_detector.process(rgb_image)

            if not pose_results.pose_landmarks:
                return []

            keypoints = []
            scale *= self.scale_size
            for lm in pose_results.pose_landmarks.landmark:
                # MediaPipe 출력 좌표는 정사각형(0~1) 기준 → crop 박스 기준으로 변환 → 원본 이미지 좌표로 보정
                x = lm.x * scale + x1
                y = lm.y * scale + y1
                keypoints.append((x, y, lm.visibility))

            return keypoints
        except Exception as e:
            return []

    def Pose_Estimator(self, image, results):
        """
        YOLO 결과 리스트 중 사람 박스에서 포즈 키포인트 추출 -> 모든 사람의 키포인트 리스트
        """
        poses_info = []
        img_shape = image.shape[:2]  # (height, width)

        for result in results:
            for res in result.boxes:
                # 사람 클래스만 처리 (ID 0)
                if int(res.cls[0]) != 0:
                    continue

                # 사람 자르고 전처리
                padded, box_info = self.Pose_person(image, res.xyxy[0].tolist(), img_shape)
                #Pose_person 예외처리
                if padded is None:
                    continue

                # RGB 변환 후 포즈 추출
                rgb_input = cv2.cvtColor(padded, cv2.COLOR_BGR2RGB)
                keypoints = self.Pose_keypoints(rgb_input, box_info)

                if keypoints:
                    poses_info.append(keypoints)
                    
        return poses_info
 
            
# # 사용 예시
# ai_model = AI_ObjectDetector()
# image, results, poses_info = ai_model.Load_image("(?).jpg")  # 이미지 로드