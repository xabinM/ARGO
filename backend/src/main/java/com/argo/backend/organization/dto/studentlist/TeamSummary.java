package com.argo.backend.organization.dto.studentlist;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class TeamSummary {
    private final Integer totalTeams;
    private final Integer assignedStudents;
    private final Integer unassignedStudents;
    private final List<TeamSummaryInfo> teams;

    public static TeamSummary from(ClassRoom classRoom) {
        List<User> approvedStudents = classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .map(app -> app.getUser())
                .collect(Collectors.toList());

        int assignedStudents = (int) approvedStudents.stream()
                .filter(student -> student.getTeam() != null)
                .count();

        int unassignedStudents = approvedStudents.size() - assignedStudents;

        List<TeamSummaryInfo> teams = classRoom.getTeams().stream()
                .map(team -> TeamSummaryInfo.from(team, approvedStudents))
                .collect(Collectors.toList());

        return TeamSummary.builder()
                .totalTeams(classRoom.getTeams().size())
                .assignedStudents(assignedStudents)
                .unassignedStudents(unassignedStudents)
                .teams(teams)
                .build();
    }

    @Getter
    @Builder
    public static class TeamSummaryInfo {
        private final Long teamId;
        private final String teamName;
        private final Integer currentMembers;
        private final Integer maxMembers;

        public static TeamSummaryInfo from(Team team, List<User> allStudents) {
            int currentMembers = (int) allStudents.stream()
                    .filter(student -> student.getTeam() != null && 
                                     student.getTeam().getTeamId().equals(team.getTeamId()))
                    .count();

            return TeamSummaryInfo.builder()
                    .teamId(team.getTeamId())
                    .teamName(team.getTeamName())
                    .currentMembers(currentMembers)
                    .maxMembers(team.getMaxMembers())
                    .build();
        }
    }
}