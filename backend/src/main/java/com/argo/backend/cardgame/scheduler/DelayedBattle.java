package com.argo.backend.cardgame.scheduler;

import lombok.Getter;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedBattle implements Delayed {

    @Getter
    private final Long matchId;
    private final long expirationTime;

    public DelayedBattle(Long matchId, long delayInMillis) {
        this.matchId = matchId;
        this.expirationTime = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayInMillis);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = expirationTime - System.nanoTime();
        return unit.convert(diff, TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.expirationTime, ((DelayedBattle) o).expirationTime);
    }
}
