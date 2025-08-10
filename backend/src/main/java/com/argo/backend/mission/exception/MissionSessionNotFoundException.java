package com.argo.backend.mission.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class MissionSessionNotFoundException extends BusinessException {

    private static final String FAIL_CODE = "5005";

    public MissionSessionNotFoundException() {
        super(FAIL_CODE, HttpStatus.NOT_FOUND);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.NOT_FOUND_MISSION_SESSION.getMessage();
    }
}
