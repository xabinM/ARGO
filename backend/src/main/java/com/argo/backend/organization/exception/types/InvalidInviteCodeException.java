package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class InvalidInviteCodeException extends OrganizationBusinessException {
    public InvalidInviteCodeException() {
        super(OrganizationErrorMessage.INVALID_INVITE_CODE, HttpStatus.BAD_REQUEST);
    }
}