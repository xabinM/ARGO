package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class ApplicationNotFoundException extends OrganizationBusinessException {
    public ApplicationNotFoundException() {
        super(OrganizationErrorMessage.APPLICATION_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}