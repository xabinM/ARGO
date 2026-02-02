package com.argo.backend.notification.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FCMService {

    private static final Logger logger = LoggerFactory.getLogger(FCMService.class);

    // 재시도 스케줄링을 위한 실행기 (단일 스레드로 충분)
    private final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000;

    public void sendChallengeNotification(String targetFcmToken, String challengerTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            logger.warn("FCM token is null or empty for challenge notification.");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_received");
        data.put("challenge_team", challengerTeamName);
        
        logger.info("[FCM 시도] 대전 신청 알림 발송. To: {}", targetFcmToken);
        sendFcmMessageWithRetry(targetFcmToken, "대전 요청", challengerTeamName + " 팀에서 대전을 신청했습니다.", data, 1);
    }

    public void sendChallengeAcceptedNotification(String targetFcmToken, String accepterTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            logger.warn("FCM token is null or empty for challenge accepted notification.");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_accepted");
        data.put("challenge_team", accepterTeamName);
        
        logger.info("[FCM 시도] 대전 수락 알림 발송. To: {}", targetFcmToken);
        sendFcmMessageWithRetry(targetFcmToken, "대전 수락", accepterTeamName + " 팀이 대전을 수락했습니다.", data, 1);
    }

    public void sendChallengeCancelledNotification(String targetFcmToken, String cancellerTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            logger.warn("FCM token is null or empty for challenge cancelled notification.");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_cancelled");
        data.put("challenge_team", cancellerTeamName);
        
        logger.info("[FCM 시도] 대전 거절/취소 알림 발송. To: {}", targetFcmToken);
        sendFcmMessageWithRetry(targetFcmToken, "대전 거절", cancellerTeamName + " 팀이 대전을 거절했습니다.", data, 1);
    }

    private void sendFcmMessageWithRetry(String token, String title, String body, Map<String, String> data, int attempt) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .putAllData(data)
                .build();

        ApiFuture<String> future = FirebaseMessaging.getInstance().sendAsync(message);

        ApiFutures.addCallback(future, new com.google.api.core.ApiFutureCallback<String>() {
            @Override
            public void onSuccess(String messageId) {
                logger.info("Successfully sent message: {} (Attempt: {})", messageId, attempt);
            }

            @Override
            public void onFailure(Throwable t) {
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    logger.warn("FCM 발송 실패. {}ms 후 재시도합니다. (시도 {}/{}) Error: {}", RETRY_DELAY_MS, attempt, MAX_RETRY_ATTEMPTS, t.getMessage());

                    // 비동기 지연 재시도
                    retryExecutor.schedule(() ->
                        sendFcmMessageWithRetry(token, title, body, data, attempt + 1),
                        RETRY_DELAY_MS,
                        TimeUnit.MILLISECONDS
                    );
                } else {
                    // 최종 실패 (Recover 역할)
                    logger.error("[FCM 최종 실패] {}회 재시도 후에도 알림 발송에 실패했습니다. To: {}, Title: {}, Error: {}", MAX_RETRY_ATTEMPTS, token, title, t.getMessage());
                }
            }
        }, MoreExecutors.directExecutor());
    }
}
