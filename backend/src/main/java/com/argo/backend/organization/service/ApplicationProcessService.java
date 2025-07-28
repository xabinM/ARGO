package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.classroom.ClassStatus;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.applicationprocess.ApplicationProcessRequest;
import com.argo.backend.organization.dto.applicationprocess.ApplicationProcessResponse;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.classroomlist.ClassApplicationRepository;
import com.argo.backend.organization.service.exception.AccessDeniedException;
import com.argo.backend.organization.service.exception.TeacherNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationProcessService {

    private final UserRepository userRepository;
    private final ClassApplicationRepository classApplicationRepository;

    public ApplicationProcessResponse processApplication(Long classId, Long applicationId, Long userId, ApplicationProcessRequest request) {
        // 1. 입력값 유효성 검증
        validateRequest(request);

        // 2. 사용자 조회 및 권한 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 선생인지
        validateTeacherRole(user);

        // 3. 신청 조회 (N+1 문제 방지)
        ClassApplication application = classApplicationRepository.findByIdWithUserAndClassRoom(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신청입니다."));

        ClassRoom classRoom = application.getClassRoom();

        // 4. 반 ID 일치 검증
        if (!classRoom.getClassId().equals(classId)) {
            throw new IllegalArgumentException("존재하지 않는 신청입니다.");
        }

        // 5. 권한 검증 - 본인이 생성한 반인지 확인
        validateClassOwnership(classRoom, user);

        // 6. 반 상태 검증
        validateClassStatus(classRoom);

        // 7. 신청 상태 검증 - 이미 처리된 신청인지 확인
        validateApplicationStatus(application);

        // 8. 승인 처리 시 정원 체크 (max가 안넘는지 검사)
        if (request.isApprove()) {
            validateCapacity(classRoom);
        }

        // 9. 신청 처리
        processApplicationStatus(application, request);

        log.info("참여 신청 처리 완료 - userId: {}, applicationId: {}, action: {}", 
                userId, applicationId, request.getAction());

        return ApplicationProcessResponse.from(application, classRoom);
    }

    private void validateRequest(ApplicationProcessRequest request) {
        if (!request.isValidAction()) {
            throw new IllegalArgumentException("올바르지 않은 처리 유형입니다. (approve 또는 reject)");
        }
    }

    private void validateTeacherRole(User user) {
        if (user.getRole() != Role.TEACHER) {
            throw new TeacherNotAllowedException("선생님만 신청을 처리할 수 있습니다.");
        }
    }

    private void validateClassOwnership(ClassRoom classRoom, User user) {
        if (!classRoom.getTeacher().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("해당 신청을 처리할 권한이 없습니다.");
        }
    }

    private void validateClassStatus(ClassRoom classRoom) {
        if (classRoom.getStatus() != ClassStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성 상태의 반은 신청을 처리할 수 없습니다.");
        }
    }

    private void validateApplicationStatus(ClassApplication application) {
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 신청입니다.");
        }
    }

    private void validateCapacity(ClassRoom classRoom) {
        // 현재 승인된 학생 수 계산
        long currentApprovedCount = classApplicationRepository
                .countByClassRoomAndStatus(classRoom, ApplicationStatus.APPROVED);

        if (currentApprovedCount >= classRoom.getMaxStudents()) {
            throw new IllegalArgumentException("반의 최대 인원을 초과하여 승인할 수 없습니다.");
        }
    }

    private void processApplicationStatus(ClassApplication application, ApplicationProcessRequest request) {
        LocalDateTime processedAt = LocalDateTime.now();
        
        if (request.isApprove()) {
            application.setStatus(ApplicationStatus.APPROVED);
        } else {
            application.setStatus(ApplicationStatus.REJECTED);
        }
        
        application.setProcessedAt(processedAt); // 처리 시간 설정
        classApplicationRepository.save(application); // 저장
    }
}