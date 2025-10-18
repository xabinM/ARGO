package com.argo.backend.notification.service;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FCMService {

    public void sendChallengeNotification(String targetFcmToken, String challengerTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_received");
        data.put("challenge_team", challengerTeamName);
        
        sendFcmMessage(targetFcmToken, "대전 요청", challengerTeamName + " 팀에서 대전을 신청했습니다.", data);
    }

    public void sendChallengeAcceptedNotification(String targetFcmToken, String accepterTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_accepted");
        data.put("challenge_team", accepterTeamName);
        
        sendFcmMessage(targetFcmToken, "대전 수락", accepterTeamName + " 팀이 대전을 수락했습니다.", data);
    }

    public void sendChallengeCancelledNotification(String targetFcmToken, String cancellerTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_cancelled");
        data.put("challenge_team", cancellerTeamName);
        
        sendFcmMessage(targetFcmToken, "대전 거절", cancellerTeamName + " 팀이 대전을 거절했습니다.", data);
    }

    private void sendFcmMessage(String token, String title, String body, Map<String, String> data) {
        try {
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

            FirebaseMessaging.getInstance().send(message);
            
        } catch (FirebaseMessagingException e) {
        } catch (Exception e) {
        }
    }
}