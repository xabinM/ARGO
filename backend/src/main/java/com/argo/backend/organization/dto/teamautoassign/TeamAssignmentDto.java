package com.argo.backend.organization.dto.teamautoassign;

import com.argo.backend.domain.team.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamAssignmentDto {
    
    private final Long teamId;
    private final String teamName;
    private final List<AssignedStudentInfoDto> assignedStudents;
    private final TeamStatusDto teamInfo;
    
    public static TeamAssignmentDto of(
            Team team,
            List<AssignedStudentInfoDto> assignedStudents,
            TeamStatusDto teamInfo
    ) {
        return new TeamAssignmentDto(
                team.getTeamId(),
                team.getTeamName(),
                assignedStudents,
                teamInfo
        );
    }
}