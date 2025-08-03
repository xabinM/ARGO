package com.argo.backend.organization.dto.teamautoassign;

import com.argo.backend.domain.team.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamStatusDto {
    
    private final int currentMembers;
    private final Integer maxMembers;
    private final int availableSlots;
    
    public static TeamStatusDto of(Team team, int currentMembers) {
        int availableSlots = team.getMaxMembers() != null ? 
                Math.max(0, team.getMaxMembers() - currentMembers) : 
                Integer.MAX_VALUE;
                
        return new TeamStatusDto(
                currentMembers,
                team.getMaxMembers(),
                availableSlots
        );
    }
}