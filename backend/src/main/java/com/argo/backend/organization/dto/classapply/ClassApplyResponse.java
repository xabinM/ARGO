package com.argo.backend.organization.dto.classapply;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ClassApplyResponse {
    private final Long applicationId;
    
    private final Long classId;
    
    private final String className;
    
    private final String description;
    
    private final String location;
    
    private final LocalDate activityDate;
    
    private final String teacherName;
    
    private final String applicationStatus;
    
    private final LocalDateTime appliedAt;

    public static ClassApplyResponse from(ClassApplication application, ClassRoom classRoom) {
        // 위치 정보 추출
        String location = classRoom.getClassLocations().stream()
                .findFirst()
                .map(classLocation -> classLocation.getLocation().getName())
                .orElse("미정");

        return ClassApplyResponse.builder()
                .applicationId(application.getApplicationId())
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .description(classRoom.getDescription())
                .location(location)
                .activityDate(classRoom.getActivityDate())
                .teacherName(classRoom.getTeacher().getName())
                .applicationStatus(application.getStatus().name().toLowerCase())
                .appliedAt(application.getCreatedAt())
                .build();
    }
}