package com.argo.backend.organization.dto.classapply;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClassApplyResponse(
    Long applicationId,

    Long classId,

    String className,

    String description,

    String location,

    LocalDate activityDate,
    
    String teacherName,
    
    String applicationStatus,
    
    LocalDateTime appliedAt
){}