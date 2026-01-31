package com.argo.backend.cardgame.exception.types;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProcessedBattleCancelException extends BusinessException {
    private static final String FAIL_CODE = "7007";

    public ProcessedBattleCancelException() {
        super(FAIL_CODE, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.CANNOT_CANCEL_PROCESSED_BATTLE.getMessage();
    }
}
