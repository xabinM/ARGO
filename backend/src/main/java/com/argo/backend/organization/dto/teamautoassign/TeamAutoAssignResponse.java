package com.argo.backend.organization.dto.teamautoassign;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamAutoAssignResponse {
    
    private final Long classId;
    private final String className;
    private final int totalAssigned;
    private final LocalDateTime assignedAt;
    private final List<TeamAssignmentDto> teamAssignments;
    private final List<AutoAssignUnassignedStudentDto> remainingUnassigned;
    private final String message;
    
    public static TeamAutoAssignResponse of(
            Long classId,
            String className,
            int totalAssigned,
            LocalDateTime assignedAt,
            List<TeamAssignmentDto> teamAssignments,
            List<AutoAssignUnassignedStudentDto> remainingUnassigned
    ) {
        return new TeamAutoAssignResponse(
                classId,
                className,
                totalAssigned,
                assignedAt,
                teamAssignments,
                remainingUnassigned,
                null
        );
    }
    
    public static TeamAutoAssignResponse ofNoAssignment(
            Long classId,
            String className,
            LocalDateTime assignedAt
    ) {
        return new TeamAutoAssignResponse(
                classId,
                className,
                0,
                assignedAt,
                List.of(),
                List.of(),
                "모든 학생이 이미 팀에 배정되어 있습니다."
        );
    }
}