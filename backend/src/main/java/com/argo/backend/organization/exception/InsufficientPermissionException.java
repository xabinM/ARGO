package com.argo.backend.organization.exception;

public class InsufficientPermissionException extends RuntimeException {
    public InsufficientPermissionException() {
        super(OrganizationErrorMessage.INSUFFICIENT_PERMISSION.getMessage());
    }
}