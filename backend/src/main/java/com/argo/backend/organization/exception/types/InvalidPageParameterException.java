package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class InvalidPageParameterException extends RuntimeException {
    
    public InvalidPageParameterException() {
        super(OrganizationErrorMessage.INVALID_PAGE_PARAMETER.getMessage());
    }
}