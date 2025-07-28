package com.argo.backend.organization.dto.teamdelete;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UnassignedStudentInfo {
    private final Long studentId;
    private final String studentName;
    private final LocalDateTime unassignedAt;
    
    public static UnassignedStudentInfo from(com.argo.backend.domain.user.User student, LocalDateTime unassignedAt) {
        return UnassignedStudentInfo.builder()
                .studentId(student.getUserId())
                .studentName(student.getName())
                .unassignedAt(unassignedAt)
                .build();
    }
}