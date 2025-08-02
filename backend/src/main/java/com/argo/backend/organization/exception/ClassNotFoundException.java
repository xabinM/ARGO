package com.argo.backend.organization.exception;

public class ClassNotFoundException extends RuntimeException {
    public ClassNotFoundException() {
        super(OrganizationErrorMessage.CLASS_NOT_FOUND.getMessage());
    }
}