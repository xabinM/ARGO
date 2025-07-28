package com.argo.backend.organization.dto.randomassign;

import com.argo.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RemainingStudent {
    private final Long studentId;
    private final String studentName;
    private final String reason;

    public static RemainingStudent from(User student, String reason) {
        return RemainingStudent.builder()
                .studentId(student.getUserId())
                .studentName(student.getName())
                .reason(reason)
                .build();
    }
}