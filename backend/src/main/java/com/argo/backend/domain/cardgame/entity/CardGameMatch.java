package com.argo.backend.domain.cardgame.entity;

import com.argo.backend.domain.cardgame.enums.BattleStrategy;
import com.argo.backend.domain.cardgame.enums.MatchStatus;
import com.argo.backend.domain.common.CreatedAtEntity;
import com.argo.backend.domain.team.entity.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "card_game_matches")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardGameMatch extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenger_team_id", nullable = false)
    private Team challengerTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenged_team_id", nullable = false)
    private Team challengedTeam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "enum('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'EXPIRED') default 'PENDING'")
    private MatchStatus status = MatchStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenger_card_id")
    private TeamCard challengerCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenged_card_id")
    private TeamCard challengedCard;

    @Enumerated(EnumType.STRING)
    @Column(name = "challenger_strategy")
    private BattleStrategy challengerStrategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "challenged_strategy")
    private BattleStrategy challengedStrategy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loser_team_id")
    private Team loserTeam;

    @Column(name = "is_draw")
    private boolean isDraw;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_card_id")
    private TeamCard winnerCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loser_card_id")
    private TeamCard loserCard;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public static CardGameMatch createMatch(Team challengerTeam, Team challengedTeam) {
        return new CardGameMatch(challengerTeam, challengedTeam);
    }
    
    public static CardGameMatch from(Team challengerTeam, Team challengedTeam, TeamCard challengerCard, BattleStrategy challengerStrategy) {
        return new CardGameMatch(challengerTeam, challengedTeam, challengerCard, challengerStrategy);
    }

    protected CardGameMatch(Team challengerTeam, Team challengedTeam) {
        this.challengerTeam = challengerTeam;
        this.challengedTeam = challengedTeam;
        this.status = MatchStatus.PENDING;
    }
    
    protected CardGameMatch(Team challengerTeam, Team challengedTeam, TeamCard challengerCard, BattleStrategy challengerStrategy) {
        this.challengerTeam = challengerTeam;
        this.challengedTeam = challengedTeam;
        this.challengerCard = challengerCard;
        this.challengerStrategy = challengerStrategy;
        this.status = MatchStatus.PENDING;
    }

    public void acceptMatch() {
        this.status = MatchStatus.COMPLETED;
    }

    public void startMatch() {
        this.status = MatchStatus.PENDING;
        this.startedAt = LocalDateTime.now();
    }

    public void completeMatch(Team winner, Team loser, TeamCard winnerCard, TeamCard loserCard) {
        this.status = MatchStatus.COMPLETED;
        this.winnerTeam = winner;
        this.loserTeam = loser;
        this.winnerCard = winnerCard;
        this.loserCard = loserCard;
        this.endedAt = LocalDateTime.now();
    }

    public void cancelMatch() {
        this.status = MatchStatus.CANCELLED;
    }

    public void expireMatch() {
        this.status = MatchStatus.EXPIRED;
    }

    public boolean isWinner(Team team) {
        return winnerTeam != null && winnerTeam.equals(team);
    }

    public boolean isLoser(Team team) {
        return loserTeam != null && loserTeam.equals(team);
    }

    public boolean isDraw() {
        return winnerTeam == null && loserTeam == null && status == MatchStatus.COMPLETED;
    }
}
