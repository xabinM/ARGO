package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.randomassign.RandomAssignRequest;
import com.argo.backend.organization.dto.randomassign.RandomAssignResponse;
import com.argo.backend.organization.dto.randomassign.TeamAssignmentResult;
import com.argo.backend.organization.repository.classroomcreate.ClassRoomRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.service.exception.AccessDeniedException;
import com.argo.backend.organization.service.exception.ClassNotFoundException;
import com.argo.backend.organization.service.exception.TeacherNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RandomAssignService {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;

    public RandomAssignResponse randomAssignStudents(Long classId, Long userId, RandomAssignRequest request) {
        // 1. 사용자 조회 및 권한 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (user.getRole() != Role.TEACHER) {
            throw new TeacherNotAllowedException("선생님만 학생을 배정할 수 있습니다.");
        }

        // 2. 반 조회 - applications와 teams 데이터 로드 (MultipleBagFetchException 방지)
        ClassRoom classRoom = classRoomRepository.findByIdWithApplications(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        
        // teams 데이터 별도 로드
        ClassRoom classRoomWithTeams = classRoomRepository.findByIdWithTeams(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));
        classRoom.setTeams(classRoomWithTeams.getTeams());

        // 선생이 반 만들었는지 검증
        if (!classRoom.getTeacher().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("해당 반에서 학생을 배정할 권한이 없습니다.");
        }

        // 3. 팀이 없으면 오류
        if (classRoom.getTeams().isEmpty()) {
            throw new IllegalArgumentException("배정할 팀이 존재하지 않습니다.");
        }

        // 4. 미배정 학생들 조회
        List<User> unassignedStudents = classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(ClassApplication::getUser)
                .filter(student -> student.getTeam() == null)
                .collect(Collectors.toList());

        // 배정이 모두 완료되어 있을 때 처리
        if (unassignedStudents.isEmpty()) {
            return createEmptyResponse(classRoom, request);
        }

        // 5. 배정 가능한 팀들 확인
        List<Team> availableTeams = getAvailableTeams(classRoom);
        if (availableTeams.isEmpty()) {
            throw new IllegalArgumentException("모든 팀이 가득 차서 배정할 수 없습니다.");
        }

        // 6. 균등 배정 수행
        LocalDateTime assignedAt = LocalDateTime.now();
        Map<Team, List<User>> assignments = performBalancedAssignment(unassignedStudents, availableTeams);

        // 7. 실제 배정 처리
        for (Map.Entry<Team, List<User>> entry : assignments.entrySet()) {
            Team team = entry.getKey();
            List<User> studentsToAssign = entry.getValue();
            for (User student : studentsToAssign) {
                student.setTeam(team);
                userRepository.save(student);
            }
        }

        // 8. 응답 생성
        return createAssignResponse(classRoom, request, assignments, assignedAt);
    }

    private RandomAssignResponse createEmptyResponse(ClassRoom classRoom, RandomAssignRequest request) {
        // 이미 배정된 학생들의 정보를 조회
        List<User> allStudents = classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(ClassApplication::getUser)
                .collect(Collectors.toList());

        List<TeamAssignmentResult> existingAssignments = classRoom.getTeams().stream()
                // 인원이 존재하는 팀 필터링
                .filter(team -> allStudents.stream().anyMatch(student ->
                    student.getTeam() != null && student.getTeam().getTeamId().equals(team.getTeamId()))) // anyMatch(A) : A가 하나라도 true면 true반환
                //
                .map(team -> {
                    List<User> teamMembers = allStudents.stream()
                            // 학생들이 team이 있고, 올바르게 배정되어있는 경우만 필터링
                            .filter(student -> student.getTeam() != null && 
                                             student.getTeam().getTeamId().equals(team.getTeamId()))
                            .collect(Collectors.toList());
                    return TeamAssignmentResult.of(team, teamMembers, allStudents);
                })
                .collect(Collectors.toList());

        // 배정된게 몇명인지
        int totalAssigned = existingAssignments.stream()
                .mapToInt(assignment -> assignment.getAssignedStudents().size())
                .sum();

        return RandomAssignResponse.of(
                classRoom.getClassId(),
                classRoom.getClassName(),
                "balanced",
                totalAssigned,
                LocalDateTime.now(),
                existingAssignments,  // 기존 배정 결과
                Collections.emptyList(),  // 남은 학생 없음
                "모든 학생이 이미 팀에 배정되어 있습니다."
        );
    }

    private List<Team> getAvailableTeams(ClassRoom classRoom) {
        return classRoom.getTeams().stream()
                .filter(team -> {
                    if (team.getMaxMembers() == null) return true; // 최대인원이 설정되지 않았을 때

                    // 특정 team에 현재 배정된 학생 수
                    long currentMembers = classRoom.getApplications().stream()
                            .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                            .map(ClassApplication::getUser)
                            .filter(student -> student.getTeam() != null && 
                                             student.getTeam().getTeamId().equals(team.getTeamId()))
                            .count();
                    
                    return currentMembers < team.getMaxMembers();
                })
                .collect(Collectors.toList());
    }

    private Map<Team, List<User>> performBalancedAssignment(List<User> unassignedStudents, List<Team> availableTeams) {
        Map<Team, List<User>> assignments = new HashMap<>();
        for (Team team : availableTeams) {
            assignments.put(team, new ArrayList<>());
        }

        Collections.shuffle(unassignedStudents);


        // 1. 각 팀별 배정 가능한 최대 인원 계산
        Map<Team, Integer> teamCapacity = new HashMap<>();
        for (Team team : availableTeams) {
            int currentMembers = getCurrentTeamSize(team);
            int maxCapacity = team.getMaxMembers() == null ?  //팀의 최대 허용 인원수가 null이다?
                unassignedStudents.size() : team.getMaxMembers();
            teamCapacity.put(team, Math.max(0, maxCapacity - currentMembers));
        }

        // 2. 균등 분배
        int teamIndex = 0;
        while (!unassignedStudents.isEmpty() && hasAvailableCapacity(teamCapacity)) { // 팀들 중 인워을 받을 수 있는 팀이 있다면 계속 진행
            Team team = availableTeams.get(teamIndex % availableTeams.size());

            if (teamCapacity.get(team) > 0) {
                // 배정
                User student = unassignedStudents.remove(0);
                assignments.get(team).add(student);
                teamCapacity.put(team, teamCapacity.get(team) - 1);
            }

            teamIndex++;
        }


        return assignments;
    }

    private int getCurrentTeamSize(Team team) {
        // 현재 팀에 배정된 실제 인원 수 계산 (DB에서)
        return (int) team.getUsers().size();
    }

    private boolean hasAvailableCapacity(Map<Team, Integer> teamCapacity) {
        return teamCapacity.values().stream().anyMatch(capacity -> capacity > 0);
    }

    private RandomAssignResponse createAssignResponse(ClassRoom classRoom, 
                                                     RandomAssignRequest request,
                                                     Map<Team, List<User>> assignments,
                                                     LocalDateTime assignedAt) {
        List<User> allStudents = classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(ClassApplication::getUser)
                .collect(Collectors.toList());

        List<TeamAssignmentResult> teamAssignments = assignments.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> TeamAssignmentResult.of(entry.getKey(), entry.getValue(), allStudents))
                .collect(Collectors.toList());

        int totalAssigned = assignments.values().stream()
                .mapToInt(List::size)
                .sum();

        return RandomAssignResponse.of(
                classRoom.getClassId(),
                classRoom.getClassName(),
                "balanced",
                totalAssigned,
                assignedAt,
                teamAssignments,
                Collections.emptyList(), // 간단화를 위해 남은 학생 처리 생략
                null
        );
    }
}