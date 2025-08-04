package com.argo.backend.organization.exception.types;

import static com.argo.backend.organization.exception.OrganizationErrorMessage.APPLICATION_ALREADY_PROCESSED;

public class ApplicationAlreadyProcessedException extends RuntimeException {
    public ApplicationAlreadyProcessedException() {
        super(APPLICATION_ALREADY_PROCESSED.getMessage());
    }
}