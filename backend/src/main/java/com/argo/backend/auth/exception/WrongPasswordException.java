package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;

public class WrongPasswordException extends RuntimeException{
    public WrongPasswordException() {
        super(ResponseMessage.PASSWORD_NOT_MATCH_EXCEPTION.getMessage());
    }
}
