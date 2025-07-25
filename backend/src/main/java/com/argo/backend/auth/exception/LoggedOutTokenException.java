package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;
import org.springframework.security.core.AuthenticationException;

public class LoggedOutTokenException extends AuthenticationException {
    public LoggedOutTokenException() {
        super(ResponseMessage.LOGGED_OUT_TOKEN.getMessage());
    }
}
