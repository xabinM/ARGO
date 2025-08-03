package com.argo.backend.organization.exception;

public class NotParticipatingClassException extends RuntimeException {
    public NotParticipatingClassException() {
        super(OrganizationErrorMessage.NOT_PARTICIPATING_CLASS.getMessage());
    }
}