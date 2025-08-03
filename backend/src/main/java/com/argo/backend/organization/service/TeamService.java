package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.Teacher;
import com.argo.backend.organization.dto.teamcreate.TeamCreateRequest;
import com.argo.backend.organization.dto.teamcreate.TeamCreateResponse;
import com.argo.backend.organization.exception.*;
import com.argo.backend.organization.repository.ClassRoomRepository;
import com.argo.backend.organization.repository.TeacherRepository;
import com.argo.backend.organization.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final ClassRoomRepository classRoomRepository;
    private final TeacherRepository teacherRepository;

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
}