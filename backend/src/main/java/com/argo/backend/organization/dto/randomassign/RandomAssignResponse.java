package com.argo.backend.organization.dto.randomassign;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RandomAssignResponse {
    private final Long classId;
    private final String className;
    private final String assignmentType;
    private final Integer totalAssigned;
    private final LocalDateTime assignedAt;
    private final List<TeamAssignmentResult> teamAssignments;
    private final List<RemainingStudent> remainingUnassigned;
    private final String message; // 특별한 경우 메시지 (모든 학생이 이미 배정된 경우 등)

    public static RandomAssignResponse of(Long classId,
                                        String className,
                                        String assignmentType,
                                        Integer totalAssigned,
                                        LocalDateTime assignedAt,
                                        List<TeamAssignmentResult> teamAssignments, // 배정받은 애들
                                        List<RemainingStudent> remainingUnassigned, // 배정받지 못한애들
                                        String message) {
        return RandomAssignResponse.builder()
                .classId(classId)
                .className(className)
                .assignmentType(assignmentType)
                .totalAssigned(totalAssigned)
                .assignedAt(assignedAt)
                .teamAssignments(teamAssignments)
                .remainingUnassigned(remainingUnassigned)
                .message(message)
                .build();
    }
}