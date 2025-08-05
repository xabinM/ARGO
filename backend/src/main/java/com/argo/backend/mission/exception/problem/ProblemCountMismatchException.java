package com.argo.backend.mission.exception.problem;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProblemCountMismatchException extends BusinessException {

    private static final String FAIL_CODE = "6003";

    public ProblemCountMismatchException() {
        super(FAIL_CODE, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.PROBLEM_COUNT_MISMATCH.getMessage();
    }
}
