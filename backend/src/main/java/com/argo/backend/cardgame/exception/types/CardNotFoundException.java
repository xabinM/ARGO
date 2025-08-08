package com.argo.backend.cardgame.exception.types;

import com.argo.backend.cardgame.exception.CardGameErrorMessage;
import com.argo.backend.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CardNotFoundException extends BusinessException {
    public CardNotFoundException() {
        super(CardGameErrorMessage.CARD_NOT_FOUND.getMessage(), HttpStatus.NOT_FOUND);
    }
    
    public CardNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}