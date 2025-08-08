package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class ClassNotFoundException extends RuntimeException {
    public ClassNotFoundException() {
        super(OrganizationErrorMessage.CLASS_NOT_FOUND.getMessage());
    }
}