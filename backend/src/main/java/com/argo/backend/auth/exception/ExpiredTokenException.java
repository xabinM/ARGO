package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;
import org.springframework.security.core.AuthenticationException;

public class ExpiredTokenException extends AuthenticationException {
    public ExpiredTokenException() {
        super(ResponseMessage.EXPIRED_TOKEN.getMessage());
    }
}
