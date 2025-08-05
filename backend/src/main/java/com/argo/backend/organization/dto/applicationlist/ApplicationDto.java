package com.argo.backend.organization.dto.applicationlist;

import com.argo.backend.domain.classroom.entity.ClassApplication;

import java.time.LocalDateTime;

public record ApplicationDto(
    Long applicationId,
    Long studentId,
    String studentName,
    String status,
    LocalDateTime appliedAt,
    LocalDateTime processedAt
) {
    public static ApplicationDto from(ClassApplication application) {
        return new ApplicationDto(
                application.getApplicationId(),
                application.getUser().getUserId(),
                application.getUser().getName(),
                application.getStatus().name().toLowerCase(),
                application.getCreatedAt(),
                application.getProcessedAt()
        );
    }
}