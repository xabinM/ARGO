package com.argo.backend.global.exception;

import com.argo.backend.auth.exception.AuthorizationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse error = new ErrorResponse(e.getCode(), e.getMessage());

        return new ResponseEntity<>(error, e.getHttpStatus());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(AuthorizationException e) {
        ErrorResponse error = new ErrorResponse(e.getCode(), e.getMessage());

        return new ResponseEntity<>(error, e.getHttpStatus());
    }
}
