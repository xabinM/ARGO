package com.argo.backend.notification.service;

import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.organization.exception.types.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FCMTokenService {

    private final UserRepository userRepository;

    public void registerFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        
        user.updateFcmToken(fcmToken);
        log.info("FCM 토큰 등록 완료: userId={}", userId);
    }

    // 현재 사용되지 않는 서비스 (우리 프로젝트에는 토큰을 해제하는건 없음) - 삭제 예정
    public void unregisterFcmToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        
        user.updateFcmToken(null);
        log.info("FCM 토큰 해제 완료: userId={}", userId);
    }
}