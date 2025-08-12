package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class StudentOnlyException extends OrganizationBusinessException {
    public StudentOnlyException() {
        super(OrganizationErrorMessage.STUDENT_ONLY_ALLOWED, HttpStatus.FORBIDDEN);
    }
}