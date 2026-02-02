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
        log.info("트랜잭션 커밋 완료. 대전 상태 변경 이벤트 수신: {}", event.newStatus());

        String token = event.targetFcmToken();
        String teamName = event.teamName();
        MatchStatus status = event.newStatus();

        switch (status) {
            case PENDING: // 대전 신청
                fcmService.sendChallengeNotification(token, teamName);
                break;
            case COMPLETED: // 대전 수락 (수락 시 COMPLETED로 변경됨)
                fcmService.sendChallengeAcceptedNotification(token, teamName);
                break;
            case CANCELLED: // 대전 거절 또는 취소
                fcmService.sendChallengeCancelledNotification(token, teamName);
                break;
            case EXPIRED: // 대전 만료
                fcmService.sendChallengeExpiredNotification(token, teamName);
                break;
            default:
                log.warn("처리되지 않은 대전 상태 이벤트: {}", status);
                break;
        }
    }
}
