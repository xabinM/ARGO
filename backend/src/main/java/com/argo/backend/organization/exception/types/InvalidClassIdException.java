package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class InvalidClassIdException extends RuntimeException {
    
    public InvalidClassIdException() {
        super(OrganizationErrorMessage.INVALID_CLASS_ID.getMessage());
    }
}