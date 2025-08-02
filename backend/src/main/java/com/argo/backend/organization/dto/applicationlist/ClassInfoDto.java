package com.argo.backend.organization.dto.applicationlist;

import com.argo.backend.domain.classroom.ClassRoom;

public record ClassInfoDto(
    Long classId,
    String className,
    Integer currentStudents,
    Integer maxStudents
) {
    public static ClassInfoDto from(ClassRoom classRoom, Integer currentStudents) {
        return new ClassInfoDto(
                classRoom.getClassId(),
                classRoom.getClassName(),
                currentStudents,
                classRoom.getMaxStudents()
        );
    }
}