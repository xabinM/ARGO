package com.argo.backend.mission.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProblemGenerationFailedException extends BusinessException {

    private static final String FAIL_CODE = "6002";

    public ProblemGenerationFailedException() {
        super(FAIL_CODE, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.PROBLEM_GENERATION_FAILED.getMessage();
    }
}
