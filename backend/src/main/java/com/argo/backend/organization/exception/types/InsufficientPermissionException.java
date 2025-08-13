package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class InsufficientPermissionException extends OrganizationBusinessException {
    public InsufficientPermissionException() {
        super(OrganizationErrorMessage.INSUFFICIENT_PERMISSION, HttpStatus.FORBIDDEN);
    }
}