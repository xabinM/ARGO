package com.argo.backend.organization.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException() {
        super(OrganizationErrorMessage.LOCATION_NOT_FOUND.getMessage());
    }
}