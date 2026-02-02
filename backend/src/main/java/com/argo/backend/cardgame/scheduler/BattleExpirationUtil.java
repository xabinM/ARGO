package com.argo.backend.cardgame.scheduler;

import com.argo.backend.cardgame.service.BattleService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class BattleExpirationUtil {

    private final DelayQueue<DelayedBattle> expirationQueue = new DelayQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final BattleService battleService;

    public BattleExpirationUtil(@Lazy BattleService battleService) {
        this.battleService = battleService;
    }

    @PostConstruct
    public void init() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DelayedBattle expiredBattle = expirationQueue.take();
                    battleService.expireMatch(expiredBattle.getMatchId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public void registerBattleExpiration(Long matchId, long delayInMillis) {
        expirationQueue.put(new DelayedBattle(matchId, delayInMillis));
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }
}
