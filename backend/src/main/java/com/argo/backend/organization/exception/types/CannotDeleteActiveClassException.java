package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class CannotDeleteActiveClassException extends OrganizationBusinessException {
    public CannotDeleteActiveClassException() {
        super(OrganizationErrorMessage.CANNOT_DELETE_ACTIVE_CLASS, HttpStatus.CONFLICT);
    }
}