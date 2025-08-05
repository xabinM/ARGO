package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class CannotDeleteActiveClassException extends RuntimeException {
    public CannotDeleteActiveClassException() {
        super(OrganizationErrorMessage.CANNOT_DELETE_ACTIVE_CLASS.getMessage());
    }
}