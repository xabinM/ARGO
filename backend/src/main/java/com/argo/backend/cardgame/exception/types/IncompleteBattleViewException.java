package com.argo.backend.cardgame.exception.types;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class IncompleteBattleViewException extends BusinessException {
    private static final String FAIL_CODE = "7008";

    public IncompleteBattleViewException() {
        super(FAIL_CODE, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.ONLY_COMPLETED_BATTLE_CAN_BE_VIEWED.getMessage();
    }
}
