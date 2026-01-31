package com.argo.backend.cardgame.exception.types;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CardNotOwnedByTeamException extends BusinessException {
    private static final String FAIL_CODE = "7004";

    public CardNotOwnedByTeamException() {
        super(FAIL_CODE, HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.CARD_NOT_OWNED_BY_TEAM.getMessage();
    }
}
