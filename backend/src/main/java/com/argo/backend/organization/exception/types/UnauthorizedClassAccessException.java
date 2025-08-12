package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class UnauthorizedClassAccessException extends OrganizationBusinessException {
    public UnauthorizedClassAccessException() {
        super(OrganizationErrorMessage.UNAUTHORIZED_CLASS_ACCESS, HttpStatus.NOT_FOUND);
    }
}