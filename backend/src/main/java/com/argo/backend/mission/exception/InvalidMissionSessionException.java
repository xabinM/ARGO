package com.argo.backend.mission.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidMissionSessionException extends BusinessException {

    private static final String FAIL_CODE = "5006";

    public InvalidMissionSessionException() {
        super(FAIL_CODE, HttpStatus.NOT_FOUND);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.INVALID_MISSION_SESSION.getMessage();
    }
}
