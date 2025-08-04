package com.argo.backend.global.enums;

import lombok.Getter;

@Getter
public enum ResponseMessage {
    // Auth
    SIGNUP_SUCCESS("회원 가입에 성공하였습니다."),
    LOGIN_SUCCESS("로그인에 성공했습니다."),
    WITHDRAW_SUCCESS("회원 탈퇴되었습니다."),
    SUCCESS_LOGOUT("로그아웃 되었습니다."),

    USER_NOT_FOUND_EXCEPTION("사용자를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH_EXCEPTION("비밀번호가 일치하지 않습니다."),
    SIGNUP_USERNAME_DUPLICATE_EXCEPTION("이미 존재하는 아이디입니다."),
    EXPIRED_TOKEN("만료된 토큰입니다."),
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    LOGGED_OUT_TOKEN("로그 아웃된 토큰입니다."),
    INVALID_CLAIM_TYPE("토큰에 클레임이 없거나 형식이 올바르지 않습니다."),
    ALREADY_WITHDRAW_USER("이미 탈퇴한 회원입니다."),

    // Parameter
    WRONG_REQUEST_PARAMETER("요청 파라미터가 잘못되었습니다."),

    // mission
    SUCCESS_CREATE_MISSION("미션 생성에 성공하였습니다."),

    TEAM_NOT_FOUND("해당 팀이 존재하지 않습니다."),
    SPOT_NOT_FOUND("해당 스팟이 존재하지 않습니다."),
    PROBLEM_NOT_FOUND("문제가 존재하지 않습니다."),


    // problem
    SUCCESS_REGISTER_PROBLEM("문제가 성공적으로 등록되었습니다."),
    ;

    private final String message;

    ResponseMessage(String message) {
        this.message = message;
    }
}
