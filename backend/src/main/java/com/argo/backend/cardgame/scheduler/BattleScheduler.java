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

    @Scheduled(fixedRate = 50000)
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

        cardGameMatchRepository.saveAll(expiredMatches);
    }
}
