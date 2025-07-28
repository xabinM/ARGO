package com.argo.backend.organization.dto.teamassign;

import com.argo.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AssignedStudent {
    private final Long studentId;
    private final String studentName;
    private final LocalDateTime assignedAt;

    public static AssignedStudent from(User student, LocalDateTime assignedAt) {
        return AssignedStudent.builder()
                .studentId(student.getUserId())
                .studentName(student.getName())
                .assignedAt(assignedAt)
                .build();
    }
}