package com.argo.backend.organization.exception;

public class UnauthorizedClassAccessException extends RuntimeException {
    public UnauthorizedClassAccessException() {
        super(OrganizationErrorMessage.UNAUTHORIZED_CLASS_ACCESS.getMessage());
    }
}