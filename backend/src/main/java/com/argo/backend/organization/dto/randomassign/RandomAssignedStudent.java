package com.argo.backend.organization.dto.randomassign;

import com.argo.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RandomAssignedStudent {
    private final Long studentId;
    private final String studentName;

    public static RandomAssignedStudent from(User student) {
        return RandomAssignedStudent.builder()
                .studentId(student.getUserId())
                .studentName(student.getName())
                .build();
    }
}