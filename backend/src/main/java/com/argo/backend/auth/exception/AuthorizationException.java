package com.argo.backend.auth.exception;

import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AuthorizationException extends BusinessException {
    public AuthorizationException(String code, HttpStatus httpStatus) {
        super(code, httpStatus);
    }
}
