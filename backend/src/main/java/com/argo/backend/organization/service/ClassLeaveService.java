package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.classleave.*;
import com.argo.backend.organization.repository.classroomcreate.ClassRoomRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.classroomlist.ClassApplicationRepository;
import com.argo.backend.organization.service.exception.ClassNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClassLeaveService {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final ClassApplicationRepository classApplicationRepository;

    public ClassLeaveResponse leaveClass(Long classId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (user.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("학생만 반에서 탈퇴할 수 있습니다.");
        }

        ClassRoom classRoom = classRoomRepository.findByIdWithBasicDetails(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));

        // 활동 진행 중 체크
        if (classRoom.getActivityDate() != null && !classRoom.getActivityDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("활동 진행 중에는 탈퇴할 수 없습니다.");
        }

        // 참여 확인
        ClassApplication application = classApplicationRepository.findByUserAndClassRoom(user, classRoom)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 반입니다."));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new IllegalArgumentException("참여하지 않은 반입니다.");
        }

        LocalDateTime leftAt = LocalDateTime.now();
        
        // 응답 생성을 위한 정보 수집 (삭제 전에 수집)
        LeftClassInfo leftClassInfo = LeftClassInfo.from(classRoom);
        StudentLeaveInfo studentInfo = StudentLeaveInfo.of(user, application, leftAt);
        TeamLeaveInfo teamInfo = TeamLeaveInfo.of(user.getTeam(), leftAt);
        
        // 팀에서 제거
        if (user.getTeam() != null) {
            user.setTeam(null);
            userRepository.save(user);
        }

        // 참여 기록 삭제
        classApplicationRepository.delete(application);
        
        // 삭제 후 반 상태 정보 (탈퇴 반영된 상태)
        ClassStatusInfo classStatus = ClassStatusInfo.from(classRoom);

        log.info("반 탈퇴 완료 - userId: {}, classId: {}, wasInTeam: {}", 
                userId, classId, teamInfo.getWasInTeam());

        return ClassLeaveResponse.of(leftClassInfo, studentInfo, teamInfo, classStatus);
    }
}