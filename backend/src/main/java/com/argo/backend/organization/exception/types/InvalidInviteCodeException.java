package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class InvalidInviteCodeException extends RuntimeException {
    public InvalidInviteCodeException() {
        super(OrganizationErrorMessage.INVALID_INVITE_CODE.getMessage());
    }
}