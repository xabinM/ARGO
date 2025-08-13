package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class DuplicateTeamNameException extends OrganizationBusinessException {
    public DuplicateTeamNameException() {
        super(OrganizationErrorMessage.DUPLICATE_TEAM_NAME, HttpStatus.CONFLICT);
    }
}