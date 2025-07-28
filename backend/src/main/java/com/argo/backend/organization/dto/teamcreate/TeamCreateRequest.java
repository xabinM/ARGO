package com.argo.backend.organization.dto.teamcreate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamCreateRequest {
    private String teamName;
    
    private Integer maxMembers;

    public boolean isValid() {
        return teamName != null && !teamName.trim().isEmpty();
    }

    public String getTeamNameTrimmed() {
        return teamName != null ? teamName.trim() : null;
    }

    public Integer getMaxMembersWithDefault() {
        return maxMembers != null && maxMembers > 0 ? maxMembers : null;
    }
}