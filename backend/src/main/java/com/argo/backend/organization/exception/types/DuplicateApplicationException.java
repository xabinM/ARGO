package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class DuplicateApplicationException extends RuntimeException {
    public DuplicateApplicationException() {
        super(OrganizationErrorMessage.DUPLICATE_APPLICATION.getMessage());
    }
}