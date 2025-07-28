package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AlreadyWithdrawUserException extends BusinessException {

    private static final String FAIL_CODE = "4005";

    public AlreadyWithdrawUserException() {
        super(FAIL_CODE, HttpStatus.NOT_FOUND);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.ALREADY_WITHDRAW_USER.getMessage();
    }
}
