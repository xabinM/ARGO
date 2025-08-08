package com.argo.backend.organization.exception.types;

import static com.argo.backend.organization.exception.OrganizationErrorMessage.STUDENT_NOT_FOUND;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException() {
        super(STUDENT_NOT_FOUND.getMessage());
    }
}