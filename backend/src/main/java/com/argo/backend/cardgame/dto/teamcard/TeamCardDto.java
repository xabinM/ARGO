package com.argo.backend.cardgame.dto.teamcard;

import com.argo.backend.domain.cardgame.entity.TeamCard;
import com.argo.backend.domain.cardgame.enums.CardTier;

public record TeamCardDto(
        Long teamCardId,
        Long cardId,
        CardTier tier,
        Boolean isLost,
        Boolean isLocked
) {
    public static TeamCardDto from(TeamCard teamCard) {
        return new TeamCardDto(
                teamCard.getTeamCardId(),
                teamCard.getCard().getCardId(),
                teamCard.getTier(),
                teamCard.getIsLost(),
                teamCard.getIsLocked()
        );
    }
}