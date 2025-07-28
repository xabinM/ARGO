package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;
import org.springframework.http.HttpStatus;

public class ExpiredTokenException extends AuthorizationException {

    private static final String FAIL_CODE = "4006";

    public ExpiredTokenException() {
        super(FAIL_CODE, HttpStatus.FORBIDDEN);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.EXPIRED_TOKEN.getMessage();
    }
}
