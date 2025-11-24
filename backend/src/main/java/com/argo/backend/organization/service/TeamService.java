package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.team.entity.Team;
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
import com.argo.backend.organization.dto.teamautoassign.AutoAssignUnassignedStudentDto;
import com.argo.backend.organization.dto.teamdelete.TeamDeleteResponse;
import com.argo.backend.organization.dto.teamdelete.DeletedTeamDto;
import com.argo.backend.organization.dto.teamdelete.ClassTeamStatusDto;
import com.argo.backend.organization.dto.teamdelete.TeamDeletionUnassignedStudentDto;
import com.argo.backend.organization.exception.types.*;
import com.argo.backend.organization.exception.types.ClassNotFoundException;
import com.argo.backend.domain.classroom.repository.ClassRoomRepository;
import com.argo.backend.domain.classroom.repository.ClassStudentRepository;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.user.repository.UserTeamRepository;
import com.argo.backend.domain.user.entity.UserTeam;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final ClassRoomRepository classRoomRepository;
    private final ClassStudentRepository classStudentRepository;
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
                0,
                savedTeam.getCreatedAt()
        );
    }

    @Transactional
    public TeamAssignResponse assignStudentsToTeam(Long classId, Long teamId, TeamAssignRequest request, Long teacherId) {
        
        ClassRoom classRoom = validateClassAccess(classId, teacherId);
        
        Team team = validateTeamAccess(teamId, classRoom);

        List<User> students = findAndValidateStudents(classId, request.getStudentIds());
        
        validateTeamCapacity(team, students.size());
        
        LocalDateTime assignedAt = LocalDateTime.now();
        List<AssignedStudentDto> assignedStudents = assignStudentsToTeam(team, students, assignedAt, classId);
        
        long currentMembers = classStudentRepository.countByTeamId(teamId);
        TeamInfoDto teamInfo = TeamInfoDto.from(team, (int) currentMembers);
        
        return TeamAssignResponse.of(teamInfo, assignedStudents);
    }

    @Transactional
    public TeamAutoAssignResponse AssignStudentsToTeams(Long classId, Long teacherId) {
        ClassRoom classRoom = validateClassAccess(classId, teacherId);
        
        List<Team> teams = teamRepository.findTeamsByClassIdWithActiveMembersAndLeader(classRoom.getClassId());
        if (teams.isEmpty()) throw new TeamNotFoundException();
        
        List<User> unassignedStudents = classStudentRepository.findUnassignedStudentsByClassId(classId);
        if (unassignedStudents.isEmpty()) {
            return TeamAutoAssignResponse.ofNoAssignment(classRoom.getClassId(), classRoom.getClassName(), LocalDateTime.now());
        }
        
        Collections.shuffle(unassignedStudents);

        List<Long> teamIds = teams.stream().map(Team::getTeamId).toList();
        
        List<Object[]> counts = classStudentRepository.findTeamMemberCounts(teamIds);
        
        Map<Long, Integer> countMap = new HashMap<>();
        for (Object[] count : counts) {
            countMap.put((Long) count[0], ((Number) count[1]).intValue());
        }
        
        int[] teamCounts = new int[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            teamCounts[i] = countMap.getOrDefault(teams.get(i).getTeamId(), 0);
        }
        
        List<UserTeam> userTeamsToSave = new ArrayList<>();
        int totalAssigned = 0;
        while (!unassignedStudents.isEmpty()) {
            boolean anyAssignment = false;
            for (int i = 0; i < teams.size() && !unassignedStudents.isEmpty(); i++) {
                if (teams.get(i).getMaxMembers() == null || teamCounts[i] < teams.get(i).getMaxMembers()) {
                    User student = unassignedStudents.remove(0);
                    UserTeam userTeam = UserTeam.create(student, teams.get(i));
                    userTeamsToSave.add(userTeam);
                    student.getUserTeams().add(userTeam);
                    teamCounts[i]++;
                    totalAssigned++;
                    anyAssignment = true;
                }
            }
            if (!anyAssignment) break;
        }

        userTeamRepository.saveAll(userTeamsToSave);
        
        LocalDateTime assignedAt = LocalDateTime.now();
        return TeamAutoAssignResponse.of(
                classRoom.getClassId(),
                classRoom.getClassName(),
                totalAssigned,
                assignedAt,
                createTeamAssignments(teams),
                unassignedStudents.stream().map(u -> AutoAssignUnassignedStudentDto.of(u, "모든 팀이 가득 참")).toList()
        );
    }

    @Transactional
    public TeamDeleteResponse deleteTeam(Long classId, Long teamId, Long teacherId) {
        Team team = validateTeamAccess(classId, teamId, teacherId);
        
        List<UserTeam> activeUserTeams = userTeamRepository.findActiveByTeamId(teamId);
        List<User> teamMembers = activeUserTeams.stream()
                .map(UserTeam::getUser)
                .toList();
        
        activeUserTeams.forEach(UserTeam::deactivate);
        userTeamRepository.saveAll(activeUserTeams);
        
        LocalDateTime now = LocalDateTime.now();
        DeletedTeamDto deletedTeam = DeletedTeamDto.from(team, now);
        List<TeamDeletionUnassignedStudentDto> unassignedStudents = teamMembers.stream()
                .map(student -> TeamDeletionUnassignedStudentDto.from(student, now))
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

        if (students.size() != studentIds.size()) {
            throw new StudentNotFoundException();
        }

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
        List<UserTeam> userTeamsToSave = students.stream()
                .map(student -> {
                    UserTeam userTeam = UserTeam.create(student, team);
                    student.getUserTeams().add(userTeam);
                    return userTeam;
                })
                .collect(Collectors.toList());

        userTeamRepository.saveAll(userTeamsToSave);

        return students.stream()
                .map(student -> AssignedStudentDto.from(student, assignedAt))
                .collect(Collectors.toList());
    }

    private List<TeamAssignmentDto> createTeamAssignments(List<Team> teams) {
        return teams.stream()
                .map(team -> {
                    List<User> members = team.getActiveMembers();
                    return members.isEmpty() ? null : TeamAssignmentDto.of(team,
                            members.stream().map(AssignedStudentInfoDto::from).toList(),
                            TeamStatusDto.of(team, members.size()));
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Team validateTeamAccess(Long classId, Long teamId, Long teacherId) {
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(ClassNotFoundException::new);

        if (!classRoom.getTeacher().getUserId().equals(teacherId)) {
            throw new UnauthorizedClassAccessException();
        }

        Team team = teamRepository.findByTeamIdAndClassRoom(teamId, classRoom);
        if (team == null) {
            throw new TeamNotFoundException();
        }

        return team;
    }

    private ClassTeamStatusDto buildClassTeamStatus(Long classId) {
        List<Object[]> results = classStudentRepository.findApprovedStudentsWithTeamAndJoinDateByClassId(classId);
        int totalStudents = results.size();
        int assignedStudents = (int) results.stream()
                .filter(result -> result[2] != null)
                .count();
        int totalTeams = teamRepository.findTeamsByClassId(classId).size();

        return ClassTeamStatusDto.of(totalTeams, totalStudents, assignedStudents, totalStudents - assignedStudents);
    }

}
