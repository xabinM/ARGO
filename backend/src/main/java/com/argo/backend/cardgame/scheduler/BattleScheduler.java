package com.argo.backend.cardgame.scheduler;

import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import com.argo.backend.domain.cardgame.enums.MatchStatus;
import com.argo.backend.domain.cardgame.repository.CardGameMatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BattleScheduler {

    private final CardGameMatchRepository cardGameMatchRepository;

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행 (DelayQueue 처리 실패 안전망)
    @Transactional
    public void expireOldBattles() {
        LocalDateTime thirtySecondsAgo = LocalDateTime.now().minusSeconds(30);

        List<CardGameMatch> expiredMatches = cardGameMatchRepository.findAllByStatusAndCreatedAtBefore(MatchStatus.PENDING, thirtySecondsAgo);

        if (expiredMatches.isEmpty()) {
            return;
        }

        for (CardGameMatch match : expiredMatches) {
            match.setStatus(MatchStatus.EXPIRED);

            if (match.getChallengerCard() != null) {
                match.getChallengerCard().setIsLocked(false);
            }
        }

        log.warn("안전망 스케줄러 동작: {}개의 미처리 만료 배틀을 정리했습니다.", expiredMatches.size());
        cardGameMatchRepository.saveAll(expiredMatches);
    }
}
