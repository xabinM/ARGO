package com.argo.backend.domain.cardgame.enums;


import com.argo.backend.domain.cardgame.entity.Card;
import lombok.Getter;

import java.util.List;
import java.util.Random;

@Getter
public enum CardTier {
    COMMON(1.0, 25),
    EPIC(1.2, 25),
    RARE(1.5, 25),
    LEGENDARY(2.0, 25);

    private final double weight;
    private final int probability;

    CardTier(double weight, int probability) {
        this.weight = weight;
        this.probability = probability;
    }

    private static final Random random = new Random();

    public static CardTier getRandomByCardTier(Card card) {
        boolean hasSpot = card.getSpot() != null;
        return getRandomByStrategy(hasSpot ? CardTierStrategy.PREMIUM : CardTierStrategy.NORMAL);
    }

    private static CardTier getRandomByStrategy(CardTierStrategy strategy) {
        List<CardTier> candidates = switch (strategy) {
            case NORMAL -> List.of(COMMON, EPIC, RARE);
            case PREMIUM -> List.of(EPIC, RARE, LEGENDARY);
        };

        int total = candidates.stream().mapToInt(CardTier::getProbability).sum();
        int r = random.nextInt(total);
        int cumulative = 0;

        for (CardTier tier : candidates) {
            cumulative += tier.getProbability();
            if (r < cumulative) {
                return tier;
            }
        }

        return candidates.get(0);
    }

    private enum CardTierStrategy {
        NORMAL,
        PREMIUM
    }
}