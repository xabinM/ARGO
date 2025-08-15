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
    // Firebase에서 토큰을 갱신할 가능성이 있음
    // 하지만 거의 없다고함
    // 그래서 로그인을 하더라도 토큰이 바뀌는 경우가 있는데, 그 경우를 방지하기 위한 컨트롤러임
    // 만약 바뀌게 된다면 프론트에서 바뀐걸 감지해서 다시 userDb에 등록해주는 과정을 해줘야함
    // 거기서 사용되는 컨트롤러
    // 하지만 바뀔일이 "거의" 없음
    // 즉, 추가적으로 구현할 때를 위해 만들어둔 컨트롤러. 이 프로젝트에는 사용되지 않고 있음

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