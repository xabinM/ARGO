package com.argo.backend.organization.dto.classleave;

import com.argo.backend.domain.team.Team;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TeamLeaveInfo {
    private final Boolean wasInTeam;
    private final Long teamId;
    private final String teamName;
    private final LocalDateTime removedFromTeamAt;

    public static TeamLeaveInfo of(Team team, LocalDateTime removedAt) {
        if (team == null) {
            return TeamLeaveInfo.builder()
                    .wasInTeam(false)
                    .teamId(null)
                    .teamName(null)
                    .removedFromTeamAt(null)
                    .build();
        }

        return TeamLeaveInfo.builder()
                .wasInTeam(true)
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .removedFromTeamAt(removedAt)
                .build();
    }
}