package com.argo.backend.organization.dto.applicationlist;

public record StatisticsDto(
    Integer totalApplications,
    Integer pendingCount,
    Integer approvedCount,
    Integer rejectedCount
) {
    public static StatisticsDto of(Long totalApplications, Long pendingCount, Long approvedCount, Long rejectedCount) {
        return new StatisticsDto(
                totalApplications.intValue(),
                pendingCount.intValue(),
                approvedCount.intValue(),
                rejectedCount.intValue()
        );
    }
}