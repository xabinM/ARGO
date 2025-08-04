package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.Teacher;
import com.argo.backend.domain.user.User;
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
import com.argo.backend.organization.exception.*;
import com.argo.backend.organization.exception.ClassNotFoundException;
import com.argo.backend.organization.repository.ClassRoomRepository;
import com.argo.backend.organization.repository.ClassStudentRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.TeacherRepository;
import com.argo.backend.organization.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TeacherRepository teacherRepository;
    private final ClassStudentRepository classStudentRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamCreateResponse createTeam(Long classId, TeamCreateRequest request, Long teacherId) {
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(InsufficientPermissionException::new);

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
        List<AssignedStudentDto> assignedStudents = assignStudentsToTeam(team, students, assignedAt);
        
        // 6. 응답 생성
        long currentMembers = classStudentRepository.countByTeamId(teamId);
        TeamInfoDto teamInfo = TeamInfoDto.from(team, (int) currentMembers);
        
        return TeamAssignResponse.of(teamInfo, assignedStudents);
    }
    
    private Team validateTeamAccess(Long teamId, ClassRoom classRoom) {
        Team team = teamRepository.findByTeamIdAndClassRoom(teamId, classRoom);
        if (team == null) {
            throw new TeamNotFoundException();
        }
        return team;
    }
    
    private List<User> findAndValidateStudents(Long classId, List<Long> studentIds) {
        List<User> students = classStudentRepository.findApprovedStudentsByIdsAndClassId(studentIds, classId);
        
        // 존재 검증
        if (students.size() != studentIds.size()) {
            throw new StudentNotFoundException();
        }
        
        // 팀 배정 상태 검증
        for (User student : students) {
            if (student.getTeam() != null) {
                throw new StudentAlreadyAssignedException();
            }
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
    
    private List<AssignedStudentDto> assignStudentsToTeam(Team team, List<User> students, LocalDateTime assignedAt) {
        return students.stream()
                .map(student -> {
                    student.setTeam(team);
                    userRepository.save(student);
                    return AssignedStudentDto.from(student, assignedAt);
                })
                .toList();
    }
    
    @Transactional
    public TeamAutoAssignResponse autoAssignStudentsToTeams(Long classId, Long teacherId) {
        ClassRoom classRoom = validateClassAccess(classId, teacherId);
        
        List<Team> teams = teamRepository.findAllTeamsWithDetailsByClassRoom(classRoom);
        if (teams.isEmpty()) throw new TeamNotFoundException();
        
        List<User> unassignedStudents = classStudentRepository.findUnassignedStudentsByClassId(classId);
        if (unassignedStudents.isEmpty()) {
            return TeamAutoAssignResponse.ofNoAssignment(classRoom.getClassId(), classRoom.getClassName(), LocalDateTime.now());
        }
        
        Collections.shuffle(unassignedStudents);
        
        // 직접 배정 로직
        int[] teamCounts = new int[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            teamCounts[i] = (int) classStudentRepository.countByTeamId(teams.get(i).getTeamId());
        }
        
        int totalAssigned = 0;
        while (!unassignedStudents.isEmpty()) {
            boolean anyAssignment = false;
            for (int i = 0; i < teams.size() && !unassignedStudents.isEmpty(); i++) {
                if (teams.get(i).getMaxMembers() == null || teamCounts[i] < teams.get(i).getMaxMembers()) {
                    User student = unassignedStudents.remove(0);
                    student.setTeam(teams.get(i));
                    userRepository.save(student);
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
    
    private List<TeamAssignmentDto> createTeamAssignments(List<Team> teams) {
        return teams.stream()
                .map(team -> {
                    List<User> members = userRepository.findByTeamId(team.getTeamId());
                    return members.isEmpty() ? null : TeamAssignmentDto.of(team,
                            members.stream().map(AssignedStudentInfoDto::from).toList(),
                            TeamStatusDto.of(team, members.size()));
                })
                .filter(dto -> dto != null)
                .toList();
    }
    
    @Transactional
    public TeamDeleteResponse deleteTeam(Long classId, Long teamId, Long teacherId) {
        ClassRoom classRoom = validateTeamAccess(classId, teamId, teacherId);
        Team team = teamRepository.findByTeamIdAndClassRoom(teamId, classRoom);
        
        List<User> teamMembers = userRepository.findByTeamId(teamId);
        teamMembers.forEach(student -> student.setTeam(null));
        
        LocalDateTime now = LocalDateTime.now();
        DeletedTeamDto deletedTeam = DeletedTeamDto.from(team, now);
        List<UnassignedStudentDto> unassignedStudents = teamMembers.stream()
                .map(student -> UnassignedStudentDto.from(student, now))
                .toList();
        
        teamRepository.delete(team);
        ClassTeamStatusDto status = buildClassTeamStatus(classId);
        
        return TeamDeleteResponse.of(deletedTeam, unassignedStudents, status);
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
                .filter(result -> ((User) result[0]).getTeam() != null)
                .count();
        int totalTeams = teamRepository.findTeamsByClassId(classId).size();
        
        return ClassTeamStatusDto.of(totalTeams, totalStudents, assignedStudents, totalStudents - assignedStudents);
    }
}