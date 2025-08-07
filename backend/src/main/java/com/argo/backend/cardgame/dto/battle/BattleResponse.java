package com.argo.backend.cardgame.dto.battle;

public record BattleResponse(String message) {
    public static BattleResponse success(String message) {
        return new BattleResponse(message);
    }
}