package com.argo.backend.cardgame.scheduler;

import com.argo.backend.cardgame.dto.BattleStatusChangedEvent;
import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import com.argo.backend.domain.cardgame.enums.MatchStatus;
import com.argo.backend.domain.cardgame.repository.CardGameMatchRepository;
import com.argo.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedRate = 300000) // 5분마다 실행
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

            // 만료 알림 이벤트 발행 (누락된 알림 발송)
            User challengerLeader = match.getChallengerTeam().getLeader();
            if (challengerLeader != null && challengerLeader.getFcmToken() != null) {
                eventPublisher.publishEvent(new BattleStatusChangedEvent(
                        challengerLeader.getFcmToken(),
                        match.getChallengedTeam().getTeamName(),
                        MatchStatus.EXPIRED
                ));
            }
        }

        cardGameMatchRepository.saveAll(expiredMatches);
    }
}
