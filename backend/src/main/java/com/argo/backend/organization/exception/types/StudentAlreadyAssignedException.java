package com.argo.backend.organization.exception.types;

import static com.argo.backend.organization.exception.OrganizationErrorMessage.STUDENT_ALREADY_ASSIGNED;

public class StudentAlreadyAssignedException extends RuntimeException {
    public StudentAlreadyAssignedException() {
        super(STUDENT_ALREADY_ASSIGNED.getMessage());
    }
}