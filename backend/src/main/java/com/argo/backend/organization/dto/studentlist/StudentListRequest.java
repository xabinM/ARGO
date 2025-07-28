package com.argo.backend.organization.dto.studentlist;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentListRequest {
    private final String status; // 반에 배정 되었는지 !
    private final int page;
    private final int size;

    public boolean isValidStatus() {
        if (status == null) return true;
        return "all".equals(status) || 
               "assigned".equals(status) || 
               "unassigned".equals(status) || 
               status.startsWith("team-");
    }

    public String getStatusWithDefault() {
        return status != null ? status : "all";
    }

    public int getPageWithDefault() {
        return page > 0 ? page : 1;
    }

    public int getSizeWithDefault() {
        return (size > 0 && size <= 100) ? size : 20;
    }

    public boolean isTeamFilter() {
        return status != null && status.startsWith("team-");
    }

    public Long getTeamIdFromFilter() {
        if (!isTeamFilter()) return null;
        try {
            return Long.parseLong(status.substring(5)); // "team-" 제거 후 숫자 추출
        } catch (NumberFormatException e) {
            return null;
        }
    }
}