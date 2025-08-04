package com.argo.backend.mission.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class TeamNotFoundException extends BusinessException {

    private static final String FAIL_CODE = "5001";

    public TeamNotFoundException() {
        super(FAIL_CODE, HttpStatus.NOT_FOUND);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.TEAM_NOT_FOUND.getMessage();
    }
}
