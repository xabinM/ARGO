package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class TeamFullException extends OrganizationBusinessException {
    public TeamFullException() {
        super(OrganizationErrorMessage.TEAM_FULL, HttpStatus.BAD_REQUEST);
    }
}