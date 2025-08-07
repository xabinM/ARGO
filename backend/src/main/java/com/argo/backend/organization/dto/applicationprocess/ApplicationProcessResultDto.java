package com.argo.backend.organization.dto.applicationprocess;

import com.argo.backend.domain.classroom.entity.ClassApplication;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ApplicationProcessResultDto(
        Long applicationId,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime processedAt
) {
    public static ApplicationProcessResultDto from(ClassApplication application) {
        return new ApplicationProcessResultDto(
                application.getApplicationId(),
                application.getStatus().name().toLowerCase(),
                application.getProcessedAt()
        );
    }
}