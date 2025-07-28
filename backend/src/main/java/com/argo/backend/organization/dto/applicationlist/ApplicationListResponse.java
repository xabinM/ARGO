package com.argo.backend.organization.dto.applicationlist;

import com.argo.backend.organization.dto.classroomlist.PaginationResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApplicationListResponse {
    private final ApplicationClassInfo classInfo;
    private final List<ApplicationDto> applications;
    private final ApplicationStatistics statistics;
    private final PaginationResponseDto pagination;

    public static ApplicationListResponse of(
            ApplicationClassInfo classInfo,
            List<ApplicationDto> applications,
            ApplicationStatistics statistics,
            PaginationResponseDto pagination) {
        return ApplicationListResponse.builder()
                .classInfo(classInfo)
                .applications(applications)
                .statistics(statistics)
                .pagination(pagination)
                .build();
    }
}