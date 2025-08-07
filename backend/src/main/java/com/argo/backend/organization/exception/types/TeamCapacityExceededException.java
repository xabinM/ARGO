package com.argo.backend.organization.exception.types;

import static com.argo.backend.organization.exception.OrganizationErrorMessage.TEAM_CAPACITY_EXCEEDED;

public class TeamCapacityExceededException extends RuntimeException {
    public TeamCapacityExceededException() {
        super(TEAM_CAPACITY_EXCEEDED.getMessage());
    }
}