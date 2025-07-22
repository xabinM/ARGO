package com.argo.backend.auth.service;

import com.argo.backend.auth.dto.SignupRequest;
import com.argo.backend.auth.exception.DuplicateUsernameException;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public void signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException();
        }

        User user = User.from(request);
        userRepository.save(user);
    }
}
