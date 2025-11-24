package com.argo.backend.notification.service;

import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.organization.exception.types.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FCMTokenService {

    private final UserRepository userRepository;

    public void registerFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!Objects.equals(user.getFcmToken(), fcmToken)) {
            user.updateFcmToken(fcmToken);
        }
    }

    public void unregisterFcmToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        
        user.updateFcmToken(null);
    }
}
