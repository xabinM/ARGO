package com.argo.backend.cardgame.exception.types;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class BattleNotFoundException extends BusinessException {
    private static final String FAIL_CODE = "7001";

    public BattleNotFoundException() {
        super(FAIL_CODE, HttpStatus.NOT_FOUND);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.BATTLE_NOT_FOUND.getMessage();
    }
}
