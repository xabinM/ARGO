package com.argo.backend.organization.exception;

public class InvalidPageParameterException extends RuntimeException {
    
    public InvalidPageParameterException() {
        super(OrganizationErrorMessage.INVALID_PAGE_PARAMETER.getMessage());
    }
}