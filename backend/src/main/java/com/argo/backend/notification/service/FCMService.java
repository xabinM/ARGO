package com.argo.backend.notification.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FCMService {

    public void sendChallengeNotification(String targetFcmToken, String challengerTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            log.warn("FCM 토큰이 없어 알림을 보낼 수 없습니다.");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_received");
        data.put("challenge_team", challengerTeamName);
        
        sendFcmMessage(targetFcmToken, "대결 요청", challengerTeamName + " 팀에서 대결을 신청했습니다.", data);
    }

    public void sendChallengeAcceptedNotification(String targetFcmToken, String accepterTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            log.warn("FCM 토큰이 없어 알림을 보낼 수 없습니다.");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_accepted");
        data.put("challenge_team", accepterTeamName);
        
        sendFcmMessage(targetFcmToken, "대결 수락", accepterTeamName + " 팀이 대결을 수락했습니다.", data);
    }

    public void sendChallengeCancelledNotification(String targetFcmToken, String cancellerTeamName) {
        if (targetFcmToken == null || targetFcmToken.trim().isEmpty()) {
            log.warn("FCM 토큰이 없어 알림을 보낼 수 없습니다.");
            return;
        }
        
        Map<String, String> data = new HashMap<>();
        data.put("command", "challenge_cancelled");
        data.put("challenge_team", cancellerTeamName);
        
        sendFcmMessage(targetFcmToken, "대결 거절", cancellerTeamName + " 팀이 대결을 거절했습니다.", data);
    }

    private void sendFcmMessage(String token, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setToken(token) // 수신자
                    .setNotification(Notification.builder() //알림 표시 부분
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data) // 앱에서 처리할 부분
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공: {} -> {}", title, response);
            
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패: {} - {}", title, e.getMessage(), e);
        } catch (Exception e) {
            log.error("FCM 전송 중 예외 발생: {} - {}", title, e.getMessage(), e);
        }
    }
}