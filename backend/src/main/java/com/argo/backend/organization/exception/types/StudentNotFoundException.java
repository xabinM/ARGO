package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class StudentNotFoundException extends OrganizationBusinessException {
    public StudentNotFoundException() {
        super(OrganizationErrorMessage.STUDENT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}