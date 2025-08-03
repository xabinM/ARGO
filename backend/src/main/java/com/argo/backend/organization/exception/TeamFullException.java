package com.argo.backend.organization.exception;

public class TeamFullException extends RuntimeException {
    public TeamFullException() {
        super(OrganizationErrorMessage.TEAM_FULL.getMessage());
    }
}