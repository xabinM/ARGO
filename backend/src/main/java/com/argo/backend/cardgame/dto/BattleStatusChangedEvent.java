package com.argo.backend.cardgame.dto;

import com.argo.backend.domain.cardgame.enums.MatchStatus;

public record BattleStatusChangedEvent(String targetFcmToken, String teamName, MatchStatus newStatus) {
}
