package com.argo.backend.organization.dto.classapply;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassApplyRequest {
    private final String inviteCode;

    public ClassApplyRequest(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    // 기본 생성자 (Jackson 역직렬화용)
    public ClassApplyRequest() {
        this.inviteCode = null;
    }

    // 유효성 검증
    public boolean isValid() {
        return inviteCode != null && !inviteCode.trim().isEmpty();
    }
}