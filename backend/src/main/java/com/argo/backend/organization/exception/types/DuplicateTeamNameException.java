package com.argo.backend.organization.exception.types;

import static com.argo.backend.organization.exception.OrganizationErrorMessage.DUPLICATE_TEAM_NAME;

public class DuplicateTeamNameException extends RuntimeException {
    public DuplicateTeamNameException() {
        super(DUPLICATE_TEAM_NAME.getMessage());
    }
}