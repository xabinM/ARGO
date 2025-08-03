package com.argo.backend.organization.exception;

public class InvalidClassIdException extends RuntimeException {
    
    public InvalidClassIdException() {
        super(OrganizationErrorMessage.INVALID_CLASS_ID.getMessage());
    }
}