package com.argo.backend.organization.dto.classleave;

import com.argo.backend.domain.team.entity.Team;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TeamInfoDto {
    
    private boolean wasInTeam;
    private Long teamId;
    private String teamName;
    private LocalDateTime removedFromTeamAt;
    
    public TeamInfoDto(boolean wasInTeam, Long teamId, String teamName, LocalDateTime removedFromTeamAt) {
        this.wasInTeam = wasInTeam;
        this.teamId = teamId;
        this.teamName = teamName;
        this.removedFromTeamAt = removedFromTeamAt;
    }
    
    public static TeamInfoDto fromTeam(Team team, LocalDateTime removedAt) {
        return new TeamInfoDto(
                true,
                team.getTeamId(),
                team.getTeamName(),
                removedAt
        );
    }
    
    public static TeamInfoDto noTeam() {
        return new TeamInfoDto(false, null, null, null);
    }
}