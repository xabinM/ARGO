package com.argo.backend.organization.dto.classdelete;

import lombok.Getter;

@Getter
public class DeletedApplicationsDto {
    
    private int count;
    private int pendingCount;
    private int approvedCount;
    private int rejectedCount;
    
    public DeletedApplicationsDto(int count, int pendingCount, int approvedCount, int rejectedCount) {
        this.count = count;
        this.pendingCount = pendingCount;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
    }
    
    public static DeletedApplicationsDto of(Object[] statistics) {
        if (statistics == null || statistics.length < 4) {
            return new DeletedApplicationsDto(0, 0, 0, 0);
        }
        
        // 실제 통계 쿼리 결과는 배열 내부에 배열로 반환됨
        Object[] actualStats = statistics;
        if (statistics.length == 1 && statistics[0] instanceof Object[]) {
            actualStats = (Object[]) statistics[0];
        }
        
        long total = actualStats[0] != null ? ((Number) actualStats[0]).longValue() : 0;
        long pending = actualStats[1] != null ? ((Number) actualStats[1]).longValue() : 0;
        long approved = actualStats[2] != null ? ((Number) actualStats[2]).longValue() : 0;
        long rejected = actualStats[3] != null ? ((Number) actualStats[3]).longValue() : 0;
        
        return new DeletedApplicationsDto(
                (int) total,
                (int) pending,
                (int) approved,
                (int) rejected
        );
    }
}