package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.Role;
import com.argo.backend.domain.user.User;
import com.argo.backend.organization.dto.teamassign.AssignedStudent;
import com.argo.backend.organization.dto.teamassign.TeamAssignInfo;
import com.argo.backend.organization.dto.teamassign.TeamAssignRequest;
import com.argo.backend.organization.dto.teamassign.TeamAssignResponse;
import com.argo.backend.organization.repository.classroomcreate.ClassRoomRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.classroomlist.TeamRepository;
import com.argo.backend.organization.service.exception.AccessDeniedException;
import com.argo.backend.organization.service.exception.ClassNotFoundException;
import com.argo.backend.organization.service.exception.TeacherNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeamAssignService {

    private final UserRepository userRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TeamRepository teamRepository;

    public TeamAssignResponse assignStudentsToTeam(Long classId, Long teamId, Long userId, TeamAssignRequest request) {
        // 1. 입력값 유효성 검증
        validateRequest(request);

        // 2. 사용자 조회 및 권한 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        validateTeacherRole(user);

        // 3. 반 조회 - applications 데이터 로드 (MultipleBagFetchException 방지)
        ClassRoom classRoom = classRoomRepository.findByIdWithApplications(classId)
                .orElseThrow(() -> new ClassNotFoundException("존재하지 않는 반입니다."));

        // 4. 권한 검증 - 본인이 생성한 반인지 확인
        validateClassOwnership(classRoom, user);

        // 5. 팀 조회
        Team team = teamRepository.findByIdAndClassRoom(teamId, classRoom)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        // 6. 승인된 학생들만 필터링
        List<User> approvedStudents = classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(ClassApplication::getUser)
                .collect(Collectors.toList());

        // 7. 배정할 학생들 검증 (우리반이 맞는지, 소속된 팀이 없는지)
        List<User> studentsToAssign = validateAndGetStudentsToAssign(request.getStudentIds(), approvedStudents);

        // 8. 팀 최대 인원 검증 (각 팀에서 (이미 있는 애들 + 들어갈 애들) 보다 max가 작다면, 에러처리
        validateTeamCapacity(team, approvedStudents, studentsToAssign.size());

        // 9. 학생들을 팀에 배정 (
        LocalDateTime assignedAt = LocalDateTime.now();
        assignStudentsToTeam(studentsToAssign, team);

        // 10. 응답 생성
        return createTeamAssignResponse(team, classRoom, studentsToAssign, approvedStudents, assignedAt);
    }

    private void validateRequest(TeamAssignRequest request) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("학생 ID 배열을 입력해주세요.");
        }
    }

    private void validateTeacherRole(User user) {
        if (user.getRole() != Role.TEACHER) {
            throw new TeacherNotAllowedException("선생님만 학생을 배정할 수 있습니다.");
        }
    }

    private void validateClassOwnership(ClassRoom classRoom, User user) {
        if (!classRoom.getTeacher().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("해당 팀에 학생을 배정할 권한이 없습니다.");
        }
    }

    private List<User> validateAndGetStudentsToAssign(List<Long> studentIds, List<User> approvedStudents) {
        Map<Long, User> studentMap = approvedStudents.stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        for (Long studentId : studentIds) {
            User student = studentMap.get(studentId);
            if (student == null) {
                throw new IllegalArgumentException("해당 반에 참여하지 않은 학생입니다.");
            }
            if (student.getTeam() != null) {
                throw new IllegalArgumentException("이미 다른 팀에 속한 학생이 포함되어 있습니다. (" + 
                        student.getName() + ": " + student.getTeam().getTeamName() + ")");
            }
        }

        return studentIds.stream()
                .map(studentMap::get)
                .collect(Collectors.toList());
    }
    // (team, 승인된 학생들, 배정할 학생들 수)
    private void validateTeamCapacity(Team team, List<User> allStudents, int newStudentCount) {
        if (team.getMaxMembers() != null) {
            int currentMembers = (int) allStudents.stream()
                    .filter(student -> student.getTeam() != null && 
                                     student.getTeam().getTeamId().equals(team.getTeamId()))
                    .count();

            if (currentMembers + newStudentCount > team.getMaxMembers()) {
                throw new IllegalArgumentException("팀의 최대 인원을 초과합니다. (현재: " + 
                        currentMembers + "명, 최대: " + team.getMaxMembers() + 
                        "명, 배정 시도: " + newStudentCount + "명)");
            }
        }
    }

    private void assignStudentsToTeam(List<User> students, Team team) {
        for (User student : students) {
            student.setTeam(team);
            userRepository.save(student);
        }
    }

    private TeamAssignResponse createTeamAssignResponse(Team team, 
                                                      ClassRoom classRoom,
                                                      List<User> assignedStudents,
                                                      List<User> allStudents,
                                                      LocalDateTime assignedAt) {
        // 배정된 학생 정보
        List<AssignedStudent> assignedStudentDtos = assignedStudents.stream()
                .map(student -> AssignedStudent.from(student, assignedAt))
                .collect(Collectors.toList());

        // 팀 정보 (배정 후 현황)
        TeamAssignInfo teamInfo = TeamAssignInfo.from(team, allStudents);

        log.info("팀 배정 완료 - teamId: {}, assignedStudents: {}", 
                team.getTeamId(), assignedStudents.size());

        return TeamAssignResponse.of(team, classRoom, assignedStudentDtos, teamInfo);
    }
}