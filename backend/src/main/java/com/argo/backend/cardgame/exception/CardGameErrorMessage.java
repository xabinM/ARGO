package com.argo.backend.cardgame.exception;

import lombok.Getter;

@Getter
public enum CardGameErrorMessage {
    // Card 관련
    CARD_NOT_FOUND("존재하지 않는 카드입니다."),
    CARD_VALIDATION_FAILED("카드 정보 유효성 검증에 실패했습니다."),
    
    // System
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

    private final String message;

    CardGameErrorMessage(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}