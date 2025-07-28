package com.argo.backend.organization.dto.classdelete;

import com.argo.backend.domain.classroom.ClassRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ClassDeletedInfo {
    private final Long classId;
    private final String className;
    private final String description;
    private final String location;
    private final LocalDate activityDate;
    private final Long teacherId;
    private final String teacherName;
    private final LocalDateTime createdAt;
    private final LocalDateTime deletedAt;

    public static ClassDeletedInfo from(ClassRoom classRoom, LocalDateTime deletedAt) {
        return ClassDeletedInfo.builder()
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .description(classRoom.getDescription())
                .location(classRoom.getLocation() != null ? classRoom.getLocation().getName() : null)
                .activityDate(classRoom.getActivityDate())
                .teacherId(classRoom.getTeacher().getUserId())
                .teacherName(classRoom.getTeacher().getName())
                .createdAt(classRoom.getCreatedAt())
                .deletedAt(deletedAt)
                .build();
    }
}