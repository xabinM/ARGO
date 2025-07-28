package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.applicationlist.*;
import com.argo.backend.organization.dto.classroomlist.PaginationResponseDto;
import com.argo.backend.organization.repository.classroomcreate.ClassRoomRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.classroomlist.ClassApplicationRepository;
import com.argo.backend.organization.service.exception.AccessDeniedException;
import com.argo.backend.organization.service.exception.ClassNotFoundException;
import com.argo.backend.organization.service.exception.TeacherNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationListService {

    private final ClassRoomRepository classRoomRepository;
    private final UserRepository userRepository;
    private final ClassApplicationRepository classApplicationRepository;

    public ApplicationListResponse getApplicationList(Long classId, Long userId, ApplicationListRequest request) {
        // 1. 사용자 조회 및 권한 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 선생인지 검사
        validateTeacherRole(user);

        // 2. 반 조회 (기본 정보만) - N+1 문제 방지
        ClassRoom classRoom = classRoomRepository.findByIdWithBasicDetails(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));

        // 3. 권한 검증 - 본인이 생성한 반인지 확인
        validateClassOwnership(classRoom, user);

        // 4. 상태 검증 -> (status에 아무것도 안넣으면 기본값으로 감)
        if (!request.isValidStatus()) {
            throw new IllegalArgumentException("올바르지 않은 신청 상태입니다. (pending, approved, rejected, all 중 선택)");
        }

        // 5. 페이징 처리를 위한 Pageable 생성
        Pageable pageable = PageRequest.of(
                request.getPageWithDefault() - 1, // 0-based index
                request.getSizeWithDefault()
        );

        // 6. 신청 목록 조회 (페이징 + N+1 문제 방지) - status 맞춤
        ApplicationStatus statusFilter = getApplicationStatusFilter(request.getStatusWithDefault());
        Page<ClassApplication> applicationPage = classApplicationRepository
                .findByClassRoomAndStatusWithUser(classRoom, statusFilter, pageable);

        // 7. 통계 정보를 위한 전체 신청 목록 조회 - status랑 상관없이 모두 가져옴
        List<ClassApplication> allApplications = classApplicationRepository
                .findByClassRoomWithUser(classRoom);

        // 8. 응답 DTO 생성
        return createApplicationListResponse(classRoom, applicationPage, allApplications);
    }

    private void validateTeacherRole(User user) {
        if (user.getRole() != Role.TEACHER) {
            throw new TeacherNotAllowedException("선생님만 신청 목록을 조회할 수 있습니다.");
        }
    }

    private void validateClassOwnership(ClassRoom classRoom, User user) {
        if (!classRoom.getTeacher().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("해당 반의 신청 목록을 조회할 권한이 없습니다.");
        }
    }

    private ApplicationStatus getApplicationStatusFilter(String status) {
        return switch (status.toLowerCase()) {
            case "pending" -> ApplicationStatus.PENDING;
            case "approved" -> ApplicationStatus.APPROVED;
            case "rejected" -> ApplicationStatus.REJECTED;
            case "all" -> null; // null이면 모든 상태 조회
            default -> throw new IllegalArgumentException("올바르지 않은 신청 상태입니다.");
        };
    }

    private ApplicationListResponse createApplicationListResponse(
            ClassRoom classRoom, 
            Page<ClassApplication> applicationPage, // status에 따른 신청목록 (대기, 승인, 거절 선택)
            List<ClassApplication> allApplications) { // status 상관 없이 전체

        // 지원한 반 정보
        ApplicationClassInfo classInfo = ApplicationClassInfo.from(classRoom);

        // 신청 목록
        List<ApplicationDto> applications = applicationPage.getContent().stream()
                .map(ApplicationDto::from)
                .collect(Collectors.toList());

        // 통계 정보 (대기, 승인, 거절)
        ApplicationStatistics statistics = ApplicationStatistics.from(allApplications);

        // 페이징 정보
        PaginationResponseDto pagination = PaginationResponseDto.builder()
                .totalCount((long) applicationPage.getTotalElements())
                .currentPage(applicationPage.getNumber() + 1) // 1-based
                .totalPages(applicationPage.getTotalPages())
                .pageSize(applicationPage.getSize())
                .hasNext(applicationPage.hasNext())
                .hasPrev(applicationPage.hasPrevious())
                .build();

        return ApplicationListResponse.of(classInfo, applications, statistics, pagination);
    }
}