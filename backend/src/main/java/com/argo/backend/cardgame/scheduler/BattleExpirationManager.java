package com.argo.backend.cardgame.scheduler;

import com.argo.backend.cardgame.service.BattleExpireService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BattleExpirationManager {

    private final BattleExpireService battleExpireService;

    private final DelayQueue<DelayedBattle> expirationQueue = new DelayQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void init() {
        // 서버 재시작 시 유실된 데이터 복구
        battleExpireService.expireAllPendingMatches();
        // 만료 처리 시작
        startExpirationProcessor();
    }

    // 대전 생성 시 만료 등록
    public void registerBattleExpiration(Long matchId, long delayInMillis) {
        expirationQueue.put(new DelayedBattle(matchId, delayInMillis));
    }

    private void startExpirationProcessor() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DelayedBattle expiredBattle = expirationQueue.take();
                    battleExpireService.expireMatch(expiredBattle.getMatchId());
                } catch (InterruptedException e) {
                    // 서버 종료 신호 → 정상 종료
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    // 장애 발생 → DB 복구 후 재시작
                    log.error("만료 처리 중 예외 발생, 복구 시작", e);
                    recoverAndRestart();
                }
            }
        });
    }

    private void recoverAndRestart() {
        battleExpireService.expireAllPendingMatches();
        startExpirationProcessor();
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }
}