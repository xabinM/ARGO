package com.argo.backend.organization.exception.types;

import com.argo.backend.organization.exception.OrganizationBusinessException;
import com.argo.backend.organization.exception.OrganizationErrorMessage;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends OrganizationBusinessException {
    public UserNotFoundException() {
        super(OrganizationErrorMessage.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}