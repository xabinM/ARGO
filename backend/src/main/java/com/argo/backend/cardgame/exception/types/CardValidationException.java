package com.argo.backend.cardgame.exception.types;

import com.argo.backend.cardgame.exception.CardGameErrorMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CardValidationException extends BusinessException {
    public CardValidationException() {
        super(CardGameErrorMessage.CARD_VALIDATION_FAILED.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    public CardValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}