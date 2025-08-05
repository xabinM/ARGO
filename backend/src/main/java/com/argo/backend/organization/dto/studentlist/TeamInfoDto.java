package com.argo.backend.organization.dto.studentlist;

import com.argo.backend.domain.team.entity.Team;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TeamInfoDto {
    
    private Long teamId;
    private String teamName;
    private LocalDateTime assignedAt;
    
    public TeamInfoDto(Long teamId, String teamName, LocalDateTime assignedAt) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.assignedAt = assignedAt;
    }
    
    public static TeamInfoDto from(Team team, LocalDateTime assignedAt) {
        return new TeamInfoDto(
                team.getTeamId(),
                team.getTeamName(),
                assignedAt
        );
    }
}