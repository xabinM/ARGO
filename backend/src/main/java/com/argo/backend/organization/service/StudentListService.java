package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.classroomlist.PaginationResponseDto;
import com.argo.backend.organization.dto.studentlist.*;
import com.argo.backend.organization.repository.classroomcreate.ClassRoomRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.classroomlist.ClassApplicationRepository;
import com.argo.backend.organization.service.exception.AccessDeniedException;
import com.argo.backend.organization.service.exception.ClassNotFoundException;
import com.argo.backend.organization.service.exception.TeacherNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
public class StudentListService {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final ClassApplicationRepository classApplicationRepository;

    public StudentListResponse getStudentList(Long classId, Long userId, StudentListRequest request) {
        // 1. 사용자 조회 및 권한 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        validateTeacherRole(user);

        // 2. 반 조회 - applications 데이터 로드 (MultipleBagFetchException 방지)
        ClassRoom classRoom = classRoomRepository.findByIdWithApplications(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        
        // teams 데이터 별도 로드 (팀 요약 정보 생성에 필요)
        ClassRoom classRoomWithTeams = classRoomRepository.findByIdWithTeams(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        classRoom.setTeams(classRoomWithTeams.getTeams());

        // 3. 권한 검증 - 본인이 생성한 반인지 확인
        validateClassOwnership(classRoom, user);

        // 4. 상태 검증
        validateStatus(request);

        // 5. 승인된 학생들만 필터링
        List<ClassApplication> approvedApplications = classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .collect(Collectors.toList());

        // 6. 상태별 필터링 (모두 조회할지, 팀이 배정된 애들만 조회 할지, 안된애들만 조회 할지)
        List<ClassApplication> filteredApplications = filterByStatus(approvedApplications, request);

        // 7. 페이징 처리
        Pageable pageable = PageRequest.of(
                request.getPageWithDefault() - 1,
                request.getSizeWithDefault()
        );

        // 메모리에서 필터링된 데이터를 페이징 처리해서 Spring Page 형태로 반환
        Page<ClassApplication> applicationPage = createPageFromList(filteredApplications, pageable);

        // 8. DTO 변환
        List<StudentInfo> students = applicationPage.getContent().stream()
                .map(StudentInfo::from) // calssApplication을 studentinfo.from(classapplication) 으로 넣어서 studentinfo로 변환
                .collect(Collectors.toList());

        // 9. 응답 생성
        return createStudentListResponse(classRoom, students, applicationPage);
    }

    private void validateTeacherRole(User user) {
        if (user.getRole() != Role.TEACHER) {
            throw new TeacherNotAllowedException("선생님만 학생 목록을 조회할 수 있습니다.");
        }
    }

    private void validateClassOwnership(ClassRoom classRoom, User user) {
        if (!classRoom.getTeacher().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("해당 반의 학생 목록을 조회할 권한이 없습니다.");
        }
    }

    private void validateStatus(StudentListRequest request) {
        if (!request.isValidStatus()) {
            throw new IllegalArgumentException("올바르지 않은 상태값입니다. (all, assigned, unassigned, team-{teamId} 중 선택)");
        }
    }

    private List<ClassApplication> filterByStatus(List<ClassApplication> applications, StudentListRequest request) {
        String status = request.getStatusWithDefault();
        
        switch (status) {
            case "all":
                return applications;
            case "assigned": // 팀에 배정된
                return applications.stream()
                        .filter(app -> app.getUser().getTeam() != null)
                        .collect(Collectors.toList());
            case "unassigned": // 팀에 배정 안된
                return applications.stream()
                        .filter(app -> app.getUser().getTeam() == null)
                        .collect(Collectors.toList());
            default:
                if (request.isTeamFilter()) {
                    Long teamId = request.getTeamIdFromFilter();
                    if (teamId == null) {
                        throw new IllegalArgumentException("올바르지 않은 팀 ID 형식입니다.");
                    }
                    return applications.stream()
                            .filter(app -> app.getUser().getTeam() != null && 
                                         app.getUser().getTeam().getTeamId().equals(teamId))
                            .collect(Collectors.toList());
                }
                return applications;
        }
    }

    private Page<ClassApplication> createPageFromList(List<ClassApplication> applications, Pageable pageable) {
        int start = (int) pageable.getOffset(); // 시작 인덱스 계산
        int end = Math.min((start + pageable.getPageSize()), applications.size()); // 끝 인덱스 계산
        
        List<ClassApplication> pageContent = applications.subList(start, end); // 전체 인덱스에서 해당 페이지 구간만 추출
        return new PageImpl<>(pageContent, pageable, applications.size());
    }

    private StudentListResponse createStudentListResponse(ClassRoom classRoom, 
                                                        List<StudentInfo> students,
                                                        Page<ClassApplication> applicationPage) {
        // 반 정보
        StudentListClassInfo classInfo = StudentListClassInfo.from(classRoom);

        // 팀 요약 정보
        TeamSummary teamSummary = TeamSummary.from(classRoom);

        // 페이징 정보
        PaginationResponseDto pagination = PaginationResponseDto.builder()
                .totalCount((long) applicationPage.getTotalElements())
                .currentPage(applicationPage.getNumber() + 1)
                .totalPages(applicationPage.getTotalPages())
                .pageSize(applicationPage.getSize())
                .hasNext(applicationPage.hasNext())
                .hasPrev(applicationPage.hasPrevious())
                .build();

        return StudentListResponse.of(classInfo, students, teamSummary, pagination);
    }
}