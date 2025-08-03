package com.argo.backend.organization.exception;

public class ActivityInProgressException extends RuntimeException {
    public ActivityInProgressException() {
        super(OrganizationErrorMessage.ACTIVITY_IN_PROGRESS.getMessage());
    }
}