package com.argo.backend.organization.dto.randomassign;

import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamAssignmentResult {
    private final Long teamId;
    private final String teamName;
    private final List<RandomAssignedStudent> assignedStudents;
    private final TeamInfoForRandom teamInfo;

    public static TeamAssignmentResult of(Team team, 
                                        List<User> assignedStudents,
                                        List<User> allStudents) {
        List<RandomAssignedStudent> studentDtos = assignedStudents.stream()
                .map(RandomAssignedStudent::from)
                .collect(java.util.stream.Collectors.toList());

        TeamInfoForRandom teamInfo = TeamInfoForRandom.from(team, allStudents);

        return TeamAssignmentResult.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .assignedStudents(studentDtos)
                .teamInfo(teamInfo)
                .build();
    }

    @Getter
    @Builder
    public static class TeamInfoForRandom {
        private final Integer currentMembers;
        private final Integer maxMembers;
        private final Integer availableSlots;

        public static TeamInfoForRandom from(Team team, List<User> allStudents) {
            int currentMembers = (int) allStudents.stream()
                    .filter(student -> student.getTeam() != null && 
                                     student.getTeam().getTeamId().equals(team.getTeamId()))
                    .count();

            Integer maxMembers = team.getMaxMembers();
            Integer availableSlots = null;
            if (maxMembers != null) {
                availableSlots = Math.max(0, maxMembers - currentMembers);
            }

            return TeamInfoForRandom.builder()
                    .currentMembers(currentMembers)
                    .maxMembers(maxMembers)
                    .availableSlots(availableSlots)
                    .build();
        }
    }
}