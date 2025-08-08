package com.argo.backend.organization.exception.types;

import static com.argo.backend.organization.exception.OrganizationErrorMessage.TEAM_NOT_FOUND;

public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException() {
        super(TEAM_NOT_FOUND.getMessage());
    }
}