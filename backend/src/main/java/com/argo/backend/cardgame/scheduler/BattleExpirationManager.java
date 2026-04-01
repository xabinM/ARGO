package com.argo.backend.cardgame.scheduler;

import com.argo.backend.cardgame.service.BattleExpireService;
import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import com.argo.backend.domain.cardgame.enums.MatchStatus;
import com.argo.backend.domain.cardgame.repository.CardGameMatchRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BattleExpirationManager {

    private static final long BATTLE_EXPIRE_MILLIS = 30000L;

    private final DelayQueue<DelayedBattle> expirationQueue = new DelayQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final BattleExpireService battleExpireService;
    private final CardGameMatchRepository cardGameMatchRepository;

    @PostConstruct
    public void init() {
        recoverPendingMatches();
        startExpirationProcessor();
    }

    public void registerBattleExpiration(Long matchId, long delayInMillis) {
        expirationQueue.put(new DelayedBattle(matchId, delayInMillis));
    }

    private void startExpirationProcessor() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                DelayedBattle expiredBattle = null;
                try {
                    expiredBattle = expirationQueue.take();
                    battleExpireService.expireMatch(expiredBattle.getMatchId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("만료 처리 중 예외 발생: matchId={}", expiredBattle != null ? expiredBattle.getMatchId() : "unknown", e);
                }
            }
        });
    }

    private void recoverPendingMatches() {
        List<CardGameMatch> pendingMatches = cardGameMatchRepository.findAllByStatus(MatchStatus.PENDING);

        List<CardGameMatch> alreadyExpired = new ArrayList<>();

        for (CardGameMatch match : pendingMatches) {
            long elapsed = Duration.between(match.getCreatedAt(), LocalDateTime.now()).toMillis();

            if (elapsed >= BATTLE_EXPIRE_MILLIS) {
                // 만료 기준을 이미 넘긴 경우: 알림 없이 직접 처리
                match.setStatus(MatchStatus.EXPIRED);
                if (match.getChallengerCard() != null) {
                    match.getChallengerCard().setIsLocked(false);
                }
                alreadyExpired.add(match);
            } else {
                // 아직 만료 전: 남은 시간으로 DelayQueue 등록 (정상 경로)
                registerBattleExpiration(match.getMatchId(), BATTLE_EXPIRE_MILLIS - elapsed);
            }
        }

        if (!alreadyExpired.isEmpty()) {
            cardGameMatchRepository.saveAll(alreadyExpired);
            log.info("서버 재시작 복구: {}개의 이미 만료된 배틀을 알림 없이 처리했습니다.", alreadyExpired.size());
        }

        long reregistered = pendingMatches.size() - alreadyExpired.size();
        if (reregistered > 0) {
            log.info("서버 재시작 복구: {}개의 PENDING 배틀을 DelayQueue에 재등록했습니다.", reregistered);
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }
}
