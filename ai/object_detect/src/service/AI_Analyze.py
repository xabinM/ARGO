import cv2
class AI_Analyze: #이미지, YOLO 추론 결과, 포즈 추론 결과
    def __init__(self, image, results, poses_info, visibility=0.5):
        self.image = image
        self.results = results
        self.visibility = visibility
        self.okval = 1.1
        # 포즈 필터링
        self.poses_info = self.filter_poses_by_visibility(poses_info)
        
    def filter_poses_by_visibility(self, poses_info):
        """visibility 낮은 poses 필터"""
        if not poses_info:
            return []
        filtered_poses = []
        for person_keypoints in poses_info:
            def get_point(idx):
                if idx < len(person_keypoints):
                    x, y, v = person_keypoints[idx]
                    if v > self.visibility:
                        return x, y
                return None, None

            def get_extreme(values):
                valid = [v for v in values if v is not None]
                if not valid:
                    return None, None
                return min(valid), max(valid)
            
            # 얼굴 0~10
            face_indices = list(range(11))
            face_coords = []
            for idx in face_indices:
                point = get_point(idx)
                if point[0] is not None:
                    face_coords.append(point)
            
            if face_coords:
                face_x = [x for x, _ in face_coords]
                face_y = [y for _, y in face_coords]
                avg_face_x = sum(face_x) / len(face_x)
                face_width = max(face_x) - min(face_x)
                if not face_width:
                    avg_face_x = face_width = None
                face_min_y = min(face_y)
                face_max_y = max(face_y)
            else:
                avg_face_x = face_width = face_min_y = face_max_y = None

            # 어깨, 엉덩이, 무릎, 발목, 팔꿈치, 손목
            left_sh_x, left_sh_y = get_point(11)
            right_sh_x, right_sh_y = get_point(12)
            left_elbow_x, left_elbow_y = get_point(13)
            right_elbow_x, right_elbow_y = get_point(14)
            left_wrist_x, left_wrist_y = get_point(15)
            right_wrist_x, right_wrist_y = get_point(16)
            left_hip_x, left_hip_y = get_point(23)
            right_hip_x, right_hip_y = get_point(24)
            left_knee_x, left_knee_y = get_point(25)
            right_knee_x, right_knee_y = get_point(26)
            left_ankle_x, left_ankle_y = get_point(27)
            right_ankle_x, right_ankle_y = get_point(28)

            # 특수
            shoulder_max_y, _ = get_extreme([left_sh_y, right_sh_y])                     # 어깨 y 최대
            elbow_max_y, _    = get_extreme([left_elbow_y, right_elbow_y])               # 팔꿈치 y 최대
            wrist_max_y, _    = get_extreme([left_wrist_y, right_wrist_y])               # 손목 y 최대

            _, hip_min_y      = get_extreme([left_hip_y, right_hip_y])                   # 엉덩이 y 최소
            _, knee_min_y     = get_extreme([left_knee_y, right_knee_y])                 # 무릎 y 최소
            _, ankle_min_y    = get_extreme([left_ankle_y, right_ankle_y])               # 발목 y 최소

            # 최종 리스트 정리
            filtered_poses.append([
            avg_face_x, face_width,             # 0~1   : 얼굴 중심 x좌표, 얼굴 폭
            face_max_y, face_min_y,             # 2~3   : 얼굴 y 최댓값, 최솟값

            left_sh_x, left_sh_y,               # 4~5   : 왼쪽 어깨 x, y
            right_sh_x, right_sh_y,             # 6~7   : 오른쪽 어깨 x, y
            shoulder_max_y,                     # 8     : 어깨 y 최댓값

            left_elbow_x, left_elbow_y,         # 9~10  : 왼쪽 팔꿈치 x, y
            right_elbow_x, right_elbow_y,       # 11~12 : 오른쪽 팔꿈치 x, y
            elbow_max_y,                        # 13    : 팔꿈치 y 최댓값

            left_wrist_x, left_wrist_y,         # 14~15 : 왼쪽 손목 x, y
            right_wrist_x, right_wrist_y,       # 16~17 : 오른쪽 손목 x, y
            wrist_max_y,                        # 18    : 손목 y 최댓값

            left_hip_x, left_hip_y,             # 19~20 : 왼쪽 엉덩이 x, y
            right_hip_x, right_hip_y,           # 21~22 : 오른쪽 엉덩이 x, y
            hip_min_y,                          # 23    : 엉덩이 y 최솟값

            left_knee_x, left_knee_y,           # 24~25 : 왼쪽 무릎 x, y
            right_knee_x, right_knee_y,         # 26~27 : 오른쪽 무릎 x, y
            knee_min_y,                         # 28    : 무릎 y 최솟값

            left_ankle_x, left_ankle_y,         # 29~30 : 왼쪽 발목 x, y
            right_ankle_x, right_ankle_y,       # 31~32 : 오른쪽 발목 x, y
            ankle_min_y                         # 33    : 발목 y 최솟값
        ])

        return filtered_poses

    def print_keypoints(self, pose_select):
        if not self.poses_info:
            return []
        all_result = []
        for person_idx in range(len(self.poses_info)):
            #정보
            if pose_select == "sitting_pose":
                result, reason = self.check_sitting_pose(person_idx)
            elif pose_select == "ear_pose":
                result, reason = self.check_ear_pose(person_idx)
            elif pose_select == "handsup_pose":
                result, reason = self.check_handsup_pose(person_idx)
            elif pose_select == "armscrossed_pose":
                result, reason = self.check_armscrossed_pose(person_idx)
            elif pose_select == "akimbo_pose":
                result, reason = self.check_akimbo_pose(person_idx)
            elif pose_select == "heart_pose":
                result, reason = self.check_heart_pose(person_idx)
            else:
                result, reason = False, "Unknown pose"
            if result:
                return result
            all_result.append(reason)
        freq_dict = {}
        for reason in all_result:
            freq_dict[reason] = freq_dict.get(reason, 0) + 1

        # 최대 빈도 사유 추출
        most_common_reason = max(freq_dict, key=freq_dict.get)
        return most_common_reason
            
    def check_sitting_pose(self, person_idx=0):
        """
        앉은 자세 판별 함수
        - 무릎과 엉덩이가 가까운지 판단
        - 발목과 엉덩이가 가까운지 판단
        """

        hip_min_y = self.poses_info[person_idx][23]
        knee_min_y = self.poses_info[person_idx][28]
        ankle_min_y = self.poses_info[person_idx][33]
        if hip_min_y is None:
            return False, "엉덩이를 찾을 수 없습니다"

        if knee_min_y is not None:
            knee_hip_diff = max(hip_min_y, knee_min_y) / max(min(hip_min_y, knee_min_y), 1e-5)
        else:
            knee_hip_diff = 20  # 비교 불가 시 큰 값

        if ankle_min_y is not None:
            ankle_hip_diff = max(hip_min_y, ankle_min_y) / max(min(hip_min_y, ankle_min_y), 1e-5)
        else:
            ankle_hip_diff = 20  # 비교 불가 시 큰 값

        if min(knee_hip_diff, ankle_hip_diff) <= self.okval:
            return True, "Success"
        elif min(knee_hip_diff, ankle_hip_diff) == 20:
            return False, "무릎과 발목을 찾을 수 없습니다"
        else:
            return False, "무릎이나 발목이 엉덩이와 멉니다"


    def check_ear_pose(self, person_idx=0):
        """
        귀 포즈 판별 함수
         - 손목이 얼굴보다 위에 있음 (y좌표 기준)
         - 손목 x좌표가 얼굴 부위와 가까움
        """
        avg_face_x = self.poses_info[person_idx][0]
        face_width = self.poses_info[person_idx][1]
        face_max_y = self.poses_info[person_idx][2]
        if avg_face_x is None:
            return False, "얼굴 정보가 없습니다"
        
        left_wrist_x = self.poses_info[person_idx][14]
        left_wrist_y = self.poses_info[person_idx][15]
        right_wrist_x = self.poses_info[person_idx][16]
        right_wrist_y = self.poses_info[person_idx][17]

        def check(wrist_x, wrist_y):
            if wrist_y is None:
                return False, "손목을 찾을 수 없습니다"
            if wrist_y < face_max_y:
                x_diff_ratio = abs(wrist_x - avg_face_x) / face_width
                if x_diff_ratio <= self.okval * 1.7:
                    return True, "Success"
                else:
                    return False, "손목이 머리와 멉니다"
            return False, "손목이 머리 밑에 있습니다"

        left_result, left_reason = check(left_wrist_x, left_wrist_y)
        right_result, right_reason = check(right_wrist_x, right_wrist_y)
        if left_result and right_result:
            return True, "Success all"
        elif left_result:
            return True, "Success left"
        elif right_result:
            return True, "Success right"
        else:
            return False, "왼 " + left_reason + " 그리고 오른 " + right_reason

    def check_handsup_pose(self, person_idx=0, hand='all'):
        """
        손을 위로 뻗은 자세를 판별하는 함수
        hand: left, right, default=all
         - 어깨 팔꿈치 손목중 2개 인식 -> 손목으로 갈수록 높다(y좌표 기준)
        """
        def check(values):
                valid = [v for v in values if v is not None]
                if len(valid) < 2:
                    return False, "팔의 인식된 부분이 2개 미만입니다"
                if valid[0] > valid[-1]:
                    return True, "Success"
                else:
                    return False, "손을 내리고 있습니다"

        left = [self.poses_info[person_idx][5], self.poses_info[person_idx][10], self.poses_info[person_idx][15]]
        right = [self.poses_info[person_idx][7], self.poses_info[person_idx][12], self.poses_info[person_idx][17]]
        
        left_result, left_reason = check(left)
        right_result, right_reason = check(right)

        if hand == 'left':
            if left_result:
                return True, left_reason + " left"
            else:
                return False, "왼" + left_reason
        elif hand == 'right':
            if right_result:
                return True, right_reason + " right"
            else:
                return False, "오른" + right_reason
        else:  # hand == 'all'
            if left_result and right_result:
                return True, left_reason + " all"
            else:
                return False, "왼" + left_reason + " 그리고 오른" + right_reason

    def check_armscrossed_pose(self, person_idx=0):
        """
        팔짱 낀 자세 판단:
        - 팔꿈치와 손목 y좌표 비슷
        - 어깨보다 아래
        - 엉덩이보다 위
        """
        left = [
            self.poses_info[person_idx][5],   # 왼쪽 어깨 y
            self.poses_info[person_idx][10],  # 왼쪽 팔꿈치 y
            self.poses_info[person_idx][15],  # 왼쪽 손목 y
            self.poses_info[person_idx][20]   # 왼쪽 엉덩이 y
        ]
        right = [
            self.poses_info[person_idx][7],   # 오른쪽 어깨 y
            self.poses_info[person_idx][12],  # 오른쪽 팔꿈치 y
            self.poses_info[person_idx][17],  # 오른쪽 손목 y
            self.poses_info[person_idx][22]   # 오른쪽 엉덩이 y
        ]

        def arm_folded(y_list):
            y_shoulder, y_elbow, y_wrist, y_hip = y_list

            # 팔꿈치, 손목 둘 다 없으면 판단 불가
            if y_elbow is None and y_wrist is None:
                return False, "팔꿈치와 손목을 찾을 수 없습니다"
            if y_shoulder is None and y_hip is None:
                return False, "어깨와 엉덩이를 찾을 수 없습니다"

            # 팔꿈치와 손목 y차이가 너무 나면 False
            if y_elbow is not None and y_wrist is not None:
                diff_ratio = max(y_elbow, y_wrist) / max(min(y_elbow, y_wrist), 1e-5)
                if diff_ratio > self.okval:
                    return False, "손목이 팔꿈치와 멉니다"

            # 중간 높이 (팔꿈치/손목 중 더 아래에 있는 값)
            y_middle = max(v for v in [y_elbow, y_wrist] if v is not None)

            if y_shoulder is not None and y_middle <= y_shoulder:
                return False, "손을 들고 있습니다"
            if y_hip is not None and y_middle >= y_hip:
                return False, "손을 내리고 있습니다"

            return True, "Success"
    
        left_result, left_reason = arm_folded(left)
        right_result, right_reason = arm_folded(right)

        if left_result:
            return True, left_reason + " left"
        elif right_result:
            return True, right_reason + " right"
        else:
            return False, "왼" + left_reason + " 그리고 오른" + right_reason

    def check_akimbo_pose(self, person_idx=0):
        """
        손을 허리에 얹은 자세 판단:
        - 팔꿈치보다 손목 x좌표가 몸에 가까움
        - 어깨보다 아래
        - 엉덩이보다 위
        """
        left = [
            self.poses_info[person_idx][4],   # 왼쪽 어깨 x
            self.poses_info[person_idx][5],   # 왼쪽 어깨 y
            self.poses_info[person_idx][9],   # 왼쪽 팔꿈치 x
            self.poses_info[person_idx][10],  # 왼쪽 팔꿈치 y
            self.poses_info[person_idx][14],  # 왼쪽 손목 x
            self.poses_info[person_idx][15],  # 왼쪽 손목 y
            self.poses_info[person_idx][19],  # 왼쪽 엉덩이 x
            self.poses_info[person_idx][20]   # 왼쪽 엉덩이 y
        ]
        right = [
            self.poses_info[person_idx][6],   # 오른쪽 어깨 x
            self.poses_info[person_idx][7],   # 오른쪽 어깨 y
            self.poses_info[person_idx][11],  # 오른쪽 팔꿈치 x
            self.poses_info[person_idx][12],  # 오른쪽 팔꿈치 y
            self.poses_info[person_idx][16],  # 오른쪽 손목 x
            self.poses_info[person_idx][17],  # 오른쪽 손목 y
            self.poses_info[person_idx][21],  # 오른쪽 엉덩이 x
            self.poses_info[person_idx][22]   # 오른쪽 엉덩이 y
        ]

        def arm_folded(v_list):
            x_shoulder, y_shoulder, x_elbow, y_elbow, x_wrist, y_wrist, x_hip, y_hip = v_list

            # 팔꿈치, 손목 둘 다 없으면 판단 불가
            if y_elbow is None and y_wrist is None:
                return False, "팔꿈치와 손목을 찾을 수 없습니다"
            if y_shoulder is None and y_hip is None:
                return False, "어깨와 엉덩이를 찾을 수 없습니다"
            
            # 둘 다 있으면 x 위치 비교
            if x_elbow is not None and x_wrist is not None:
                # 몸 x값
                diff = [v for v in [x_shoulder, x_hip] if v is not None]
                diff = sum(diff) / len(diff)
                # 손목보다 팔꿈치가 몸에 더 가깝다면 False
                if (x_elbow - diff) / max((x_wrist - diff), 1e-5) <= 0:
                    return False, "손목이 팔꿈치보다 몸에서 멉니다"

            # 어깨 -> 팔꿈치/손목은 어깨 아래
            if y_shoulder is not None:
                y_middle = min(v for v in [y_elbow, y_wrist] if v is not None)
                if y_middle <= y_shoulder:
                    return False, "손을 들고 있습니다"
            # 엉덩이 -> 팔꿈치/손목은 엉덩이 위
            if y_hip is not None:
                y_middle = max(v for v in [y_elbow, y_wrist] if v is not None)
                if y_middle >= y_hip:
                    return False, "손을 내리고 있습니다"
            return True, "Success"
        
        left_result, left_reason = arm_folded(left)
        right_result, right_reason = arm_folded(right)

        if left_result:
            return True, left_reason + " left"
        elif right_result:
            return True, right_reason + " right"
        else:
            return False, "왼" + left_reason + " 그리고 오른" + right_reason

    def check_heart_pose(self, person_idx=0):
        """
        손을 머리에 얹은 자세 판단:
        - 양 손목이 머리보다 위에 있어야 함 
        - 양 손목은 머리 근처에 있어야 함
        - 팔꿈치/손목은 어깨보다 위에 있어야 함
        - 팔꿈치보다 어깨 x좌표가 몸에 가까움
        # - 손목이 손가락보다 위에 있어야 함
        """
        avg_face_x = self.poses_info[person_idx][0]
        face_width = self.poses_info[person_idx][1]   
        face_min_y = self.poses_info[person_idx][3]
        if avg_face_x is None:
            return False, "얼굴 정보가 없습니다"
        left_shoulder_x = self.poses_info[person_idx][4]
        right_shoulder_x = self.poses_info[person_idx][6]
        shoulder_max_y = self.poses_info[person_idx][8]
        left_elbow_x = self.poses_info[person_idx][9]
        right_elbow_x = self.poses_info[person_idx][11]
        elbow_max_y = self.poses_info[person_idx][13]
        left_wrist_x = self.poses_info[person_idx][14]
        right_wrist_x = self.poses_info[person_idx][16]
        wrist_max_y = self.poses_info[person_idx][18]

        if left_wrist_x is None and right_wrist_x is None:
            return False, "손목을 찾을 수 없습니다"
        if wrist_max_y > face_min_y:
            return False, "손목이 머리 밑에 있습니다"
        # 양 손목은 머리 근처에 있어야 함
        if abs(left_wrist_x - avg_face_x) / face_width > self.okval * 1.7:
            return False, "왼 손목이 머리와 멉니다"
        elif abs(right_wrist_x - avg_face_x) / face_width > self.okval * 1.7:
            return False, "오른 손목이 머리와 멉니다"
        # 팔꿈치/손목은 어깨보다 위에 있어야 함 
        if shoulder_max_y is not None:
            if wrist_max_y > shoulder_max_y:
                return False, "손을 내리고 있습니다"
            if elbow_max_y is not None:
                if elbow_max_y > shoulder_max_y:
                    return False, "팔꿈치가 어깨 밑에 있습니다"
        # 팔꿈치보다 어깨 x좌표가 몸에 가까움
        if None not in [left_shoulder_x, right_shoulder_x, left_elbow_x, right_elbow_x]:
            if min(left_shoulder_x, right_shoulder_x) < min(left_elbow_x, right_elbow_x) or max(left_shoulder_x, right_shoulder_x) > max(left_elbow_x, right_elbow_x):
                return False, "팔꿈치가 어깨보다 몸에서 가깝니다"
        return True, "Success"

# # 사용 예시
# analyze = AI_Analyze(image, results, poses_info)
# analyze.draw_keypoints_custom_colors(40)