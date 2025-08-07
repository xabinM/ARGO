package com.argo.backend.domain.cardgame.entity;

import com.argo.backend.domain.cardgame.enums.CardTier;
import com.argo.backend.domain.common.CreatedAtEntity;
import com.argo.backend.domain.team.entity.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_cards")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamCard extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_card_id")
    private Long teamCardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardTier tier = CardTier.EPIC;

    @Column(name = "obtained_at", columnDefinition = "datetime(6) default CURRENT_TIMESTAMP(6)")
    private LocalDateTime obtainedAt;

    @Column(name = "is_lost", columnDefinition = "boolean default false")
    private Boolean isLost = false;

    @Column(name = "is_locked", columnDefinition = "boolean default false")
    private Boolean isLocked = false;

    public static TeamCard from(Team team, Card card, CardTier tier) {
        return new TeamCard(team, card, tier);
    }

    protected TeamCard(Team team, Card card, CardTier tier) {
        this.team = team;
        this.card = card;
        this.tier = tier;
        this.obtainedAt = LocalDateTime.now();
    }

}
