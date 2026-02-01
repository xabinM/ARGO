package com.argo.backend.global.exception;

import com.argo.backend.global.enums.ResponseMessage;
import org.springframework.http.HttpStatus;

public class DataConflictException extends BusinessException {
    private static final String errorCode = "8000";

    public DataConflictException() {
        super(errorCode, HttpStatus.CONFLICT);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.DATA_INTEGRITY_VIOLATION.getMessage();
    }
}
