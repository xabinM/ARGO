package com.argo.backend.auth.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() {
        super("이미 존재하는 사용자 이름입니다.");
    }
}
