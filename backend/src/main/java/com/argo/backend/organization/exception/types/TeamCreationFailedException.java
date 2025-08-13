package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class TeamCreationFailedException extends OrganizationBusinessException {
    public TeamCreationFailedException() {
        super(OrganizationErrorMessage.TEAM_CREATION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}