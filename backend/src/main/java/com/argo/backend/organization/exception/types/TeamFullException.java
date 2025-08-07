package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class TeamFullException extends RuntimeException {
    public TeamFullException() {
        super(OrganizationErrorMessage.TEAM_FULL.getMessage());
    }
}