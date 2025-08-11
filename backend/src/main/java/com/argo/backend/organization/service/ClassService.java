package com.argo.backend.organization.service;

import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.spot.repository.SpotRepository;
import com.argo.backend.organization.dto.spot.SpotsResponse;
import com.argo.backend.domain.classroom.entity.ClassApplication;
import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.classroom.enums.ClassStatus;
import com.argo.backend.domain.location.entity.Location;
import com.argo.backend.domain.classroom.enums.ApplicationStatus;
import com.argo.backend.domain.user.enums.Role;
import com.argo.backend.domain.user.entity.Teacher;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.organization.dto.classapply.ClassApplyResponse;
import com.argo.backend.organization.dto.classleave.ClassLeaveResponse;
import com.argo.backend.organization.dto.classleave.LeftClassDto;
import com.argo.backend.organization.dto.classleave.StudentInfoDto;
import com.argo.backend.organization.dto.classleave.TeamInfoDto;
import com.argo.backend.organization.dto.classleave.ClassStatusDto;
import com.argo.backend.organization.dto.classdelete.ClassDeleteResponse;
import com.argo.backend.organization.dto.classdelete.DeletedClassDto;
import com.argo.backend.organization.dto.classdelete.DeletedDataDto;
import com.argo.backend.organization.dto.classdelete.DeletedStudentsDto;
import com.argo.backend.organization.dto.classdelete.DeletedTeamsDto;
import com.argo.backend.organization.dto.classdelete.DeletedApplicationsDto;
import com.argo.backend.organization.dto.classdetail.StatisticsDto;
import com.argo.backend.organization.dto.classroomcreate.ClassCreateRequest;
import com.argo.backend.organization.dto.classroomcreate.ClassCreateResponse;
import com.argo.backend.organization.dto.classlist.ClassListResponse;
import com.argo.backend.organization.dto.classlist.ClassInfoDto;
import com.argo.backend.organization.dto.classlist.PaginationDto;
import com.argo.backend.organization.dto.classdetail.*;
import com.argo.backend.organization.dto.location.LocationsResponse;
import com.argo.backend.organization.dto.studentlist.*;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.user.entity.UserTeam;
import com.argo.backend.organization.exception.types.ClassNotFoundException;
import com.argo.backend.organization.exception.types.UnauthorizedClassAccessException;
import com.argo.backend.organization.exception.types.InvalidClassIdException;
import com.argo.backend.organization.exception.types.InsufficientPermissionException;
import com.argo.backend.organization.exception.types.LocationNotFoundException;
import com.argo.backend.organization.exception.types.InvalidInviteCodeException;
import com.argo.backend.organization.exception.types.ClassNotAvailableException;
import com.argo.backend.organization.exception.types.DuplicateApplicationException;
import com.argo.backend.organization.exception.types.StudentOnlyException;
import com.argo.backend.organization.exception.types.UserNotFoundException;
import com.argo.backend.organization.exception.types.NotParticipatingClassException;
import com.argo.backend.organization.exception.types.ActivityInProgressException;
import com.argo.backend.organization.exception.types.CannotDeleteActiveClassException;
import com.argo.backend.organization.exception.types.InvalidStatusParameterException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.domain.classroom.repository.ClassApplicationRepository;
import com.argo.backend.domain.classroom.repository.ClassRoomRepository;
import com.argo.backend.domain.location.repository.LocationRepository;
import com.argo.backend.domain.user.repository.TeacherRepository;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.classroom.repository.ClassStudentRepository;
import com.argo.backend.domain.user.repository.UserTeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRoomRepository classRoomRepository;
    private final LocationRepository locationRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final ClassApplicationRepository classApplicationRepository;
    private final TeamRepository teamRepository;
    private final ClassStudentRepository classStudentRepository;
    private final UserTeamRepository userTeamRepository;
    private final SpotRepository spotRepository;

    private static final String INVITE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;


    @Transactional
    public List<LocationsResponse> getLocations() {
        return locationRepository.findAll().stream()
                .map(LocationsResponse::from)
                .collect(Collectors.toList());
    }


    @Transactional
    public List<SpotsResponse> getSpots(Long classId, Long teacherId){
        // 권한 검증
        ClassRoom classRoom = validateClassAccess(teacherId, classId);
                
        Location location = classRoom.getLocation();
        if (location == null) {
            throw new LocationNotFoundException();
        }
        
        Long locationId = location.getLocationId();
        List<Spot> spots = spotRepository.findByLocationLocationId(locationId);
        
        return spots.stream()
                .map(SpotsResponse::from)
                .collect(Collectors.toList());
    }


    @Transactional
    public ClassCreateResponse createClass(Long teacherId, ClassCreateRequest request){

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(InsufficientPermissionException::new);

        Location location = locationRepository.findByName(request.getLocation())
                .orElseThrow(LocationNotFoundException::new);

        String inviteCode = generateUniqueInviteCode();


        ClassRoom classRoom = ClassRoom.from(
                teacher,
                request.getClassName(),
                request.getDescription(),
                request.getActivityDate(),
                inviteCode,
                request.getMaxStudents(),
                location,
                request.getGrade()
        );


        ClassRoom saved = classRoomRepository.save(classRoom);
        teacher.addClassRoom(saved);


        return new ClassCreateResponse(
                saved.getClassId(),
                saved.getClassName(),
                saved.getInviteCode(),
                saved.getTeacher().getUserId(),
                saved.getCreatedAt()
        );
    }


    @Transactional
    public ClassApplyResponse applyToClass(Long studentId, String inviteCode) {

        if (!isValidInviteCode(inviteCode)) {
            throw new InvalidInviteCodeException();
        }

        User user = userRepository.findById(studentId)
                .orElseThrow(UserNotFoundException::new);

        validateUserRole(user);

        ClassRoom classRoom = classRoomRepository.findByInviteCodeWithLocationAndTeacher(inviteCode)
                .orElseThrow(InvalidInviteCodeException::new);

        if (!isAvailableForApplication(classRoom)) {
            throw new ClassNotAvailableException();
        }

        if (classApplicationRepository.existsByUserAndClassRoom(user, classRoom)) {
            throw new DuplicateApplicationException();
        }


        ClassApplication application = ClassApplication.from(user, classRoom);
        ClassApplication savedApplication = classApplicationRepository.save(application);


        return new ClassApplyResponse(
                savedApplication.getApplicationId(),
                classRoom.getClassId(),
                classRoom.getClassName(),
                classRoom.getDescription(),
                classRoom.getLocation().getName(),
                classRoom.getActivityDate(),
                classRoom.getTeacher().getName(),
                savedApplication.getStatus().name(),
                savedApplication.getCreatedAt()
        );
    }


    @Transactional
    public ClassListResponse getTeacherClassList(Long teacherId, String status, Pageable pageable) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(InsufficientPermissionException::new);
        
        Page<Object[]> classRoomsWithCounts;
        
        if ("all".equals(status)) {
            classRoomsWithCounts = classRoomRepository.findClassRoomsWithCounts(teacher, pageable);
        } else {
            ClassStatus classStatus = parseClassStatus(status);
            classRoomsWithCounts = classRoomRepository.findClassRoomsWithCountsByStatus(teacher, classStatus, pageable);
        }
        
        List<ClassInfoDto> classInfoList = classRoomsWithCounts.getContent().stream()
                .map(result -> {
                    ClassRoom classRoom = (ClassRoom) result[0];
                    int studentCount = ((Number) result[1]).intValue();
                    int teamCount = ((Number) result[2]).intValue();
                    return ClassInfoDto.from(classRoom, studentCount, teamCount);
                })
                .toList();
        
        PaginationDto pagination = PaginationDto.from(classRoomsWithCounts);
        
        return ClassListResponse.success(classInfoList, pagination);
    }


    @Transactional
    public ClassListResponse getStudentClassList(Long studentId, String status, Pageable pageable) {
        Page<Object[]> classRoomsWithCounts;
        
        if ("all".equals(status)) {
            classRoomsWithCounts = classRoomRepository.findStudentClassesWithCounts(studentId, pageable);
        } else {
            ClassStatus classStatus = parseClassStatus(status);
            classRoomsWithCounts = classRoomRepository.findStudentClassesWithCountsByStatus(studentId, classStatus, pageable);
        }
        
        List<ClassInfoDto> classInfoList = classRoomsWithCounts.getContent().stream()
                .map(result -> {
                    ClassRoom classRoom = (ClassRoom) result[0];
                    int studentCount = ((Number) result[1]).intValue();
                    int teamCount = ((Number) result[2]).intValue();
                    return ClassInfoDto.fromStudent(classRoom, studentCount, teamCount);
                })
                .toList();
        
        PaginationDto pagination = PaginationDto.from(classRoomsWithCounts);
        
        return ClassListResponse.success(classInfoList, pagination);
    }


    @Transactional
    public ClassDetailResponse getClassDetail(Long userId, Long classId, String include) {
        validateClassId(classId);
        
        ClassRoom classRoom = classRoomRepository.findByIdWithLocationAndTeacher(classId)
                .orElseThrow(ClassNotFoundException::new);
        
        // 권한 검증 (선생 또는 참여 학생)
        boolean isTeacher = classRoom.getTeacher().getUserId().equals(userId);
        boolean isStudent = classStudentRepository.isStudentInClass(userId, classId);
        
        if (!isTeacher && !isStudent) {
            throw new UnauthorizedClassAccessException();
        }
        
        // Teacher 버전 기준으로 통일 (모든 정보 제공)
        ClassInfoDetailDto classInfo = ClassInfoDetailDto.fromTeacher(classRoom);
        
        // include 파라미터에 따라 선택적으로 정보 포함 (기본: 모든 정보)
        List<StudentDto> students = null;
        List<TeamDetailDto> teams = null;
        
        if (include == null || include.contains("students")) {
            students = getStudentsForClass(classId);
        }
        
        if (include == null || include.contains("teams")) {
            teams = getTeamsForClass(classId);
        }
        
        StatisticsDto statistics = new StatisticsDto(
                students != null ? students.size() : getApprovedStudentCount(classId),
                teams != null ? teams.size() : getTeamCount(classId)
        );
        
        return ClassDetailResponse.from(classInfo, students, teams, statistics);
    }


    @Transactional
    public StudentListResponse getClassStudents(Long teacherId, Long classId, String status, Pageable pageable) {
        ClassRoom classRoom = validateClassAccess(teacherId, classId);
        StatusType statusType = parseStatusFilter(status);
        
        Page<Object[]> studentPage = getStudentsByStatus(classId, statusType, pageable);
        
        List<StudentDetailDto> students = studentPage.getContent().stream()
                .map(this::mapToStudentDetailDto)
                .toList();
        
        int totalStudents = getApprovedStudentCount(classId);
        StudentClassInfoDto classInfo = StudentClassInfoDto.from(classRoom, totalStudents);
        TeamSummaryDto teamSummary = buildTeamSummary(classId, totalStudents);
        StudentListPaginationDto pagination = StudentListPaginationDto.from(studentPage);
        
        return StudentListResponse.of(classInfo, students, teamSummary, pagination);
    }


    @Transactional
    public ClassLeaveResponse leaveClass(Long studentId, Long classId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(UserNotFoundException::new);

        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(ClassNotFoundException::new);
        
        ClassApplication application = findStudentApplication(studentId, classId);
        
        validateActivityStatus(classRoom);
        
        java.time.LocalDateTime joinedAt = application.getUpdatedAt();
        java.time.LocalDateTime leftAt = java.time.LocalDateTime.now();
        
        // N+1 문제 해결: Repository 메서드로 대체 (FETCH JOIN 사용)
        Optional<UserTeam> userTeamOpt = userTeamRepository.findActiveByUserIdAndClassId(studentId, classId);
        Team currentTeam = userTeamOpt.map(UserTeam::getTeam).orElse(null);
        TeamInfoDto teamInfo = currentTeam != null 
            ? TeamInfoDto.fromTeam(currentTeam, leftAt)
            : TeamInfoDto.noTeam();
        
        // UserTeam 기반으로 팀 탈퇴 처리
        if (currentTeam != null) {
            userTeamRepository.findByUserIdAndTeamId(studentId, currentTeam.getTeamId())
                .ifPresent(UserTeam::deactivate);
        }
        
        classApplicationRepository.delete(application);
        
        ClassStatusDto classStatus = buildClassStatus(classRoom);
        
        return ClassLeaveResponse.of(
            LeftClassDto.from(classRoom),
            StudentInfoDto.from(student, joinedAt, leftAt),
            teamInfo,
            classStatus
        );
    }


    @Transactional
    public ClassDeleteResponse deleteClass(Long teacherId, Long classId) {
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(ClassNotFoundException::new);
                
        if (!classRoom.getTeacher().getUserId().equals(teacherId)) {
            throw new UnauthorizedClassAccessException();
        }
        
        validateDeletionRules(classRoom);
        
        java.time.LocalDateTime deletedAt = java.time.LocalDateTime.now();
        
        Object[] applicationStats = classApplicationRepository.findStatisticsByClassId(classId);
        List<User> approvedStudents = classStudentRepository.findApprovedStudentsWithTeamAndJoinDateByClassId(classId)
                .stream()
                .map(result -> (User) result[0])
                .toList();
        List<Team> teams = teamRepository.findTeamsByClassId(classId);
        
        // N+1 문제 해결: 배치 쿼리로 모든 팀의 멤버 수 한 번에 조회
        List<Long> teamIds = teams.stream()
                .map(Team::getTeamId)
                .toList();
        List<Object[]> memberCountResults = classStudentRepository.findTeamMemberCounts(teamIds);
        Map<Long, Integer> memberCounts = memberCountResults.stream()
                .collect(Collectors.toMap(
                    result -> (Long) result[0],
                    result -> ((Number) result[1]).intValue()
                ));
        
        // UserTeam 기반으로 모든 학생의 팀 배정 해제
        List<Long> studentIds = approvedStudents.stream()
                .map(User::getUserId)
                .toList();
        
        for (Long studentId : studentIds) {
            userTeamRepository.findActiveByUserIdAndClassId(studentId, classId)
                    .ifPresent(UserTeam::deactivate);
        }
        
        teamRepository.deleteAll(teams);
        classApplicationRepository.deleteAll(classRoom.getApplications());
        classRoomRepository.delete(classRoom);
        
        DeletedClassDto deletedClass = DeletedClassDto.from(classRoom, deletedAt);
        DeletedDataDto deletedData = DeletedDataDto.of(
            DeletedStudentsDto.of(approvedStudents),
            DeletedTeamsDto.of(teams, memberCounts),
            DeletedApplicationsDto.of(applicationStats)
        );
        
        return ClassDeleteResponse.of(deletedClass, deletedData);
    }



    private boolean isValidInviteCode(String inviteCode) {
        return inviteCode != null && !inviteCode.trim().isEmpty();
    }

    private void validateUserRole(User user) {
        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new StudentOnlyException();
        }
    }

    private String generateUniqueInviteCode() {
        SecureRandom random = new SecureRandom();
        String code;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                int index = random.nextInt(INVITE_CHARACTERS.length());
                sb.append(INVITE_CHARACTERS.charAt(index));
            }
            code = sb.toString();
        } while (classRoomRepository.existsByInviteCode(code));

        return code;
    }

    public boolean isAvailableForApplication(ClassRoom classRoom) {
        return classRoom.getStatus() == ClassStatus.ACTIVE
                && classRoom.getActivityDate() != null
                && !classRoom.getActivityDate().isBefore(LocalDate.now());
    }

    private ClassStatus parseClassStatus(String status) {
        return switch (status.toLowerCase()) {
            case "active" -> ClassStatus.ACTIVE;
            case "inactive" -> ClassStatus.INACTIVE;
            default -> ClassStatus.ACTIVE;
        };
    }

    private int getApprovedStudentCount(Long classId) {
        return (int) classApplicationRepository.countByClassRoomClassIdAndStatus(
                classId, ApplicationStatus.APPROVED);
    }

    private int getTeamCount(Long classId) {
        ClassRoom classRoom = classRoomRepository.findById(classId).orElse(null);
        if (classRoom == null) return 0;
        return (int) teamRepository.countByClassRoom(classRoom); // COUNT 쿼리로 최적화
    }

    private void validateClassId(Long classId) {
        if (classId == null || classId <= 0) {
            throw new InvalidClassIdException();
        }
    }


    private List<StudentDto> getStudentsForClass(Long classId) {
        List<Object[]> results = classStudentRepository.findApprovedStudentsWithTeamAndJoinDateByClassId(classId);
        return results.stream()
                .map(result -> {
                    User student = (User) result[0];
                    java.time.LocalDateTime joinedAt = (java.time.LocalDateTime) result[1];
                    Team team = (Team) result[2]; // UserTeam 기반으로 수정된 쿼리에서 team 정보 가져옴
                    Long teamId = team != null ? team.getTeamId() : null;
                    String teamName = team != null ? team.getTeamName() : null;
                    return StudentDto.from(student, teamId, teamName, joinedAt);
                })
                .toList();
    }

    // 선생 전용
    private List<TeamDetailDto> getTeamsForClass(Long classId) {
        List<Team> teams = teamRepository.findTeamsByClassIdWithActiveMembersAndLeader(classId);
        return teams.stream()
                .map(team -> {
                    List<User> members = team.getActiveMembers(); // 이미 FETCH JOIN으로 로딩됨 (쿼리 없음)
                    List<TeamMemberDto> memberDtos = members.stream()
                            .map(TeamMemberDto::from)
                            .toList();
                    return TeamDetailDto.from(team, memberDtos); // leader도 이미 로딩됨 (쿼리 없음)
                })
                .toList();
    }

    // N+1 문제 해결: Teacher를 FETCH JOIN으로 한 번에 로딩하여 추가 쿼리 방지
    private ClassRoom validateClassAccess(Long teacherId, Long classId) {
        validateClassId(classId);
        ClassRoom classRoom = classRoomRepository.findByIdWithTeacher(classId)
                .orElseThrow(ClassNotFoundException::new);
        if (!classRoom.getTeacher().getUserId().equals(teacherId)) {
            throw new UnauthorizedClassAccessException();
        }
        return classRoom;
    }

    private StudentDetailDto mapToStudentDetailDto(Object[] result) {
        User student = (User) result[0];
        java.time.LocalDateTime joinedAt = (java.time.LocalDateTime) result[1];
        Team team = (Team) result[2]; // UserTeam 기반으로 수정된 쿼리에서 team 정보 가져옴
        com.argo.backend.organization.dto.studentlist.TeamInfoDto teamInfo = team != null ?
                com.argo.backend.organization.dto.studentlist.TeamInfoDto.from(team, null) : null;
        return StudentDetailDto.from(student, joinedAt, teamInfo);
    }

    // string -> statusType
    private StatusType parseStatusFilter(String status) {
        if (status == null || "all".equals(status)) {
            return StatusType.ALL;
        } else if ("assigned".equals(status)) {
            return StatusType.ASSIGNED;
        } else if ("unassigned".equals(status)) {
            return StatusType.UNASSIGNED;
        } else {
            throw new InvalidStatusParameterException();
        }
    }

    private Page<Object[]> getStudentsByStatus(Long classId, StatusType statusType, Pageable pageable) {
        return switch (statusType) {
            case ALL -> classStudentRepository.findApprovedStudentsWithTeamAndJoinDateByClassIdPaged(classId, pageable);
            case ASSIGNED -> classStudentRepository.findAssignedStudentsWithTeamAndJoinDateByClassIdPaged(classId, pageable);
            case UNASSIGNED -> classStudentRepository.findUnassignedStudentsWithTeamAndJoinDateByClassIdPaged(classId, pageable);
        };
    }

    private TeamSummaryDto buildTeamSummary(Long classId, int totalStudents) {
        List<Team> teams = teamRepository.findTeamsByClassId(classId);

        // N+1 문제 해결: 모든 팀의 멤버 수를 한 번에 조회
        List<Long> teamIds = teams.stream()
                .map(Team::getTeamId)
                .toList();

        // 배치 쿼리로 모든 팀의 멤버 수 조회
        List<Object[]> memberCounts = classStudentRepository.findTeamMemberCounts(teamIds);
        Map<Long, Integer> teamMemberCountMap = memberCounts.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> ((Number) result[1]).intValue()
                ));

        List<TeamStatusDto> teamStatuses = teams.stream()
                .map(team -> TeamStatusDto.from(team, teamMemberCountMap.getOrDefault(team.getTeamId(), 0)))
                .toList();

        int totalAssigned = teamStatuses.stream()
                .mapToInt(TeamStatusDto::getCurrentMembers)
                .sum();

        return TeamSummaryDto.of(teams.size(), totalAssigned, totalStudents - totalAssigned, teamStatuses);
    }

    private ClassApplication findStudentApplication(Long studentId, Long classId) {
        ClassApplication application = classApplicationRepository.findApprovedApplicationByStudentAndClass(studentId, classId);

        if (application == null) {
            throw new NotParticipatingClassException();
        }

        return application;
    }

    private void validateActivityStatus(ClassRoom classRoom) {
        LocalDate today = LocalDate.now();
        if (classRoom.getActivityDate().equals(today)) {
            throw new ActivityInProgressException();
        }
    }

    private ClassStatusDto buildClassStatus(ClassRoom classRoom) {
        long approvedCount = classApplicationRepository.countByClassRoomClassIdAndStatus(classRoom.getClassId(), ApplicationStatus.APPROVED);

        return ClassStatusDto.of((int) approvedCount, classRoom.getMaxStudents());
    }

    private void validateDeletionRules(ClassRoom classRoom) {
        LocalDate today = LocalDate.now();
        if (classRoom.getActivityDate().equals(today)) {
            throw new CannotDeleteActiveClassException();
        }
    }

    private enum StatusType {
        ALL, ASSIGNED, UNASSIGNED
    }

}
