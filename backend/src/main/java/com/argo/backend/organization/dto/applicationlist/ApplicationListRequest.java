package com.argo.backend.organization.dto.applicationlist;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationListRequest {
    private final String status;
    private final int page;
    private final int size;

    // 상태 검증
    public boolean isValidStatus() {
        if (status == null) return true; // null이면 기본값 사용
        return "pending".equals(status) || 
               "approved".equals(status) || 
               "rejected".equals(status) || 
               "all".equals(status);
    }

    // 기본값 적용
    public String getStatusWithDefault() {
        return status != null ? status : "pending";
    }

    public int getPageWithDefault() {
        return page > 0 ? page : 1;
    }

    public int getSizeWithDefault() {
        return (size > 0 && size <= 100) ? size : 10;
    }
}