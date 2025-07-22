package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() {
        super(ResponseMessage.SIGNUP_USERNAME_DUPLICATE_EXCEPTION.getMessage());
    }
}
