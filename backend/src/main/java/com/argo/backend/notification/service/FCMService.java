package com.argo.backend.notification.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FCMService {

    private static final Logger logger = LoggerFactory.getLogger(FCMService.class);

    public void sendChallengeNotification(String targetFcmToken, String challengerTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            logger.warn("FCM token is null or empty for challenge notification.");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_received");
        data.put("challenge_team", challengerTeamName);
        
        sendFcmMessage(targetFcmToken, "대전 요청", challengerTeamName + " 팀에서 대전을 신청했습니다.", data);
    }

    public void sendChallengeAcceptedNotification(String targetFcmToken, String accepterTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            logger.warn("FCM token is null or empty for challenge accepted notification.");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_accepted");
        data.put("challenge_team", accepterTeamName);
        
        sendFcmMessage(targetFcmToken, "대전 수락", accepterTeamName + " 팀이 대전을 수락했습니다.", data);
    }

    public void sendChallengeCancelledNotification(String targetFcmToken, String cancellerTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            logger.warn("FCM token is null or empty for challenge cancelled notification.");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_cancelled");
        data.put("challenge_team", cancellerTeamName);
        
        sendFcmMessage(targetFcmToken, "대전 거절", cancellerTeamName + " 팀이 대전을 거절했습니다.", data);
    }

    private void sendFcmMessage(String token, String title, String body, Map<String, String> data) {
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
                logger.info("Successfully sent message: {}", messageId);
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("Error sending FCM message", t);
            }
        }, MoreExecutors.directExecutor());
    }
}
