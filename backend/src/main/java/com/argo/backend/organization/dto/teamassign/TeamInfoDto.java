package com.argo.backend.organization.dto.teamassign;

import com.argo.backend.domain.team.Team;

public record TeamInfoDto(
        Long teamId,
        String teamName,
        Long classId,
        String className,
        Integer currentMembers,
        Integer maxMembers,
        Integer availableSlots
) {
    public static TeamInfoDto from(Team team, Integer currentMembers) {
        Integer availableSlots = team.getMaxMembers() != null 
            ? team.getMaxMembers() - currentMembers 
            : null;
            
        return new TeamInfoDto(
                team.getTeamId(),
                team.getTeamName(),
                team.getClassRoom().getClassId(),
                team.getClassRoom().getClassName(),
                currentMembers,
                team.getMaxMembers(),
                availableSlots
        );
    }
}