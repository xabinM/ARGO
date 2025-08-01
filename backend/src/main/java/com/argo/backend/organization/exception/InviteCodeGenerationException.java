package com.argo.backend.organization.exception;

public class InviteCodeGenerationException extends RuntimeException {
    public InviteCodeGenerationException() {
        super(OrganizationErrorMessage.INVITE_CODE_GENERATION_FAILED.getMessage());
    }
}