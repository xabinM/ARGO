package com.argo.backend.organization.exception.types;

import static com.argo.backend.organization.exception.OrganizationErrorMessage.CLASS_CAPACITY_EXCEEDED;

public class ClassCapacityExceededException extends RuntimeException {
    public ClassCapacityExceededException() {
        super(CLASS_CAPACITY_EXCEEDED.getMessage());
    }
}