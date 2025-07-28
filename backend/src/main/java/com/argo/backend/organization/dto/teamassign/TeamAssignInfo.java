package com.argo.backend.organization.dto.teamassign;

import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamAssignInfo {
    private final Integer currentMembers;
    private final Integer maxMembers;
    private final Integer availableSlots;

    public static TeamAssignInfo from(Team team, List<User> allStudents) {
        int currentMembers = (int) allStudents.stream()
                .filter(student -> student.getTeam() != null && 
                                 student.getTeam().getTeamId().equals(team.getTeamId()))
                .count();

        Integer maxMembers = team.getMaxMembers();
        Integer availableSlots = null;
        if (maxMembers != null) {
            availableSlots = maxMembers - currentMembers;
        }

        return TeamAssignInfo.builder()
                .currentMembers(currentMembers)
                .maxMembers(maxMembers)
                .availableSlots(availableSlots)
                .build();
    }
}