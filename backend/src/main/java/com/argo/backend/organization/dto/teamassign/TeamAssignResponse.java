package com.argo.backend.organization.dto.teamassign;

import java.util.List;

public record TeamAssignResponse(
        TeamInfoDto teamInfo,
        List<AssignedStudentDto> assignedStudents
) {
    public static TeamAssignResponse of(TeamInfoDto teamInfo, List<AssignedStudentDto> assignedStudents) {
        return new TeamAssignResponse(teamInfo, assignedStudents);
    }
}