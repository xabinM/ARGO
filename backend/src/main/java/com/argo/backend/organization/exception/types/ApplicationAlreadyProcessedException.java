package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class ApplicationAlreadyProcessedException extends OrganizationBusinessException {
    public ApplicationAlreadyProcessedException() {
        super(OrganizationErrorMessage.APPLICATION_ALREADY_PROCESSED, HttpStatus.CONFLICT);
    }
}