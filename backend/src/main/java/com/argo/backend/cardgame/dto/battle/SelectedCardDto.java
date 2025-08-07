package com.argo.backend.cardgame.dto.battle;

import com.argo.backend.domain.cardgame.enums.BattleStrategy;

public record SelectedCardDto(
        Long teamCardId,
        BattleStrategy battleStance
) {
}