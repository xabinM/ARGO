package com.argo.backend.mission.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class PythonServerNoResponseException extends BusinessException {

    private static final String FAIL_CODE = "6001";

    public PythonServerNoResponseException() {
        super(FAIL_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.PYTHON_SERVER_NO_RESPONSE.getMessage();
    }
}
