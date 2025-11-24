package com.argo.backend.notification.controller;

import com.argo.backend.notification.service.FCMTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
@Slf4j
public class FCMController {

    private final FCMTokenService fcmTokenService;

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<Void> registerFcmToken(
            @RequestParam String fcmToken,
            @AuthenticationPrincipal Long userId
    ) {
        fcmTokenService.registerFcmToken(userId, fcmToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unregister")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<Void> unregisterFcmToken(
            @AuthenticationPrincipal Long userId
    ) {
        fcmTokenService.unregisterFcmToken(userId);
        return ResponseEntity.ok().build();
    }
}