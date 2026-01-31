package com.argo.backend.cardgame.exception.types;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class LockedCardUsageException extends BusinessException {
    private static final String FAIL_CODE = "7006";

    public LockedCardUsageException() {
        super(FAIL_CODE, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.LOCKED_CARD_CANNOT_BE_USED.getMessage();
    }
}
