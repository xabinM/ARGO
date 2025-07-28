package com.argo.backend.organization.dto.teamcreate;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TeamCreateResponse {
    private final Long teamId;
    private final String teamName;
    private final Long classId;
    private final String className;
    private final Integer maxMembers;
    private final Integer currentMembers;
    private final LocalDateTime createdAt;

    public static TeamCreateResponse from(Team team, ClassRoom classRoom) {
        return TeamCreateResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .maxMembers(team.getMaxMembers())
                .currentMembers(0) // 생성 시점에는 0명
                .createdAt(team.getCreatedAt())
                .build();
    }
}