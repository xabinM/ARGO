package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.classroom.ClassStatus;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.classapply.ClassApplyRequest;
import com.argo.backend.organization.dto.classapply.ClassApplyResponse;
import com.argo.backend.organization.repository.classroomcreate.ClassRoomRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.classroomlist.ClassApplicationRepository;
import com.argo.backend.organization.service.exception.TeacherNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClassApplicationService {

    private final ClassRoomRepository classRoomRepository;
    private final UserRepository userRepository;
    private final ClassApplicationRepository classApplicationRepository;

    public ClassApplyResponse applyToClass(Long userId, ClassApplyRequest request) {
        // 1. 입력값 유효성 검증
        if (!request.isValid()) {
            throw new IllegalArgumentException("초대 코드를 입력해주세요.");
        }

        // 2. 사용자 조회 및 권한 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 선생 막기
        validateUserRole(user);

        // 3. 초대 코드로 반 조회 (연관 데이터와 함께) - N+1 문제 방지
        ClassRoom classRoom = classRoomRepository.findByInviteCodeWithDetails(request.getInviteCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));

        // 4. 반 상태 및 활동 날짜 검증
        validateClassStatus(classRoom);

        // 5. 중복 신청 검증
        validateDuplicateApplication(user, classRoom);

        // 6. 신청 생성 및 저장
        ClassApplication application = createApplication(user, classRoom);
        ClassApplication savedApplication = classApplicationRepository.save(application);

        log.info("반 참여 신청 완료 - userId: {}, classId: {}, applicationId: {}", 
                userId, classRoom.getClassId(), savedApplication.getApplicationId());

        return ClassApplyResponse.from(savedApplication, classRoom);
    }

    private void validateUserRole(User user) {
        if (user.getRole() != Role.STUDENT) {
            throw new TeacherNotAllowedException("선생님은 반에 신청할 수 없습니다.");
        }
    }

    private void validateClassStatus(ClassRoom classRoom) {
        // 반 활성 상태 검증
        if (classRoom.getStatus() != ClassStatus.ACTIVE) {
            throw new IllegalArgumentException("현재 신청할 수 없는 반입니다.");
        }

        // 활동 날짜 검증
        if (classRoom.getActivityDate() == null) {
            throw new IllegalArgumentException("활동 날짜가 설정되지 않은 반에는 신청할 수 없습니다.");
        }

        // 지났는지 검증
        if (classRoom.getActivityDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("활동 날짜가 지난 반에는 신청할 수 없습니다.");
        }

    }

    private void validateDuplicateApplication(User user, ClassRoom classRoom) {
        boolean hasExistingApplication = classApplicationRepository
                .existsByUserAndClassRoom(user, classRoom);
        
        if (hasExistingApplication) {
            throw new IllegalArgumentException("이미 신청했거나 참여 중인 반입니다.");
        }
    }

    private ClassApplication createApplication(User user, ClassRoom classRoom) {
        return ClassApplication.builder()
                .user(user)
                .classRoom(classRoom)
                .status(ApplicationStatus.PENDING)
                .processedAt(null) // PENDING 상태일 때는 처리 시간 없음
                .build();
    }
}