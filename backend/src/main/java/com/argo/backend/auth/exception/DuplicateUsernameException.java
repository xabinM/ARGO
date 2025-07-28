package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicateUsernameException extends BusinessException {

    private static final String FAIL_CODE = "4001";

    public DuplicateUsernameException() {
        super(FAIL_CODE, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.SIGNUP_USERNAME_DUPLICATE_EXCEPTION.getMessage();
    }
}
