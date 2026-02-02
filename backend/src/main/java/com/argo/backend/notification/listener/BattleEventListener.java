package com.argo.backend.notification.listener;

import com.argo.backend.cardgame.dto.BattleStatusChangedEvent;
import com.argo.backend.domain.cardgame.enums.MatchStatus;
import com.argo.backend.notification.service.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BattleEventListener {

    private final FCMService fcmService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBattleStatusChanged(BattleStatusChangedEvent event) {

        String token = event.targetFcmToken();
        String teamName = event.teamName();
        MatchStatus status = event.newStatus();

        switch (status) {
            case PENDING:
                fcmService.sendChallengeNotification(token, teamName);
                break;
            case COMPLETED:
                fcmService.sendChallengeAcceptedNotification(token, teamName);
                break;
            case CANCELLED: // 대전 거절 또는 취소
                fcmService.sendChallengeCancelledNotification(token, teamName);
                break;
            default:
                break;
        }
    }
}
