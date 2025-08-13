package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class TeamCapacityExceededException extends OrganizationBusinessException {
    public TeamCapacityExceededException() {
        super(OrganizationErrorMessage.TEAM_CAPACITY_EXCEEDED, HttpStatus.BAD_REQUEST);
    }
}