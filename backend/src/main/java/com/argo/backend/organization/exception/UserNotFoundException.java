package com.argo.backend.organization.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super(OrganizationErrorMessage.USER_NOT_FOUND.getMessage());
    }
}