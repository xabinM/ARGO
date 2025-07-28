package com.argo.backend.organization.dto.classdelete;

import com.argo.backend.organization.dto.classdetail.TeamDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamDeletedData {
    private final int count;
    private final List<TeamDto> details;

    public static TeamDeletedData of(List<TeamDto> teams) {
        return TeamDeletedData.builder()
                .count(teams.size())
                .details(teams)
                .build();
    }
}