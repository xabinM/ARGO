package com.argo.backend.global.enums;

import lombok.Getter;

@Getter
public enum ResponseMessage {
    // Auth
    SIGNUP_SUCCESS("회원 가입에 성공하였습니다."),
    LOGIN_SUCCESS("로그인에 성공했습니다."),

    MEMBER_NOT_FOUND_EXCEPTION("사용자를 찾을 수 없습니다."),
    PASSWORD_NOT_MATCH_EXCEPTION("비밀번호가 일치하지 않습니다."),
    SIGNUP_USERNAME_DUPLICATE_EXCEPTION("이미 존재하는 아이디입니다.")
    ;

    private final String message;

    ResponseMessage(String message) {
        this.message = message;
    }
}
