package com.argo.backend.organization.exception;

public class ClassNotAvailableException extends RuntimeException {
    public ClassNotAvailableException() {
        super(OrganizationErrorMessage.CLASS_NOT_AVAILABLE.getMessage());
    }
}