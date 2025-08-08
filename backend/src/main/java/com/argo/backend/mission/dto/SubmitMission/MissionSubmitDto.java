package com.argo.backend.mission.dto.SubmitMission;

import com.argo.backend.domain.cardgame.enums.CardTier;
import lombok.Getter;

public record MissionSubmitDto(boolean successful, Long cardId, CardTier tier) {

    public static MissionSubmitDto success(Long cardId, CardTier tier) {
        return new MissionSubmitDto(true, cardId, tier);
    }

    public static MissionSubmitDto fail() {
        return new MissionSubmitDto(false, null, null);
    }
}
