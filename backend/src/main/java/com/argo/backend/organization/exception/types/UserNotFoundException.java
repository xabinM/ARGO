package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super(OrganizationErrorMessage.USER_NOT_FOUND.getMessage());
    }
}