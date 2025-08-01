package com.argo.backend.organization.exception;

public class StudentOnlyException extends RuntimeException {
    public StudentOnlyException() {
        super(OrganizationErrorMessage.STUDENT_ONLY_ALLOWED.getMessage());
    }
}