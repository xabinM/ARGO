package com.argo.backend.organization.exception;

public class InvalidStatusParameterException extends RuntimeException {
    
    public InvalidStatusParameterException() {
        super(OrganizationErrorMessage.INVALID_STATUS_PARAMETER.getMessage());
    }
}