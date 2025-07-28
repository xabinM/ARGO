package com.argo.backend.organization.dto.classleave;

import com.argo.backend.domain.classroom.ClassRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class LeftClassInfo {
    private final Long classId;
    private final String className;
    private final String description;
    private final String location;
    private final LocalDate activityDate;
    private final String teacherName;

    public static LeftClassInfo from(ClassRoom classRoom) {
        return LeftClassInfo.builder()
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .description(classRoom.getDescription())
                .location(classRoom.getLocation() != null ? classRoom.getLocation().getName() : null)
                .activityDate(classRoom.getActivityDate())
                .teacherName(classRoom.getTeacher().getName())
                .build();
    }
}