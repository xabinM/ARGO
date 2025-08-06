package com.argo.backend.cardgame.dto.battle;

public record BattleRequestDto(
        Long challengerTeamId,
        Long challengedTeamId,
        SelectedCardDto selectedCard
) {
}