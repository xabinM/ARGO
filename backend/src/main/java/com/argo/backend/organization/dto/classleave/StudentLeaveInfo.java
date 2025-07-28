package com.argo.backend.organization.dto.classleave;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudentLeaveInfo {
    private final Long studentId;
    private final String studentName;
    private final LocalDateTime joinedAt;
    private final LocalDateTime leftAt;

    public static StudentLeaveInfo of(User student, ClassApplication application, LocalDateTime leftAt) {
        return StudentLeaveInfo.builder()
                .studentId(student.getUserId())
                .studentName(student.getName())
                .joinedAt(application.getCreatedAt())
                .leftAt(leftAt)
                .build();
    }
}