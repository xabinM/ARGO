package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class InvalidClassIdException extends OrganizationBusinessException {
    
    public InvalidClassIdException() {
        super(OrganizationErrorMessage.INVALID_CLASS_ID, HttpStatus.BAD_REQUEST);
    }
}