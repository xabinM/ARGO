package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.classroom.ClassStatus;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.ClassStatusFilter;
import com.argo.backend.organization.dto.classroomlist.ClassListRequestDto;
import com.argo.backend.organization.dto.classroomlist.ClassListResponseDto;
import com.argo.backend.organization.dto.classroomlist.ClassListWithPaginationDto;
import com.argo.backend.organization.dto.classroomlist.PaginationResponseDto;
import com.argo.backend.organization.repository.classroomcreate.ClassRoomRepository;
import com.argo.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ClassListService {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;

    // 반 리스트 보기
    public ClassListWithPaginationDto findByList(Long userId, ClassListRequestDto request) {

        Optional<User> userOptional = userRepository.findById(userId);
        User user = userOptional.orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        int pageNum = Math.max(0, request.getPageNum() - 1); // 1페이지 -> 0 조회해야지 (index)
        int pageSize = Math.max(1, request.getPageSize()); // 한페이지에 최소 1개의 항목이 있어야 해서 !
        String status = request.getStatus();

        // PageRequest는 spring에서 제공해주는 거임
        // 만든 날짜를 기준으로 내림차순으로 정렬하고 pagenum, pagesize에 맞출거임 그 페이지 정보를 pageable로 넣어줌
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        // class 목록을 page에 넘겨줄건데 미리 만들어놓음
        Page<ClassRoom> classPage;

        // 사용자 역할에 따라 다른 조회 로직 실행 (JOIN FETCH 사용)
        if (user.getRole() == Role.TEACHER) { // 선생 : 본인이 생성한 반만 조회 가능
            ClassStatus classStatus = parseClassStatus(status);
            classPage = classRoomRepository.findByTeacherAndStatusWithDetails(user, classStatus, pageable); // JOIN FETCH로 연관 데이터까지 한번에 조회


        } else { // 학생 : 본인이 참여 중인 반만 조회 가능
            ClassStatus classStatus = parseClassStatus(status);
            // 학생은 승인된 신청만 조회 (APPROVED) + JOIN FETCH
            classPage = classRoomRepository.findByStudentAndStatusWithDetails(user, classStatus, pageable);
        }

        // classList -> DTO 변환
        List<ClassListResponseDto> classListDtos = classPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 페이징 정보 생성
        PaginationResponseDto paginationDto = PaginationResponseDto.builder()
                .totalCount(classPage.getTotalElements())
                .currentPage(classPage.getNumber() + 1) // 0-based to 1-based
                .totalPages(classPage.getTotalPages())
                .pageSize(classPage.getSize())
                .hasNext(classPage.hasNext())
                .hasPrev(classPage.hasPrevious())
                .build();

        return ClassListWithPaginationDto.builder()
                .classes(classListDtos)
                .pagination(paginationDto)
                .build();
    }

    // ACTIVE, INACTIVE, ALL 구별
    private ClassStatus parseClassStatus(String status) {
        ClassStatusFilter statusFilter = ClassStatusFilter.fromString(status);
        
        switch (statusFilter) {
            case ACTIVE -> {
                return ClassStatus.ACTIVE;
            }
            case INACTIVE -> {
                return ClassStatus.INACTIVE;
            }
            case ALL -> {
                return null; // 모든 상태
            }
            default -> {
                return ClassStatus.ACTIVE; // 기본값
            }
        }
    }

    private ClassListResponseDto convertToDto(ClassRoom classRoom) {
        return ClassListResponseDto.builder()
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .description(classRoom.getDescription())
                .location(getSelectedLocation(classRoom))
                .activityDate(classRoom.getActivityDate())
                .studentCount(getStudentCount(classRoom))
                .maxStudents(classRoom.getMaxStudents())
                .teamCount(getTeamCount(classRoom))
                .status(classRoom.getStatus().name().toLowerCase())
                .inviteCode(classRoom.getInviteCode())
                .createdAt(classRoom.getCreatedAt())
                .build();
    }

    // ==================== JOIN FETCH 최적화된 메소드들 ====================
    
    private Integer getStudentCount(ClassRoom classRoom) {
        // JOIN FETCH로 이미 로드된 applications를 활용 (추가 쿼리 없음)
        return (int) classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .count();
    }

    private Integer getTeamCount(ClassRoom classRoom) {
        // JOIN FETCH로 이미 로드된 teams를 활용 (추가 쿼리 없음)
        return classRoom.getTeams().size();
    }

    private String getSelectedLocation(ClassRoom classRoom) {
        // JOIN FETCH로 이미 로드된 classLocations를 활용 (추가 쿼리 없음)
        return classRoom.getClassLocations().stream()
                .findFirst()
                .map(classLocation -> classLocation.getLocation().getName())
                .orElse("미정");
    }
}
