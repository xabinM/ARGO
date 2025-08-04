package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException() {
        super(OrganizationErrorMessage.LOCATION_NOT_FOUND.getMessage());
    }
}