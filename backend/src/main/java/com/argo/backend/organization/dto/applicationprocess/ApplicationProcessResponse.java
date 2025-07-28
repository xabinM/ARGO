package com.argo.backend.organization.dto.applicationprocess;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApplicationProcessResponse {
    private final Long applicationId;
    private final Long studentId;
    private final String studentName;
    private final Long classId;
    private final String className;
    private final String status;
    private final LocalDateTime appliedAt;
    private final LocalDateTime processedAt;

    public static ApplicationProcessResponse from(ClassApplication application, ClassRoom classRoom) {
        return ApplicationProcessResponse.builder()
                .applicationId(application.getApplicationId())
                .studentId(application.getUser().getUserId())
                .studentName(application.getUser().getName())
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .status(application.getStatus().name().toLowerCase())
                .appliedAt(application.getCreatedAt())
                .processedAt(application.getProcessedAt())
                .build();
    }
}