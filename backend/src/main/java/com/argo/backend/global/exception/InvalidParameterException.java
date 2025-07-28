package com.argo.backend.global.exception;

import com.argo.backend.global.enums.ResponseMessage;
import org.springframework.http.HttpStatus;

public class InvalidParameterException extends BusinessException{

    private static final String errorCode = "9000";

    public InvalidParameterException(String code, HttpStatus httpStatus) {
        super(code, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.WRONG_REQUEST_PARAMETER.getMessage();
    }
}
