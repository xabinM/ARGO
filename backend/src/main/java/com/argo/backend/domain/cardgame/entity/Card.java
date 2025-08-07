package com.argo.backend.domain.cardgame.entity;

import com.argo.backend.domain.common.CreatedAtEntity;
import com.argo.backend.domain.location.entity.Location;
import com.argo.backend.domain.spot.entity.Spot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", unique = true)
    private Spot spot;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_attack", nullable = false)
    private Integer baseAttack;

    @Column(name = "base_defense", nullable = false)
    private Integer baseDefense;

    @Column(name = "is_spot_card", columnDefinition = "boolean default false")
    private Boolean isSpotCard = false;

    protected Card(Location location, Spot spot, String name, String description, Integer baseAttack, Integer baseDefense, Boolean isSpotCard) {
        this.location = location;
        this.spot = spot;
        this.name = name;
        this.description = description;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.isSpotCard = isSpotCard;
    }
}
