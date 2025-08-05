package com.argo.backend.mission.exception.problem;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProblemTypeNotExist extends BusinessException {

    private static final String FAIL_CODE = "6004";

    public ProblemTypeNotExist() {
        super(FAIL_CODE, HttpStatus.NOT_FOUND);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.PROBLEM_TYPE_NOT_EXIST.getMessage();
    }
}
