package com.argo.backend.organization.exception;

import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OrganizationBusinessException extends BusinessException {
    
    public OrganizationBusinessException(OrganizationErrorMessage errorMessage, HttpStatus httpStatus) {
        super(errorMessage.getCode(), httpStatus);
    }
    
    @Override
    public String getMessage() {
        return OrganizationErrorMessage.findByCode(super.getCode()).getMessage();
    }
}