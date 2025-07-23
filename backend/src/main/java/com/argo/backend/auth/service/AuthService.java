package com.argo.backend.auth.service;

import com.argo.backend.auth.dto.LoginRequest;
import com.argo.backend.auth.dto.SignupRequest;
import com.argo.backend.auth.dto.TokenDto;
import com.argo.backend.auth.exception.DuplicateUsernameException;
import com.argo.backend.auth.exception.ExpiredTokenException;
import com.argo.backend.auth.exception.InvalidTokenException;
import com.argo.backend.auth.repository.UserRepository;
import com.argo.backend.auth.security.jwt.JwtTokenProvider;
import com.argo.backend.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public void signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException();
        }

        User user = User.from(request);
        userRepository.save(user);
    }

    public TokenDto login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication.getName(), getRole(authentication));
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName(), getRole(authentication));

//        System.out.println("username : " + authentication.getName());
//        System.out.println(refreshToken);

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

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ExpiredTokenException();
        }
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        List<String> roles = jwtTokenProvider.getRolesFromToken(refreshToken);

        // redisService에 refreshToken을 넘겨서 redis에 있는 토큰과 같은지 확인
        String savedRefreshToken = redisService.getRefreshToken(username);
        if (!refreshToken.equals(savedRefreshToken)) {
            throw new InvalidTokenException();
        }

        // 통과하면 새 토큰 (access, refresh 둘다) 반환
        String newAccessToken = jwtTokenProvider.generateAccessToken(username, roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username, roles);

        // // 5. Redis에 새 refresh 토큰 저장 (기존 것을 덮어쓰기? 아님 삭제후 다시 저장?)
        redisService.reissueRefreshToken(username, newRefreshToken);
        // 반환
        return new TokenDto(newAccessToken, newRefreshToken);
    }
}
