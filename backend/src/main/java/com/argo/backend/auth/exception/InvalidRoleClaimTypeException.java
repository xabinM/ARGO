package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;
import org.springframework.http.HttpStatus;

public class InvalidRoleClaimType extends AuthorizationException{

    private static final String FAIL_CODE = "4008";

    public InvalidRoleClaimType() {
        super(FAIL_CODE, HttpStatus.UNAUTHORIZED);
    }

    public String getMessage() {
        return ResponseMessage.INVALID_ROLE_CLAIM_TYPE.getMessage();
    }
}
