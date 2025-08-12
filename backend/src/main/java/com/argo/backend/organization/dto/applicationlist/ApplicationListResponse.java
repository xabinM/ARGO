package com.argo.backend.organization.dto.applicationlist;

import java.util.List;

public record ApplicationListResponse(
    ClassInfoDto classInfo,
    List<ApplicationDto> applications,
    ApplicationStatisticsDto statistics,
    PaginationDto pagination
) {
}