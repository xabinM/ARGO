package com.argo.backend.cardgame.dto.battle;

import com.argo.backend.domain.cardgame.entity.GameResult;
import com.argo.backend.domain.team.entity.Team;

public record BattleOpponentDto(
        Long teamId,
        String teamName,
        String leaderName,
        int totalGames,
        int wins,
        int losses,
        int draws,
        int totalPoints
) {
    public static BattleOpponentDto from(Team team, String leaderName) {
        GameResult gameResult = team.getGameResult();
        
        if (gameResult == null) {
            return new BattleOpponentDto(
                    team.getTeamId(),
                    team.getTeamName(),
                    leaderName,
                    0, 0, 0, 0, 0
            );
        }
        
        return new BattleOpponentDto(
                team.getTeamId(),
                team.getTeamName(),
                leaderName,
                gameResult.getTotalGames(),
                gameResult.getWins(),
                gameResult.getLosses(),
                gameResult.getDraws(),
                gameResult.getTotalPoints()
        );
    }
}