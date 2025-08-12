package com.argo.backend.organization.exception;

import com.argo.backend.global.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.argo.backend.organization")
public class OrganizationExceptionHandler {

    @ExceptionHandler(OrganizationBusinessException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationBusinessException(OrganizationBusinessException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getCode(), e.getMessage());
        return new ResponseEntity<>(errorResponse, e.getHttpStatus());
    }
}