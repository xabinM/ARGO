package com.argo.backend.organization.message;

public enum ResponseMessage {
    
    // 클래스 관련
    CLASS_LIST_SUCCESS("반 목록 조회 성공"),
    CLASS_DETAIL_SUCCESS("반 상세정보 조회 성공"),
    CLASS_CREATE_SUCCESS("반 생성 성공"),
    CLASS_DELETE_SUCCESS("반 삭제 성공"),
    
    // 위치/장소 관련
    LOCATION_LIST_SUCCESS("지역 목록 조회 성공"),
    SPOT_LIST_SUCCESS("장소 목록 조회 성공"),
    
    // 신청 관련
    APPLICATION_LIST_SUCCESS("신청 목록 조회 성공"),
    APPLICATION_PROCESS_SUCCESS("신청 처리 성공"),
    CLASS_APPLY_SUCCESS("반 신청 성공"),
    CLASS_LEAVE_SUCCESS("반 탈퇴 성공"),
    
    // 학생 관련
    STUDENT_LIST_SUCCESS("학생 목록 조회 성공"),
    
    // 팀 관련
    TEAM_CREATE_SUCCESS("팀 생성 성공"),
    TEAM_ASSIGN_SUCCESS("학생 팀 배정 성공"),
    TEAM_AUTO_ASSIGN_SUCCESS("학생 자동 배정 성공"),
    TEAM_DELETE_SUCCESS("팀 삭제 성공");
    
    private final String message;
    
    ResponseMessage(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}