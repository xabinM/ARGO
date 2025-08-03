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
import com.argo.backend.organization.dto.classroomcreate.ClassCreateRequest;
import com.argo.backend.organization.dto.classroomcreate.ClassCreateResponse;
import com.argo.backend.organization.dto.classlist.ClassListResponse;
import com.argo.backend.organization.dto.classlist.ClassInfoDto;
import com.argo.backend.organization.dto.classlist.PaginationDto;
import com.argo.backend.organization.exception.LocationNotFoundException;
import com.argo.backend.organization.exception.InsufficientPermissionException;
import com.argo.backend.organization.exception.InvalidInviteCodeException;
import com.argo.backend.organization.exception.StudentOnlyException;
import com.argo.backend.organization.exception.UserNotFoundException;
import com.argo.backend.organization.exception.ClassNotAvailableException;
import com.argo.backend.organization.exception.DuplicateApplicationException;
import com.argo.backend.organization.exception.ClassNotFoundException;
import com.argo.backend.organization.exception.UnauthorizedClassAccessException;
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
}
