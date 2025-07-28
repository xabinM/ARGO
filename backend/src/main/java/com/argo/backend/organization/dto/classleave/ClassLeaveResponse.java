package com.argo.backend.organization.dto.classleave;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassLeaveResponse {
    private final LeftClassInfo leftClass;
    private final StudentLeaveInfo studentInfo;
    private final TeamLeaveInfo teamInfo;
    private final ClassStatusInfo classStatus;

    public static ClassLeaveResponse of(
            LeftClassInfo leftClass,
            StudentLeaveInfo studentInfo,
            TeamLeaveInfo teamInfo,
            ClassStatusInfo classStatus
    ) {
        return ClassLeaveResponse.builder()
                .leftClass(leftClass)
                .studentInfo(studentInfo)
                .teamInfo(teamInfo)
                .classStatus(classStatus)
                .build();
    }
}