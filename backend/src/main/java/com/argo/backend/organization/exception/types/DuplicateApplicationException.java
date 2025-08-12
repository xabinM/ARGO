package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class DuplicateApplicationException extends OrganizationBusinessException {
    public DuplicateApplicationException() {
        super(OrganizationErrorMessage.DUPLICATE_APPLICATION, HttpStatus.CONFLICT);
    }
}