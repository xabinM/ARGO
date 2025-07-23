package com.argo.backend.auth.security;

import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.domain.user.User;
import com.argo.backend.global.enums.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userEntity = userRepository.findByUsername(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException(ResponseMessage.MEMBER_NOT_FOUND_EXCEPTION.getMessage());
        }

        return new CustomUserDetails(userEntity);
    }
}
