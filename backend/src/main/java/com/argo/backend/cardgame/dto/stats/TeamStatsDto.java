package com.argo.backend.cardgame.dto.stats;

import com.argo.backend.domain.cardgame.entity.GameResult;
import com.argo.backend.domain.team.entity.Team;

import java.time.LocalDateTime;

public record TeamStatsDto(
        Long teamId,
        String teamName,
        String leaderName,
        int totalGames,
        int wins,
        int losses,
        int draws,
        int totalPoints,
        LocalDateTime createdAt
) {
    public static TeamStatsDto from(Team team, String leaderName) {
        GameResult gameResult = team.getGameResult();
        
        if (gameResult == null) {
            return new TeamStatsDto(
                    team.getTeamId(),
                    team.getTeamName(),
                    leaderName,
                    0, 0, 0, 0, 0,
                    team.getCreatedAt()
            );
        }
        
        return new TeamStatsDto(
                team.getTeamId(),
                team.getTeamName(),
                leaderName,
                gameResult.getTotalGames(),
                gameResult.getWins(),
                gameResult.getLosses(),
                gameResult.getDraws(),
                gameResult.getTotalPoints(),
                team.getCreatedAt()
        );
    }
}