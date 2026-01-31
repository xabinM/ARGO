package com.argo.backend.mission.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AlreadyProgressedMissionException extends BusinessException {

    private static final String FAIL_CODE = "5006";

    public AlreadyProgressedMissionException() {
        super(FAIL_CODE, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.ALREADY_PROGRESSED_MISSION.getMessage();
    }
}
