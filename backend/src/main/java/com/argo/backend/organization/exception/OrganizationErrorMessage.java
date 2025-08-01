package com.argo.backend.organization.exception;

import lombok.Getter;

@Getter
public enum OrganizationErrorMessage {
    // User 관련
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    ACCESS_DENIED("권한이 없습니다."),
    INSUFFICIENT_PERMISSION("선생님만 반을 생성할 수 있습니다."),
    
    // Location 관련
    LOCATION_NOT_FOUND("존재하지 않는 활동 장소입니다."),
    
    // ClassRoom 관련
    INVITE_CODE_GENERATION_FAILED("초대 코드 생성에 실패했습니다."),
    CLASS_CREATION_FAILED("반 생성에 실패했습니다."),
    
    // System
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

    private final String message;

    OrganizationErrorMessage(String message) {
        this.message = message;
    }
}