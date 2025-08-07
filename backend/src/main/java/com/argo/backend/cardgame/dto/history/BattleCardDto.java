package com.argo.backend.cardgame.dto.history;

import com.argo.backend.domain.cardgame.entity.TeamCard;
import com.argo.backend.domain.cardgame.enums.BattleStrategy;
import com.argo.backend.domain.cardgame.enums.CardTier;

public record BattleCardDto(
        Long teamCardId,
        Long cardId,
        CardTier tier,
        BattleStrategy battleStance
) {
    public static BattleCardDto from(TeamCard teamCard, BattleStrategy battleStance) {
        return new BattleCardDto(
                teamCard.getTeamCardId(),
                teamCard.getCard().getCardId(),
                teamCard.getTier(),
                battleStance
        );
    }
}