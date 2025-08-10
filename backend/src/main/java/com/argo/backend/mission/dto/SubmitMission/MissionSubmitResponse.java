package com.argo.backend.mission.dto.SubmitMission;

import com.argo.backend.domain.cardgame.enums.CardTier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionSubmitResponse {

    private boolean success;
    private String message;
    private Long cardId;
    private CardTier tier;
}
