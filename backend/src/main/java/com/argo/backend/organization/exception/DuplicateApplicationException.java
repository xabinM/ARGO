package com.argo.backend.organization.exception;

public class DuplicateApplicationException extends RuntimeException {
    public DuplicateApplicationException() {
        super(OrganizationErrorMessage.DUPLICATE_APPLICATION.getMessage());
    }
}