package com.argo.backend.domain.cardgame.enums;

public enum CardTier {
    COMMON(1.0),
    EPIC(1.2),
    RARE(1.5),
    LEGENDARY(2.0);

    private final double weight;

    CardTier(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}