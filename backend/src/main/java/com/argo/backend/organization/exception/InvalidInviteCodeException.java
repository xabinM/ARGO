package com.argo.backend.organization.exception;

public class InvalidInviteCodeException extends RuntimeException {
    public InvalidInviteCodeException() {
        super(OrganizationErrorMessage.INVALID_INVITE_CODE.getMessage());
    }
}