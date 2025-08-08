package com.argo.backend.organization.exception.types;

import static com.argo.backend.organization.exception.OrganizationErrorMessage.APPLICATION_NOT_FOUND;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException() {
        super(APPLICATION_NOT_FOUND.getMessage());
    }
}