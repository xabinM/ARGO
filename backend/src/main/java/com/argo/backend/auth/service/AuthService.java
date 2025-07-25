package com.argo.backend.auth.service;

import com.argo.backend.auth.dto.login.LoginRequest;
import com.argo.backend.auth.dto.signup.SignupRequest;
import com.argo.backend.auth.dto.common.TokenDto;
import com.argo.backend.auth.dto.withdraw.WithdrawalRequest;
import com.argo.backend.auth.exception.DuplicateUsernameException;
import com.argo.backend.auth.exception.InvalidTokenException;
import com.argo.backend.auth.exception.WrongPasswordException;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.auth.repository.UserWithdrawalRepository;
import com.argo.backend.auth.security.jwt.JwtTokenProvider;
import com.argo.backend.domain.user.User;
import com.argo.backend.domain.user.UserWithdrawal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public void signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException();
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.from(request.getUsername(),
                encodedPassword,
                request.getName(),
                request.getRole());
        userRepository.save(user);
    }

    public TokenDto login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication.getName(), getRole(authentication));
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName(), getRole(authentication));

        redisService.saveRefreshToken(request.getUsername(), refreshToken);

        return new TokenDto(accessToken, refreshToken);
    }

    private List<String> getRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public TokenDto refresh(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveToken(request);

        jwtTokenProvider.validateToken(refreshToken);

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        List<String> roles = jwtTokenProvider.getRolesFromToken(refreshToken);

        String savedRefreshToken = redisService.getRefreshToken(username);
        if (!refreshToken.equals(savedRefreshToken)) {
            throw new InvalidTokenException();
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(username, roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username, roles);

        redisService.reissueRefreshToken(username, newRefreshToken);

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void withdraw(String username, WithdrawalRequest request) {
        User user = userRepository.findByUsername(username);
        if (!user.isPasswordMatching(passwordEncoder, request.getPassword())) {
            throw new WrongPasswordException();
        }


        user.updateStatusByWithdraw();
        UserWithdrawal userWithdrawal = UserWithdrawal.from(user);
        userWithdrawalRepository.save(userWithdrawal);
    }
}
