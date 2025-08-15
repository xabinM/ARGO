package com.argo.backend.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Component
@Slf4j
public class FirebaseInitializer {

    @Value("${fcm.service-account-file:}") // 값이 없을 경우를 대비해 기본값 설정
    private Resource serviceAccountResource;
    
    @Value("${fcm.project-id}")
    private String projectId;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                if (!serviceAccountResource.exists()) {
                    log.warn("FCM 서비스 계정 파일을 찾을 수 없거나 설정되지 않았습니다. FCM 기능이 비활성화됩니다.");
                    return;
                }

                try (InputStream in = serviceAccountResource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(in))
                            .setProjectId(projectId)
                            .build();

                    FirebaseApp.initializeApp(options);
                    log.info("Firebase 초기화가 완료되었습니다. 프로젝트 ID: {}", projectId);
                }
            } else {
                log.info("Firebase가 이미 초기화되어 있습니다.");
            }
        } catch (IOException e) {
            log.error("Firebase 초기화 실패: {}", e.getMessage(), e);
        }
    }
}