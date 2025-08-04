package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class ActivityInProgressException extends RuntimeException {
    public ActivityInProgressException() {
        super(OrganizationErrorMessage.ACTIVITY_IN_PROGRESS.getMessage());
    }
}