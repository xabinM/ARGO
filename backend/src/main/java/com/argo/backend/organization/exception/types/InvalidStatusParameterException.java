package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class InvalidStatusParameterException extends OrganizationBusinessException {
    
    public InvalidStatusParameterException() {
        super(OrganizationErrorMessage.INVALID_STATUS_PARAMETER, HttpStatus.BAD_REQUEST);
    }
}