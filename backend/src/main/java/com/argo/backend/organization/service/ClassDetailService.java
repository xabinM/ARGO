package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.classdetail.*;
import com.argo.backend.organization.repository.classroomcreate.ClassRoomRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.service.exception.AccessDeniedException;
import com.argo.backend.organization.service.exception.ClassNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassDetailService {

    private final ClassRoomRepository classRoomRepository;
    private final UserRepository userRepository;

    public ClassDetailResponse getClassDetail(Long classId, Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 반 조회 - applications 데이터 로드 (MultipleBagFetchException 방지)
        ClassRoom classRoom = classRoomRepository.findByIdWithApplications(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        
        // teams 데이터 별도 로드
        ClassRoom classRoomWithTeams = classRoomRepository.findByIdWithTeams(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        classRoom.setTeams(classRoomWithTeams.getTeams());
        
        // locations 데이터 별도 로드
        ClassRoom classRoomWithLocations = classRoomRepository.findByIdWithLocations(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        classRoom.setClassLocations(classRoomWithLocations.getClassLocations());

        // 권한 검증
        validateAccess(classRoom, user);

        // 역할에 따른 응답 생성
        if (user.getRole() == Role.TEACHER) {
            return createTeacherResponse(classRoom);
        } else {
            return createStudentResponse(classRoom, user);
        }
    }

    private void validateAccess(ClassRoom classRoom, User user) {
        if (user.getRole() == Role.TEACHER) {
            // 선생님: 본인이 생성한 반만 조회 가능
            if (!classRoom.getTeacher().getUserId().equals(user.getUserId())) {
                throw new AccessDeniedException("해당 반에 접근할 권한이 없습니다.");
            }
        } else {
            // 학생: 참여 중인 반만 조회 가능
            boolean isParticipant = classRoom.getApplications().stream()
                    .anyMatch(app -> app.getUser().getUserId().equals(user.getUserId()) 
                            && app.getStatus() == ApplicationStatus.APPROVED);
            
            if (!isParticipant) {
                throw new AccessDeniedException("해당 반에 접근할 권한이 없습니다.");
            }
        }
    }

    private ClassDetailResponse createTeacherResponse(ClassRoom classRoom) {
        // 위치 정보 조회
        String location = getSelectedLocation(classRoom);
        
        // 기본 반 정보 (선생님용 - 전체 정보 포함)
        ClassInfo classInfo = ClassInfo.fromForTeacher(classRoom, location);

        // 모든 정보 로드
        List<StudentDto> students = getStudents(classRoom);
        List<TeamDto> teams = getTeams(classRoom);
        
        // 통계 정보 생성
        int totalStudents = (int) classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .count();
        int totalTeams = classRoom.getTeams().size();
        StatisticsDto statistics = StatisticsDto.from(totalStudents, totalTeams);

        return ClassDetailResponse.forTeacher(classInfo, students, teams, statistics);
    }

    private ClassDetailResponse createStudentResponse(ClassRoom classRoom, User student) {
        // 위치 정보 조회
        String location = getSelectedLocation(classRoom);
        
        // 기본 반 정보 (학생용 - 제한된 정보)
        ClassInfo classInfo = ClassInfo.fromForStudent(classRoom, location);

        // 학생의 팀 정보 조회
        TeamDto myTeam = getStudentTeam(classRoom, student);

        return ClassDetailResponse.forStudent(classInfo, myTeam);
    }

    private String getSelectedLocation(ClassRoom classRoom) {
        // 이미 JOIN FETCH로 location까지 로드됨 - N+1 문제 해결
        return classRoom.getClassLocations().stream()
                .findFirst()
                .map(classLocation -> classLocation.getLocation().getName())
                //  findByIdWithAllDetails()에서 JOIN FETCH로 미리 로딩됨
                .orElse("미정");
    }

    private List<StudentDto> getStudents(ClassRoom classRoom) {
        // 이미 JOIN FETCH로 로드된 데이터 활용 - N+1 문제 해결
        return classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(app -> {
                    User student = app.getUser(); // 이미 JOIN FETCH로 로드됨
                    // 학생의 팀 정보 (이미 JOIN FETCH로 로드됨)
                    Team studentTeam = findStudentTeam(classRoom, student);
                    return StudentDto.from(student, studentTeam, app.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    private List<TeamDto> getTeams(ClassRoom classRoom) {
        // 이미 JOIN FETCH로 로드된 데이터 활용 - N+1 문제 해결
        return classRoom.getTeams().stream()
                .map(team -> {
                    // 팀 멤버들 조회 (User.team 관계를 통해)
                    List<User> teamMembers = getTeamMembers(classRoom, team);
                    // 팀 점수 계산 (현재는 임시로 랜덤 점수, 실제로는 점수 계산 로직 필요)
                    int totalScore = calculateTeamScore(team);
                    return TeamDto.from(team, teamMembers, totalScore);
                })
                .collect(Collectors.toList());
    }

    private TeamDto getStudentTeam(ClassRoom classRoom, User student) {
        Team studentTeam = findStudentTeam(classRoom, student);
        if (studentTeam == null) { // 팀이 없는 경우
            return null;
        }

        List<User> teamMembers = getTeamMembers(classRoom, studentTeam);
        int totalScore = calculateTeamScore(studentTeam);
        return TeamDto.from(studentTeam, teamMembers, totalScore);
    }

    private Team findStudentTeam(ClassRoom classRoom, User student) {
        // 학생의 팀 정보는 User 엔티티에 직접 저장됨
        return student.getTeam();
    }

    private List<User> getTeamMembers(ClassRoom classRoom, Team team) {
        // 해당 팀에 속한 승인된 학생들만 반환
        return classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(ClassApplication::getUser)
                .filter(user -> user.getTeam() != null && 
                              Objects.equals(user.getTeam().getTeamId(), team.getTeamId()))
                .collect(Collectors.toList());
    }

    private int calculateTeamScore(Team team) {
        // 팀 점수 계산 로직 (실제 프로젝트에 맞게 구현 필요)
        // 현재는 임시로 랜덤 점수 반환
        return new Random().nextInt(100) + 50; // 50-150 점 사이
    }
}