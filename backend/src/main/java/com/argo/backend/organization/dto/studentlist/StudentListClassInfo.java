package com.argo.backend.organization.dto.studentlist;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.user.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentListClassInfo {
    private final Long classId;
    private final String className;
    private final Integer totalStudents;
    private final Integer maxStudents;

    public static StudentListClassInfo from(ClassRoom classRoom) {
        int totalStudents = (int) classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .count();

        return StudentListClassInfo.builder()
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .totalStudents(totalStudents)
                .maxStudents(classRoom.getMaxStudents())
                .build();
    }
}