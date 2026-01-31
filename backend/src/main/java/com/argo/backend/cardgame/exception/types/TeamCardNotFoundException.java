package com.argo.backend.cardgame.exception.types;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class TeamCardNotFoundException extends BusinessException {
    private static final String FAIL_CODE = "7003";

    public TeamCardNotFoundException() {
        super(FAIL_CODE, HttpStatus.NOT_FOUND);
    }

    @Override
    public String getMessage() {
        return ResponseMessage.TEAM_CARD_NOT_FOUND.getMessage();
    }
}
