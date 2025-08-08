package com.argo.backend.cardgame.dto.teamcard;

import java.util.List;
import java.util.Map;

public record TeamCardCollectionResponse(
        Long teamId,
        List<TeamCardDto> teamCards,
        int totalCount,
        Map<String, Integer> tierStats
) {
    public static TeamCardCollectionResponse of(Long teamId, List<TeamCardDto> teamCards, Map<String, Integer> tierStats) {
        return new TeamCardCollectionResponse(
                teamId,
                teamCards,
                teamCards.size(),
                tierStats
        );
    }
}