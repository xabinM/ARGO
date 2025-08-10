package com.argo.backend.mission.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class MissionAlreadyCompletedException extends BusinessException {

    private static final String FAIL_CODE = "5007";

    public MissionAlreadyCompletedException() {
        super(FAIL_CODE, HttpStatus.NOT_FOUND);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.ALREADY_COMPLETED_MISSION.getMessage();
    }
}
