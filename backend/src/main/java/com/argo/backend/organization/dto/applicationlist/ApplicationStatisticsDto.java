package com.argo.backend.organization.dto.applicationlist;

public record ApplicationStatisticsDto(
    Integer totalApplications,
    Integer pendingCount,
    Integer approvedCount,
    Integer rejectedCount
) {
    public static ApplicationStatisticsDto of(Long totalApplications, Long pendingCount, Long approvedCount, Long rejectedCount) {
        return new ApplicationStatisticsDto(
                totalApplications.intValue(),
                pendingCount.intValue(),
                approvedCount.intValue(),
                rejectedCount.intValue()
        );
    }
}