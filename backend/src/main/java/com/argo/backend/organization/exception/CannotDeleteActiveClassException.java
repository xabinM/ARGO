package com.argo.backend.organization.exception;

public class CannotDeleteActiveClassException extends RuntimeException {
    public CannotDeleteActiveClassException() {
        super(OrganizationErrorMessage.CANNOT_DELETE_ACTIVE_CLASS.getMessage());
    }
}