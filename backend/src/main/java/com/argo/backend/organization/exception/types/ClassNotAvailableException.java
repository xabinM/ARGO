package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class ClassNotAvailableException extends RuntimeException {
    public ClassNotAvailableException() {
        super(OrganizationErrorMessage.CLASS_NOT_AVAILABLE.getMessage());
    }
}