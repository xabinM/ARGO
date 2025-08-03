package com.argo.backend.organization.exception;

import static com.argo.backend.organization.exception.OrganizationErrorMessage.TEAM_CREATION_FAILED;

public class TeamCreationFailedException extends RuntimeException {
    public TeamCreationFailedException() {
        super(TEAM_CREATION_FAILED.getMessage());
    }
}