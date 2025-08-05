package com.argo.backend.organization.dto.studentlist;

import com.argo.backend.domain.team.Team;
import lombok.Getter;

@Getter
public class TeamStatusDto {
    
    private Long teamId;
    private String teamName;
    private int currentMembers;
    private Integer maxMembers;
    
    public TeamStatusDto(Long teamId, String teamName, int currentMembers, Integer maxMembers) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.currentMembers = currentMembers;
        this.maxMembers = maxMembers;
    }
    
    public static TeamStatusDto from(Team team, int currentMembers) {
        return new TeamStatusDto(
                team.getTeamId(),
                team.getTeamName(),
                currentMembers,
                team.getMaxMembers()
        );
    }
}