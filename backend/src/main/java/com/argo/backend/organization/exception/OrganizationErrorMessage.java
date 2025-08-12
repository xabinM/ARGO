package com.argo.backend.organization.exception;

import lombok.Getter;

@Getter
public enum OrganizationErrorMessage {
    // User 관련
    USER_NOT_FOUND("5001", "존재하지 않는 사용자입니다."),
    ACCESS_DENIED("5002", "권한이 없습니다."),
    INSUFFICIENT_PERMISSION("5003", "선생님만 반을 생성할 수 있습니다."),
    
    // Location 관련
    LOCATION_NOT_FOUND("5004", "존재하지 않는 활동 장소입니다."),
    
    // ClassRoom 관련
    INVITE_CODE_GENERATION_FAILED("5005", "초대 코드 생성에 실패했습니다."),
    CLASS_CREATION_FAILED("5006", "반 생성에 실패했습니다."),
    INVALID_INVITE_CODE("5007", "유효하지 않은 초대 코드입니다."),
    STUDENT_ONLY_ALLOWED("5008", "학생만 반에 신청할 수 있습니다."),
    CLASS_NOT_AVAILABLE("5009", "현재 신청할 수 없는 반입니다."),
    DUPLICATE_APPLICATION("5010", "이미 신청했거나 참여 중인 반입니다."),
    CLASS_NOT_FOUND("5011", "존재하지 않는 반입니다."),
    UNAUTHORIZED_CLASS_ACCESS("5012", "해당 반에 접근할 권한이 없습니다."),
    CLASS_CAPACITY_EXCEEDED("5013", "반 정원을 초과하여 승인할 수 없습니다."),
    
    // Application 관련
    APPLICATION_NOT_FOUND("5014", "존재하지 않는 신청입니다."),
    APPLICATION_ALREADY_PROCESSED("5015", "이미 처리된 신청입니다."),
    
    // Team 관련
    DUPLICATE_TEAM_NAME("5016", "이미 동일한 이름의 팀이 존재합니다."),
    TEAM_CREATION_FAILED("5017", "팀 생성에 실패했습니다."),
    TEAM_NOT_FOUND("5018", "존재하지 않는 팀입니다."),
    TEAM_CAPACITY_EXCEEDED("5019", "팀의 최대 인원을 초과합니다."),
    
    // Student 관련
    STUDENT_NOT_FOUND("5020", "해당 반에 참여하지 않은 학생입니다."),
    STUDENT_ALREADY_ASSIGNED("5021", "이미 다른 팀에 속한 학생이 포함되어 있습니다."),
    
    // Team Auto Assignment 관련
    TEAM_FULL("5022", "모든 팀이 가득 차서 배정할 수 없습니다."),
    
    // Page 관련
    INVALID_PAGE_PARAMETER("5023", "페이지 번호는 1 이상이어야 합니다."),
    
    // Class Detail 관련
    INVALID_CLASS_ID("5024", "올바르지 않은 반 ID입니다."),
    
    // Student List 관련
    INVALID_STATUS_PARAMETER("5025", "올바르지 않은 상태값입니다. (all, assigned, unassigned 중 선택)"),
    
    // Class Leave 관련
    NOT_PARTICIPATING_CLASS("5026", "참여하지 않은 반입니다."),
    ACTIVITY_IN_PROGRESS("5027", "활동 진행 중에는 탈퇴할 수 없습니다."),
    
    // Class Delete 관련
    CANNOT_DELETE_ACTIVE_CLASS("5028", "활동 진행 중인 반은 삭제할 수 없습니다."),
    
    // System
    INTERNAL_SERVER_ERROR("5029", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String message;

    OrganizationErrorMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public static OrganizationErrorMessage findByCode(String code) {
        for (OrganizationErrorMessage errorMessage : values()) {
            if (errorMessage.getCode().equals(code)) {
                return errorMessage;
            }
        }
        throw new IllegalArgumentException("Unknown error code: " + code);
    }
}