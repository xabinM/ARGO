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
import com.argo.backend.organization.exception.*;
import com.argo.backend.organization.repository.ClassRoomRepository;
import com.argo.backend.organization.repository.ClassStudentRepository;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.organization.repository.TeacherRepository;
import com.argo.backend.organization.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
                .orElseThrow(com.argo.backend.organization.exception.ClassNotFoundException::new);

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
}