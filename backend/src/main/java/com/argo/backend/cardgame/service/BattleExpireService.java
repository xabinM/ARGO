package com.argo.backend.cardgame.service;

import com.argo.backend.cardgame.dto.BattleStatusChangedEvent;
import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import com.argo.backend.domain.cardgame.enums.MatchStatus;
import com.argo.backend.domain.cardgame.repository.CardGameMatchRepository;
import com.argo.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BattleExpireService {

    private final CardGameMatchRepository cardGameMatchRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 단건 만료 처리 (DelayQueue에 의해 호출)
    @Transactional
    public void expireMatch(Long matchId) {
        CardGameMatch match = cardGameMatchRepository.findById(matchId).orElse(null);

        if (match != null && match.getStatus() == MatchStatus.PENDING) {
            match.setStatus(MatchStatus.EXPIRED);

            if (match.getChallengerCard() != null) {
                match.getChallengerCard().setIsLocked(false);
            }
            cardGameMatchRepository.save(match);

            User challengerLeader = match.getChallengerTeam().getLeader();
            if (challengerLeader != null && challengerLeader.getFcmToken() != null) {
                eventPublisher.publishEvent(new BattleStatusChangedEvent(
                        challengerLeader.getFcmToken(),
                        match.getChallengedTeam().getTeamName(),
                        MatchStatus.EXPIRED
                ));
            }
        }
    }

    // 다건 만료 처리 (서버 재시작 복구 / 장애 복구 시 호출)
    @Transactional
    public void expireAllPendingMatches() {
        List<CardGameMatch> pendingMatches = cardGameMatchRepository
                .findAllByStatusAndCreatedAtBefore(MatchStatus.PENDING, LocalDateTime.now().minusSeconds(30));

        if (!pendingMatches.isEmpty()) {
            log.info("만료 복구 대상: {}건", pendingMatches.size());
            pendingMatches.forEach(match -> expireMatch(match.getMatchId()));
        }
    }
}