package com.argo.backend.organization.dto.applicationlist;

import com.argo.backend.domain.classroom.ClassApplication;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApplicationDto {
    private final Long applicationId;
    private final Long studentId;
    private final String studentName;
    private final String status;
    private final LocalDateTime appliedAt;
    private final LocalDateTime processedAt;

    public static ApplicationDto from(ClassApplication application) {
        return ApplicationDto.builder()
                .applicationId(application.getApplicationId())
                .studentId(application.getUser().getUserId())
                .studentName(application.getUser().getName())
                .status(application.getStatus().name().toLowerCase())
                .appliedAt(application.getCreatedAt())
                .processedAt(application.getProcessedAt())
                .build();
    }
}