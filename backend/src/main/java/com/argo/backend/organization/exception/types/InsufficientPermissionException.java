package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class InsufficientPermissionException extends RuntimeException {
    public InsufficientPermissionException() {
        super(OrganizationErrorMessage.INSUFFICIENT_PERMISSION.getMessage());
    }
}