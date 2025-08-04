package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class UnauthorizedClassAccessException extends RuntimeException {
    public UnauthorizedClassAccessException() {
        super(OrganizationErrorMessage.UNAUTHORIZED_CLASS_ACCESS.getMessage());
    }
}