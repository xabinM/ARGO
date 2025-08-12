package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class ActivityInProgressException extends OrganizationBusinessException {
    public ActivityInProgressException() {
        super(OrganizationErrorMessage.ACTIVITY_IN_PROGRESS, HttpStatus.CONFLICT);
    }
}