package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class InviteCodeGenerationException extends OrganizationBusinessException {
    public InviteCodeGenerationException() {
        super(OrganizationErrorMessage.INVITE_CODE_GENERATION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}