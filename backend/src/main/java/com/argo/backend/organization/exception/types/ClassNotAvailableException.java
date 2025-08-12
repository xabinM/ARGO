package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class ClassNotAvailableException extends OrganizationBusinessException {
    public ClassNotAvailableException() {
        super(OrganizationErrorMessage.CLASS_NOT_AVAILABLE, HttpStatus.CONFLICT);
    }
}