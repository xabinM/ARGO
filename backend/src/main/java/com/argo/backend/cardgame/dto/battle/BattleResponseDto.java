package com.argo.backend.cardgame.dto.battle;

public record BattleResponseDto(
        String action,
        SelectedCardDto selectedCard
) {
}