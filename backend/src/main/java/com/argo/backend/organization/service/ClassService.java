package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.classroom.ClassStatus;
import com.argo.backend.domain.location.Location;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.Teacher;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.classapply.ClassApplyResponse;
import com.argo.backend.organization.dto.classdetail.StatisticsDto;
import com.argo.backend.organization.dto.classroomcreate.ClassCreateRequest;
import com.argo.backend.organization.dto.classroomcreate.ClassCreateResponse;
import com.argo.backend.organization.dto.classlist.ClassListResponse;
import com.argo.backend.organization.dto.classlist.ClassInfoDto;
import com.argo.backend.organization.dto.classlist.PaginationDto;
import com.argo.backend.organization.dto.classdetail.*;
import com.argo.backend.domain.team.Team;
import com.argo.backend.organization.exception.LocationNotFoundException;
import com.argo.backend.organization.exception.InsufficientPermissionException;
import com.argo.backend.organization.exception.InvalidInviteCodeException;
import com.argo.backend.organization.exception.StudentOnlyException;
import com.argo.backend.organization.exception.UserNotFoundException;
import com.argo.backend.organization.exception.ClassNotAvailableException;
import com.argo.backend.organization.exception.DuplicateApplicationException;
import com.argo.backend.organization.exception.ClassNotFoundException;
import com.argo.backend.organization.exception.UnauthorizedClassAccessException;
import com.argo.backend.organization.exception.InvalidClassIdException;
import com.argo.backend.organization.dto.applicationlist.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.ClassApplicationRepository;
import com.argo.backend.organization.repository.ClassRoomRepository;
import com.argo.backend.organization.repository.LocationRepository;
import com.argo.backend.organization.repository.TeacherRepository;
import com.argo.backend.organization.repository.TeamRepository;
import com.argo.backend.organization.repository.ClassStudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

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


    private static final String INVITE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;

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

        ClassRoom classRoom = classRoomRepository.findByInviteCode(inviteCode)
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


    // 반 신청 가능 여부 검증
    public boolean isAvailableForApplication(ClassRoom classRoom) {
        return classRoom.getStatus() == ClassStatus.ACTIVE
                && classRoom.getActivityDate() != null
                && !classRoom.getActivityDate().isBefore(LocalDate.now());
    }
    
    // 선생님의 반 목록 조회
    public ClassListResponse getTeacherClassList(Long teacherId, String status, Pageable pageable) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(InsufficientPermissionException::new);
        
        Page<ClassRoom> classPage;
        
        if ("all".equals(status)) {
            classPage = classRoomRepository.findByTeacher(teacher, pageable);
        } else {
            ClassStatus classStatus = parseClassStatus(status);
            classPage = classRoomRepository.findByTeacherAndStatus(teacher, classStatus, pageable);
        }
        
        List<ClassInfoDto> classInfoList = classPage.getContent().stream()
                .map(classRoom -> {
                    int studentCount = getApprovedStudentCount(classRoom.getClassId());
                    int teamCount = getTeamCount(classRoom.getClassId());
                    return ClassInfoDto.from(classRoom, studentCount, teamCount);
                })
                .toList();
        
        PaginationDto pagination = PaginationDto.from(classPage);
        
        return ClassListResponse.success(classInfoList, pagination);
    }
    
    // 학생의 반 목록 조회
    public ClassListResponse getStudentClassList(Long studentId, String status, Pageable pageable) {
        User student = userRepository.findById(studentId)
                .orElseThrow(UserNotFoundException::new);
        
        if (student.getRole() != Role.ROLE_STUDENT) {
            throw new StudentOnlyException();
        }
        
        Page<ClassRoom> classPage;
        
        if ("all".equals(status)) {
            classPage = classRoomRepository.findStudentClasses(studentId, pageable);
        } else {
            ClassStatus classStatus = parseClassStatus(status);
            classPage = classRoomRepository.findStudentClassesByStatus(studentId, classStatus, pageable);
        }
        
        List<ClassInfoDto> classInfoList = classPage.getContent().stream()
                .map(classRoom -> {
                    int studentCount = getApprovedStudentCount(classRoom.getClassId());
                    int teamCount = getTeamCount(classRoom.getClassId());
                    return ClassInfoDto.fromStudent(classRoom, studentCount, teamCount);
                })
                .toList();
        
        PaginationDto pagination = PaginationDto.from(classPage);
        
        return ClassListResponse.success(classInfoList, pagination);
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
        return teamRepository.findByClassRoomOrderByCreatedAtAsc(classRoom).size();
    }
    
    // 선생님의 반 상세정보 조회
    public ClassDetailResponse getTeacherClassDetail(Long teacherId, Long classId, String include) {
        validateClassId(classId);
        
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(ClassNotFoundException::new);
        
        // 선생님 권한 검증
        if (!classRoom.getTeacher().getUserId().equals(teacherId)) {
            throw new UnauthorizedClassAccessException();
        }
        
        ClassInfoDetailDto classInfo = ClassInfoDetailDto.fromTeacher(classRoom);
        
        // include 파라미터에 따라 선택적으로 정보 포함
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
        
        return ClassDetailResponse.forTeacher(classInfo, students, teams, statistics);
    }
    
    // 학생의 반 상세정보 조회
    public ClassDetailResponse getStudentClassDetail(Long studentId, Long classId) {
        validateClassId(classId);
        
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(ClassNotFoundException::new);
        
        // 학생 권한 검증 (해당 반에 참여하고 있는지)
        if (!classStudentRepository.isStudentInClass(studentId, classId)) {
            throw new UnauthorizedClassAccessException();
        }
        
        ClassInfoDetailDto classInfo = ClassInfoDetailDto.fromStudent(classRoom);
        TeamDetailDto myTeam = getStudentTeam(studentId, classId);
        
        return ClassDetailResponse.forStudent(classInfo, myTeam);
    }


    private void validateClassId(Long classId) {
        if (classId == null || classId <= 0) {
            throw new InvalidClassIdException();
        }
    }

    // 선생 전용
    private List<StudentDto> getStudentsForClass(Long classId) {
        List<Object[]> results = classStudentRepository.findApprovedStudentsWithTeamAndJoinDateByClassId(classId);
        return results.stream()
                .map(result -> {
                    User student = (User) result[0];
                    java.time.LocalDateTime joinedAt = (java.time.LocalDateTime) result[1];
                    String teamName = student.getTeam() != null ? student.getTeam().getTeamName() : null;
                    return StudentDto.from(student, teamName, joinedAt);
                })
                .toList();
    }

    // 선생 전용
    private List<TeamDetailDto> getTeamsForClass(Long classId) {
        List<Team> teams = teamRepository.findTeamsByClassId(classId);
        return teams.stream()
                .map(team -> {
                    List<User> members = userRepository.findByTeamId(team.getTeamId());
                    List<TeamMemberDto> memberDtos = members.stream()
                            .map(TeamMemberDto::from)
                            .toList();
                    return TeamDetailDto.from(team, memberDtos);
                })
                .toList();
    }
    
    // 학생(자기자신) 팀 조회
    private TeamDetailDto getStudentTeam(Long studentId, Long classId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(UserNotFoundException::new);
        
        if (student.getTeam() == null) {
            return null; // 팀에 배정되지 않은 경우
        }
        
        Team team = student.getTeam();
        List<User> members = userRepository.findByTeamId(team.getTeamId());
        List<TeamMemberDto> memberDtos = members.stream()
                .map(TeamMemberDto::from)
                .toList();
        
        return TeamDetailDto.from(team, memberDtos);
    }
}
