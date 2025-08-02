package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotFoundUserException extends BusinessException {

    private static final String FAIL_CODE = "4000";

    public NotFoundUserException() {
        super(FAIL_CODE, HttpStatus.NOT_FOUND);
    }

    public String getMessage() {
        return ResponseMessage.USER_NOT_FOUND_EXCEPTION.getMessage();
    }
}
