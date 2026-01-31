package com.argo.backend.cardgame.exception.types;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AlreadyProcessedBattleException extends BusinessException {
    private static final String FAIL_CODE = "7002";

    public AlreadyProcessedBattleException() {
        super(FAIL_CODE, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.ALREADY_PROCESSED_BATTLE.getMessage();
    }
}
