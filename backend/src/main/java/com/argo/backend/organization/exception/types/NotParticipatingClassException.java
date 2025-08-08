package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class NotParticipatingClassException extends RuntimeException {
    public NotParticipatingClassException() {
        super(OrganizationErrorMessage.NOT_PARTICIPATING_CLASS.getMessage());
    }
}