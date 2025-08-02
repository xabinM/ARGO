package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AuthorizationException {

    private static final String FAIL_CODE = "4007";

    public InvalidTokenException() {
        super(FAIL_CODE, HttpStatus.FORBIDDEN);
    }

    public String getMessage() {
        return ResponseMessage.INVALID_TOKEN.getMessage();
    }
}
