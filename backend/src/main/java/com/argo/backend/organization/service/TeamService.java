package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.user.entity.Teacher;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.organization.dto.teamcreate.TeamCreateRequest;
import com.argo.backend.organization.dto.teamcreate.TeamCreateResponse;
import com.argo.backend.organization.dto.teamassign.TeamAssignRequest;
import com.argo.backend.organization.dto.teamassign.TeamAssignResponse;
import com.argo.backend.organization.dto.teamassign.AssignedStudentDto;
import com.argo.backend.organization.dto.teamassign.TeamInfoDto;
import com.argo.backend.organization.dto.teamautoassign.TeamAutoAssignResponse;
import com.argo.backend.organization.dto.teamautoassign.TeamAssignmentDto;
import com.argo.backend.organization.dto.teamautoassign.AssignedStudentInfoDto;
import com.argo.backend.organization.dto.teamautoassign.TeamStatusDto;
import com.argo.backend.organization.dto.teamdelete.TeamDeleteResponse;
import com.argo.backend.organization.dto.teamdelete.DeletedTeamDto;
import com.argo.backend.organization.dto.teamdelete.UnassignedStudentDto;
import com.argo.backend.organization.dto.teamdelete.ClassTeamStatusDto;
import com.argo.backend.organization.exception.types.*;
import com.argo.backend.organization.exception.types.ClassNotFoundException;
import com.argo.backend.domain.classroom.repository.ClassRoomRepository;
import com.argo.backend.domain.classroom.repository.ClassStudentRepository;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.domain.user.repository.TeacherRepository;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.user.repository.UserTeamRepository;
import com.argo.backend.domain.user.entity.UserTeam;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TeacherRepository teacherRepository;
    private final ClassStudentRepository classStudentRepository;
    private final UserRepository userRepository;
    private final UserTeamRepository userTeamRepository;

    @Transactional
    public TeamCreateResponse createTeam(Long classId, TeamCreateRequest request, Long teacherId) {
        ClassRoom classRoom = validateClassAccess(classId, teacherId);

        validateTeamNameUniqueness(request.getTeamName(), classRoom);

        Team team = Team.from(
                classRoom,
                request.getTeamName(),
                request.getMaxMembers()
        );
        Team savedTeam = teamRepository.save(team);

        return new TeamCreateResponse(
                savedTeam.getTeamId(),
                savedTeam.getTeamName(),
                savedTeam.getClassRoom().getClassId(),
                savedTeam.getClassRoom().getClassName(),
                savedTeam.getMaxMembers(),
                0, // 초기 멤버 수는 0
                savedTeam.getCreatedAt()
        );
    }

    @Transactional
    public TeamAssignResponse assignStudentsToTeam(Long classId, Long teamId, TeamAssignRequest request, Long teacherId) {
        
        // 1. 반 접근 권한 검증
        ClassRoom classRoom = validateClassAccess(classId, teacherId);
        
        // 2. 팀 검증
        Team team = validateTeamAccess(teamId, classRoom);
        
        // 3. 학생 검증
        List<User> students = findAndValidateStudents(classId, request.getStudentIds());
        
        // 4. 팀 용량 검증
        validateTeamCapacity(team, students.size());
        
        // 5. 학생들을 팀에 배정
        LocalDateTime assignedAt = LocalDateTime.now();
        List<AssignedStudentDto> assignedStudents = assignStudentsToTeam(team, students, assignedAt, classId);
        
        // 6. 응답 생성
        long currentMembers = classStudentRepository.countByTeamId(teamId);
        TeamInfoDto teamInfo = TeamInfoDto.from(team, (int) currentMembers);
        
        return TeamAssignResponse.of(teamInfo, assignedStudents);
    }

    @Transactional
    public TeamAutoAssignResponse autoAssignStudentsToTeams(Long classId, Long teacherId) {
        ClassRoom classRoom = validateClassAccess(classId, teacherId);
        
        List<Team> teams = teamRepository.findTeamsByClassIdWithActiveMembersAndLeader(classRoom.getClassId());
        if (teams.isEmpty()) throw new TeamNotFoundException();
        
        List<User> unassignedStudents = classStudentRepository.findUnassignedStudentsByClassId(classId);
        if (unassignedStudents.isEmpty()) {
            return TeamAutoAssignResponse.ofNoAssignment(classRoom.getClassId(), classRoom.getClassName(), LocalDateTime.now());
        }
        
        Collections.shuffle(unassignedStudents);
        
        // 직접 배정 로직 - N+1 문제 해결: 배치 쿼리 사용
        // 1. 팀 ID들만 뽑아내기
        List<Long> teamIds = teams.stream().map(Team::getTeamId).toList();
        
        // 2. 한 번에 모든 팀 멤버 수 조회
        List<Object[]> counts = classStudentRepository.findTeamMemberCounts(teamIds);
        
        // 3. 팀ID -> 멤버수 맵으로 변환
        Map<Long, Integer> countMap = new HashMap<>();
        for (Object[] count : counts) {
            countMap.put((Long) count[0], ((Number) count[1]).intValue());
        }
        
        // 4. 배열에 넣기
        int[] teamCounts = new int[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            teamCounts[i] = countMap.getOrDefault(teams.get(i).getTeamId(), 0);
        }
        
        int totalAssigned = 0;
        while (!unassignedStudents.isEmpty()) {
            boolean anyAssignment = false;
            for (int i = 0; i < teams.size() && !unassignedStudents.isEmpty(); i++) {
                if (teams.get(i).getMaxMembers() == null || teamCounts[i] < teams.get(i).getMaxMembers()) {
                    User student = unassignedStudents.remove(0);
                    // UserTeam 생성으로 팀 배정
                    UserTeam userTeam = UserTeam.create(student, teams.get(i));
                    userTeamRepository.save(userTeam);
                    student.getUserTeams().add(userTeam);
                    teamCounts[i]++;
                    totalAssigned++;
                    anyAssignment = true;
                }
            }
            if (!anyAssignment) break;
        }
        
        LocalDateTime assignedAt = LocalDateTime.now();
        return TeamAutoAssignResponse.of(
                classRoom.getClassId(),
                classRoom.getClassName(),
                totalAssigned,
                assignedAt,
                createTeamAssignments(teams),
                unassignedStudents.stream().map(u -> com.argo.backend.organization.dto.teamautoassign.UnassignedStudentDto.of(u, "모든 팀이 가득 참")).toList()
        );
    }

    @Transactional
    public TeamDeleteResponse deleteTeam(Long classId, Long teamId, Long teacherId) {
        ClassRoom classRoom = validateTeamAccess(classId, teamId, teacherId);
        Team team = teamRepository.findByTeamIdAndClassRoom(teamId, classRoom);
        
        // UserTeam 기반으로 팀 멤버 조회 및 해제
        List<UserTeam> activeUserTeams = userTeamRepository.findActiveByTeamId(teamId);
        List<User> teamMembers = activeUserTeams.stream()
                .map(UserTeam::getUser)
                .toList();
        
        // UserTeam 비활성화
        activeUserTeams.forEach(UserTeam::deactivate);
        userTeamRepository.saveAll(activeUserTeams);
        
        LocalDateTime now = LocalDateTime.now();
        DeletedTeamDto deletedTeam = DeletedTeamDto.from(team, now);
        List<UnassignedStudentDto> unassignedStudents = teamMembers.stream()
                .map(student -> UnassignedStudentDto.from(student, now))
                .toList();
        
        teamRepository.delete(team);
        ClassTeamStatusDto status = buildClassTeamStatus(classId);
        
        return TeamDeleteResponse.of(deletedTeam, unassignedStudents, status);
    }
    


    private ClassRoom validateClassAccess(Long classId, Long teacherId) {
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(ClassNotFoundException::new);

        if (!classRoom.getTeacher().getUserId().equals(teacherId)) {
            throw new UnauthorizedClassAccessException();
        }

        return classRoom;
    }

    private void validateTeamNameUniqueness(String teamName, ClassRoom classRoom) {
        if (teamRepository.existsByTeamNameAndClassRoom(teamName, classRoom)) {
            throw new DuplicateTeamNameException();
        }
    }

    private Team validateTeamAccess(Long teamId, ClassRoom classRoom) {
        Team team = teamRepository.findByTeamIdAndClassRoom(teamId, classRoom);
        if (team == null) {
            throw new TeamNotFoundException();
        }
        return team;
    }

    private List<User> findAndValidateStudents(Long classId, List<Long> studentIds) {
        List<Object[]> studentResults = classStudentRepository.findApprovedStudentsByIdsAndClassIdWithTeam(studentIds, classId);
        List<User> students = studentResults.stream()
                .map(result -> (User) result[0])
                .toList();

        // 존재 검증
        if (students.size() != studentIds.size()) {
            throw new StudentNotFoundException();
        }

        // 팀 배정 상태 검증 (UserTeam 기반, N+1 문제 해결: 배치 조회)
        List<Long> assignedStudentIds = userTeamRepository.findAssignedStudentIdsByStudentIdsAndClassId(studentIds, classId);
        if (!assignedStudentIds.isEmpty()) {
            throw new StudentAlreadyAssignedException();
        }

        return students;
    }

    private void validateTeamCapacity(Team team, int newStudentCount) {
        if (team.getMaxMembers() != null) {
            long currentMembers = classStudentRepository.countByTeamId(team.getTeamId());
            if (currentMembers + newStudentCount > team.getMaxMembers()) {
                throw new TeamCapacityExceededException();
            }
        }
    }

    private List<AssignedStudentDto> assignStudentsToTeam(Team team, List<User> students, LocalDateTime assignedAt, Long classId) {
        // N+1 문제 해결: 이미 findAndValidateStudents()에서 중복 검증 완료했으므로 직접 배정
        return students.stream()
                .map(student -> {
                    // UserTeam 생성으로 팀 배정 (중복 체크 생략 - 이미 검증됨)
                    UserTeam userTeam = UserTeam.create(student, team);
                    userTeamRepository.save(userTeam);
                    student.getUserTeams().add(userTeam);
                    return AssignedStudentDto.from(student, assignedAt);
                })
                .toList();
    }

    private List<TeamAssignmentDto> createTeamAssignments(List<Team> teams) {
        return teams.stream()
                .map(team -> {
                    List<User> members = team.getActiveMembers(); // 이미 FETCH JOIN으로 로딩됨 (쿼리 없음)
                    return members.isEmpty() ? null : TeamAssignmentDto.of(team,
                            members.stream().map(AssignedStudentInfoDto::from).toList(),
                            TeamStatusDto.of(team, members.size()));
                })
                .filter(dto -> dto != null)
                .toList();
    }

    private ClassRoom validateTeamAccess(Long classId, Long teamId, Long teacherId) {
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(ClassNotFoundException::new);

        if (!classRoom.getTeacher().getUserId().equals(teacherId)) {
            throw new UnauthorizedClassAccessException();
        }

        Team team = teamRepository.findByTeamIdAndClassRoom(teamId, classRoom);
        if (team == null) {
            throw new TeamNotFoundException();
        }

        return classRoom;
    }

    private ClassTeamStatusDto buildClassTeamStatus(Long classId) {
        List<Object[]> results = classStudentRepository.findApprovedStudentsWithTeamAndJoinDateByClassId(classId);
        int totalStudents = results.size();
        int assignedStudents = (int) results.stream()
                .filter(result -> result[2] != null) // Team 객체가 null이 아니면 배정됨
                .count();
        int totalTeams = teamRepository.findTeamsByClassId(classId).size();

        return ClassTeamStatusDto.of(totalTeams, totalStudents, assignedStudents, totalStudents - assignedStudents);
    }

}