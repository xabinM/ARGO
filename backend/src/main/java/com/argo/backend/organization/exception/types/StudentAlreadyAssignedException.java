package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class StudentAlreadyAssignedException extends OrganizationBusinessException {
    public StudentAlreadyAssignedException() {
        super(OrganizationErrorMessage.STUDENT_ALREADY_ASSIGNED, HttpStatus.CONFLICT);
    }
}