package com.argo.backend.organization.dto.teamdelete;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassTeamStatus {
    private final Integer totalTeams;
    private final Integer totalStudents;
    private final Integer assignedStudents;
    private final Integer unassignedStudents;
}