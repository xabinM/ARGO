package com.argo.backend.organization.dto.applicationprocess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationProcessRequest {
    private String action;

    public boolean isValidAction() {
        return "approve".equals(action) || "reject".equals(action);
    }

    public boolean isApprove() {
        return "approve".equals(action);
    }

    public boolean isReject() {
        return "reject".equals(action);
    }
}