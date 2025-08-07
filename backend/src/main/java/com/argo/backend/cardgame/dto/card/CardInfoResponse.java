package com.argo.backend.cardgame.dto.card;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CardInfoResponse {
    private Long cardId;
    private boolean exists;
    
    public static CardInfoResponse of(Long cardId, boolean exists) {
        return new CardInfoResponse(cardId, exists);
    }
}