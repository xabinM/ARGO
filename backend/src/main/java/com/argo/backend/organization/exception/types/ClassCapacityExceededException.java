package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class ClassCapacityExceededException extends OrganizationBusinessException {
    public ClassCapacityExceededException() {
        super(OrganizationErrorMessage.CLASS_CAPACITY_EXCEEDED, HttpStatus.BAD_REQUEST);
    }
}