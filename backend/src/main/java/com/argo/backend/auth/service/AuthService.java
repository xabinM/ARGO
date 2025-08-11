package com.argo.backend.auth.service;

import com.argo.backend.auth.dto.login.LoginDto;
import com.argo.backend.auth.dto.login.LoginRequest;
import com.argo.backend.auth.dto.signup.SignupRequest;
import com.argo.backend.auth.dto.common.Tokens;
import com.argo.backend.auth.dto.withdraw.WithdrawalRequest;
import com.argo.backend.auth.exception.*;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.domain.user.repository.UserWithdrawalRepository;
import com.argo.backend.auth.security.jwt.JwtTokenProvider;
import com.argo.backend.domain.user.enums.Role;
import com.argo.backend.domain.user.entity.Teacher;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.entity.UserWithdrawal;
import com.argo.backend.redis.logic.AuthRedis;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BLACKLIST_STATUS_REISSUE = "reissued";
    private static final String BLACKLIST_STATUS_LOGOUT = "logout";

    private final UserRepository userRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRedis authRedis;

    public void signup(SignupRequest request) {
        validateDuplicateUsername(request.getUsername());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Role role = request.getRole();

        User user = createsUserByRole(request, role, encodedPassword);

        userRepository.save(user);
    }

    private void validateDuplicateUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException();
        }
    }

    private User createsUserByRole(SignupRequest request, Role role, String encodedPassword) {
        return switch (role) {
            case ROLE_STUDENT -> User.from(request.getUsername(), encodedPassword, request.getName(), role);
            case ROLE_TEACHER -> Teacher.from(request.getUsername(), encodedPassword, request.getName());
        };
    }


    public LoginDto login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (Objects.equals(user, null)) {
            throw new NotFoundUserException();
        }

        if (!user.isPasswordMatching(passwordEncoder, request.getPassword())) {
            throw new WrongPasswordException();
        }

        // todo 현재는 Role 이 한 가지이기에 이후 변경 필요시 변경
        List<String> roles = new ArrayList<>();
        roles.add(user.getRole().toString());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId(), user.getUsername(), roles);

        return new LoginDto(new Tokens(accessToken, refreshToken), user.getUserId(), user.getName(), user.getRole());
    }

    private List<String> getRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public Tokens refresh(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveToken(request);

        jwtTokenProvider.validateToken(refreshToken);

        if (authRedis.isBlacklisted(refreshToken)) {
            throw new InvalidTokenException();
        }
        authRedis.addToBlacklist(refreshToken, BLACKLIST_STATUS_REISSUE);

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        List<String> roles = jwtTokenProvider.getRolesFromToken(refreshToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, username, roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, username, roles);

        return new Tokens(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void withdraw(Long userId, WithdrawalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(NotFoundUserException::new);
        if (!user.checkStatus()) {
            throw new AlreadyWithdrawUserException();
        }

        if (!user.isPasswordMatching(passwordEncoder, request.getPassword())) {
            throw new WrongPasswordException();
        }

        user.updateStatusByWithdraw();
        UserWithdrawal userWithdrawal = UserWithdrawal.from(user);
        userWithdrawalRepository.save(userWithdrawal);
    }

    public void logout(String refreshToken) {
        jwtTokenProvider.validateToken(refreshToken);

        if (authRedis.isBlacklisted(refreshToken)) {
            throw new InvalidTokenException();
        }

        authRedis.addToBlacklist(refreshToken, BLACKLIST_STATUS_LOGOUT);
    }
}
