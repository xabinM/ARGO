package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException() {
        super(ResponseMessage.INVALID_TOKEN.getMessage());
    }
}
