package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class NotParticipatingClassException extends OrganizationBusinessException {
    public NotParticipatingClassException() {
        super(OrganizationErrorMessage.NOT_PARTICIPATING_CLASS, HttpStatus.CONFLICT);
    }
}