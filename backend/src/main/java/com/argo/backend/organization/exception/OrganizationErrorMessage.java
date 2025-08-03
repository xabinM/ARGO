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
    INVALID_INVITE_CODE("유효하지 않은 초대 코드입니다."),
    STUDENT_ONLY_ALLOWED("학생만 반에 신청할 수 있습니다."),
    CLASS_NOT_AVAILABLE("현재 신청할 수 없는 반입니다."),
    DUPLICATE_APPLICATION("이미 신청했거나 참여 중인 반입니다."),
    CLASS_NOT_FOUND("존재하지 않는 반입니다."),
    UNAUTHORIZED_CLASS_ACCESS("해당 반에 접근할 권한이 없습니다."),
    CLASS_CAPACITY_EXCEEDED("반 정원을 초과하여 승인할 수 없습니다."),
    
    // Application 관련
    APPLICATION_NOT_FOUND("존재하지 않는 신청입니다."),
    APPLICATION_ALREADY_PROCESSED("이미 처리된 신청입니다."),
    
    // Team 관련
    DUPLICATE_TEAM_NAME("이미 동일한 이름의 팀이 존재합니다."),
    TEAM_CREATION_FAILED("팀 생성에 실패했습니다."),
    TEAM_NOT_FOUND("존재하지 않는 팀입니다."),
    TEAM_CAPACITY_EXCEEDED("팀의 최대 인원을 초과합니다."),
    
    // Student 관련
    STUDENT_NOT_FOUND("해당 반에 참여하지 않은 학생입니다."),
    STUDENT_ALREADY_ASSIGNED("이미 다른 팀에 속한 학생이 포함되어 있습니다."),
    
    // Team Auto Assignment 관련
    TEAM_FULL("모든 팀이 가득 차서 배정할 수 없습니다."),
    
    // System
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

    private final String message;

    OrganizationErrorMessage(String message) {
        this.message = message;
    }
}