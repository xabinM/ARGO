package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;
import org.springframework.http.HttpStatus;

public class InvalidClaimTypeException extends AuthorizationException{

    private static final String FAIL_CODE = "4008";

    public InvalidClaimTypeException() {
        super(FAIL_CODE, HttpStatus.UNAUTHORIZED);
    }

    public String getMessage() {
        return ResponseMessage.INVALID_CLAIM_TYPE.getMessage();
    }
}
