package com.argo.backend.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Component
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
                    return;
                }

                try (InputStream in = serviceAccountResource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(in))
                            .setProjectId(projectId)
                            .build();

                    FirebaseApp.initializeApp(options);
                }
            }
        } catch (IOException e) {
        }
    }
}