package com.argo.backend.mission.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProblemNotFoundException extends BusinessException {

    private static final String FAIL_CODE = "4103";

    public ProblemNotFoundException() {
        super(FAIL_CODE, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.PROBLEM_NOT_FOUND.getMessage();
    }
}
