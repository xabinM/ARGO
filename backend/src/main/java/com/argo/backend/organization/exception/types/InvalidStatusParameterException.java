package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class InvalidStatusParameterException extends RuntimeException {
    
    public InvalidStatusParameterException() {
        super(OrganizationErrorMessage.INVALID_STATUS_PARAMETER.getMessage());
    }
}