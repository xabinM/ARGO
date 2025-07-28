package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.classdelete.*;
import com.argo.backend.organization.dto.classdetail.StudentDto;
import com.argo.backend.organization.dto.classdetail.TeamDto;
import com.argo.backend.organization.repository.classroomcreate.ClassRoomRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.classroomlist.ClassApplicationRepository;
import com.argo.backend.organization.repository.classroomlist.TeamRepository;
import com.argo.backend.organization.service.exception.AccessDeniedException;
import com.argo.backend.organization.service.exception.ClassNotFoundException;
import com.argo.backend.organization.service.exception.TeacherNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClassDeleteService {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TeamRepository teamRepository;
    private final ClassApplicationRepository classApplicationRepository;

    public ClassDeleteResponse deleteClass(Long classId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (user.getRole() != Role.TEACHER) {
            throw new TeacherNotAllowedException("선생님만 반을 삭제할 수 있습니다.");
        }

        // 반 조회 - applications와 teams 데이터 로드 (MultipleBagFetchException 방지)
        ClassRoom classRoom = classRoomRepository.findByIdWithApplications(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        
        // teams 데이터 별도 로드
        ClassRoom classRoomWithTeams = classRoomRepository.findByIdWithTeams(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        classRoom.setTeams(classRoomWithTeams.getTeams());

        if (!classRoom.getTeacher().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("해당 반을 삭제할 권한이 없습니다.");
        }

        // 활동 진행 중 체크
        if (classRoom.getActivityDate() != null && !classRoom.getActivityDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("활동 진행 중인 반은 삭제할 수 없습니다.");
        }

        LocalDateTime deletedAt = LocalDateTime.now();
        
        // 학생들 팀에서 제거
        List<User> students = classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(ClassApplication::getUser)
                .collect(Collectors.toList());

        for (User student : students) {
            if (student.getTeam() != null) {
                student.setTeam(null);
                userRepository.save(student);
            }
        }

        // 삭제 응답 생성
        ClassDeleteResponse response = createDeleteResponse(classRoom, deletedAt);

        // 팀 삭제
        classRoom.getTeams().forEach(teamRepository::delete);
        
        // 신청 삭제
        classRoom.getApplications().forEach(classApplicationRepository::delete);
        
        // 반 삭제
        classRoomRepository.delete(classRoom);

        log.info("반 삭제 완료 - classId: {}, deletedStudents: {}, deletedTeams: {}", 
                classId, students.size(), classRoom.getTeams().size());

        return response;
    }

    private ClassDeleteResponse createDeleteResponse(ClassRoom classRoom, LocalDateTime deletedAt) {
        // 1. 삭제된 반 정보
        ClassDeletedInfo deletedClass = ClassDeletedInfo.from(classRoom, deletedAt);
        
        // 2. 삭제된 학생 정보
        List<StudentDto> deletedStudents = classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(ClassApplication::getUser)
                .map(user -> StudentDto.builder()
                        .studentId(user.getUserId())
                        .studentName(user.getName())
                        .studentName(user.getName())
                        .build())
                .collect(Collectors.toList());
        
        StudentDeletedData studentData = StudentDeletedData.of(deletedStudents);
        
        // 3. 삭제된 팀 정보
        List<TeamDto> deletedTeams = classRoom.getTeams().stream()
                .map(team -> TeamDto.builder()
                        .teamId(team.getTeamId())
                        .teamName(team.getTeamName())
                        .memberCount((int) team.getUsers().size())
                        .build())
                .collect(Collectors.toList());
        
        TeamDeletedData teamData = TeamDeletedData.of(deletedTeams);
        
        // 4. 삭제된 신청 정보
        ApplicationDeletedData applicationData = ApplicationDeletedData.from(classRoom.getApplications());
        
        // 5. 전체 삭제 데이터 구성
        DeletedData deletedData = DeletedData.of(studentData, teamData, applicationData);
        
        return ClassDeleteResponse.of(deletedClass, deletedData);
    }
}