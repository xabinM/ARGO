package com.argo.backend.domain.cardgame.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class GameResult {
    @Column(name = "total_games", columnDefinition = "int default 0")
    private Integer totalGames = 0;

    @Column(name = "wins", columnDefinition = "int default 0") 
    private Integer wins = 0;

    @Column(name = "losses", columnDefinition = "int default 0")
    private Integer losses = 0;

    @Column(name = "total_points", columnDefinition = "int default 0")
    private Integer totalPoints = 0;

    public void addWin(int points) {
        this.totalGames++;
        this.wins++;
        this.totalPoints += points;
    }

    public void addLoss() {
        this.totalGames++;
        this.losses++;
    }

    public void addDraw() {
        this.totalGames++;
    }

    public double getWinRate() {
        return totalGames > 0 ? (double) wins / totalGames : 0.0;
    }
}
