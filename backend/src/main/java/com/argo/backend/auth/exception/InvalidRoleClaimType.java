package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;

public class InvalidRoleClaimType extends RuntimeException{
    public InvalidRoleClaimType() {
        super(ResponseMessage.INVALID_ROLE_CLAIM_TYPE.getMessage());
    }
}
