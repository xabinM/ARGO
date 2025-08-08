package com.argo.backend.cardgame.dto.teamcard;

import com.argo.backend.domain.cardgame.entity.TeamCard;
import com.argo.backend.domain.cardgame.enums.CardTier;

import java.time.LocalDateTime;

public record TeamCardDto(
        Long teamCardId,
        Long cardId,
        CardTier tier,
        LocalDateTime obtainedAt,
        Boolean isLost,
        Boolean isLocked
) {
    public static TeamCardDto from(TeamCard teamCard) {
        return new TeamCardDto(
                teamCard.getTeamCardId(),
                teamCard.getCard().getCardId(),
                teamCard.getTier(),
                teamCard.getCreatedAt(),
                teamCard.getIsLost(),
                teamCard.getIsLocked()
        );
    }
}