package com.argo.backend.organization.dto.applicationlist;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.user.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationClassInfo {
    private final Long classId;
    private final String className;
    private final Integer currentStudents;
    private final Integer maxStudents;

    public static ApplicationClassInfo from(ClassRoom classRoom) {
        // 승인된 학생 수 계산
        int currentStudents = (int) classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .count();

        return ApplicationClassInfo.builder()
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .currentStudents(currentStudents)
                .maxStudents(classRoom.getMaxStudents())
                .build();
    }
}