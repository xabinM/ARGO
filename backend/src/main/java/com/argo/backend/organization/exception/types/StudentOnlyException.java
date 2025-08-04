package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationErrorMessage;

public class StudentOnlyException extends RuntimeException {
    public StudentOnlyException() {
        super(OrganizationErrorMessage.STUDENT_ONLY_ALLOWED.getMessage());
    }
}