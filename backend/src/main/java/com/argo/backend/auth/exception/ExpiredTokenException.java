package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;

public class ExpiredTokenException extends RuntimeException{
    public ExpiredTokenException() {
        super(ResponseMessage.EXPIRED_TOKEN.getMessage());
    }
}
