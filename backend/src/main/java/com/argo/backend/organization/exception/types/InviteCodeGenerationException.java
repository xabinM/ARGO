package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class InviteCodeGenerationException extends RuntimeException {
    public InviteCodeGenerationException() {
        super(OrganizationErrorMessage.INVITE_CODE_GENERATION_FAILED.getMessage());
    }
}