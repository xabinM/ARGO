package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class InvalidPageParameterException extends OrganizationBusinessException {
    
    public InvalidPageParameterException() {
        super(OrganizationErrorMessage.INVALID_PAGE_PARAMETER, HttpStatus.BAD_REQUEST);
    }
}