package com.argo.backend.organization.dto.teamassign;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamAssignResponse {
    private final Long teamId;
    private final String teamName;
    private final Long classId;
    private final String className;
    private final List<AssignedStudent> assignedStudents;
    private final TeamAssignInfo teamInfo;

    public static TeamAssignResponse of(Team team, 
                                      ClassRoom classRoom,
                                      List<AssignedStudent> assignedStudents,
                                      TeamAssignInfo teamInfo) {
        return TeamAssignResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .assignedStudents(assignedStudents)
                .teamInfo(teamInfo)
                .build();
    }
}