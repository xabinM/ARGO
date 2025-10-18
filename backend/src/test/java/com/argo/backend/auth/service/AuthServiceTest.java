package com.argo.backend.auth.service;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.argo.backend.auth.dto.common.Tokens;
import com.argo.backend.auth.dto.login.LoginDto;
import com.argo.backend.auth.dto.login.LoginRequest;
import com.argo.backend.auth.dto.signup.SignupRequest;
import com.argo.backend.auth.dto.withdraw.WithdrawalRequest;
import com.argo.backend.auth.exception.*;
import com.argo.backend.auth.security.jwt.JwtTokenProvider;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.entity.UserWithdrawal;
import com.argo.backend.domain.user.enums.Role;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.domain.user.repository.UserWithdrawalRepository;
import com.argo.backend.redis.logic.AuthRedis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserWithdrawalRepository userWithdrawalRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthRedis authRedis;

    @InjectMocks
    private AuthService authService;

    // ===== signup =====
    @Test
    void signup_중복Username_예외() {
        final SignupRequest request = new SignupRequest("user",
                "pass", "name",
                Role.ROLE_STUDENT, true
        );
        given(userRepository.existsByUsername("user")).willReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> authService.signup(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void signup_정상() {
        final SignupRequest request = new SignupRequest("user",
                "pass", "name",
                Role.ROLE_STUDENT, true
        );
        given(userRepository.existsByUsername("user")).willReturn(false);
        given(passwordEncoder.encode("pass")).willReturn("encoded");

        authService.signup(request);

        verify(passwordEncoder).encode("pass");
        verify(userRepository).save(any(User.class));
    }

//    // ===== login =====
//    @Test
//    void login_없는유저_예외() {
//        final LoginRequest request = new LoginRequest("user", "pass");
//        given(userRepository.findByUsername("user")).willReturn(Optional.empty());
//
//        assertThrows(NotFoundUserException.class, () -> authService.login(request));
//    }
//
//    @Test
//    void login_비밀번호불일치_예외() {
//        final LoginRequest request = new LoginRequest("user", "pass");
//        final User mockUser = mock(User.class);
//        given(userRepository.findByUsername("user")).willReturn(Optional.of(mockUser));
//        given(mockUser.isPasswordMatching(passwordEncoder, "pass")).willReturn(false);
//
//        assertThrows(WrongPasswordException.class, () -> authService.login(request));
//    }
//
//    @Test
//    void login_정상_성공() {
//        // Given
//        LoginRequest request = new LoginRequest("user", "pass");
//        User mockUser = mock(User.class);
//
//        given(userRepository.findByUsername("user")).willReturn(Optional.of(mockUser));
//        given(mockUser.isPasswordMatching(passwordEncoder, "pass")).willReturn(true);
//        given(mockUser.getUserId()).willReturn(1L);
//        given(mockUser.getUsername()).willReturn("user");
//        given(mockUser.getName()).willReturn("홍길동");
//        given(mockUser.getRole()).willReturn(Role.ROLE_STUDENT);
//
//        List<String> roles = List.of(Role.ROLE_STUDENT.toString());
//        Tokens mockTokens = new Tokens("access-token", "refresh-token");
//
//        given(jwtTokenProvider.generateTokens(1L, "user", roles)).willReturn(mockTokens);
//
//        // When
//        LoginDto loginDto = authService.login(request);
//
//        // Then
//        assertEquals(mockTokens, loginDto.getTokens());
//        assertEquals(1L, loginDto.getUserId());
//        assertEquals("홍길동", loginDto.getName());
//        assertEquals(Role.ROLE_STUDENT, loginDto.getRole());
//
//        verify(userRepository).findByUsername("user");
//        verify(mockUser).isPasswordMatching(passwordEncoder, "pass");
//        verify(jwtTokenProvider).generateTokens(1L, "user", roles);
//    }


    // ===== withdraw =====
    @Test
    void withdraw_없는유저_예외() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(NotFoundUserException.class,
                () -> authService.withdraw(1L, new WithdrawalRequest("pass")));
    }

    @Test
    void withdraw_이미탈퇴유저_예외() {
        User mockUser = mock(User.class);
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(mockUser.checkStatus()).willReturn(false);

        assertThrows(AlreadyWithdrawUserException.class,
                () -> authService.withdraw(1L, new WithdrawalRequest("pass")));
    }

    @Test
    void withdraw_비밀번호불일치_예외() {
        User mockUser = mock(User.class);
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(mockUser.checkStatus()).willReturn(true);
        given(mockUser.isPasswordMatching(passwordEncoder, "pass")).willReturn(false);

        assertThrows(WrongPasswordException.class,
                () -> authService.withdraw(1L, new WithdrawalRequest("pass")));
    }

    @Test
    void withdraw_정상() {
        User mockUser = mock(User.class);
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(mockUser.checkStatus()).willReturn(true);
        given(mockUser.isPasswordMatching(passwordEncoder, "pass")).willReturn(true);

        authService.withdraw(1L, new WithdrawalRequest("pass"));

        verify(userWithdrawalRepository).save(any(UserWithdrawal.class));
    }

    // ===== logout =====
    @Test
    void logout_블랙리스트토큰_예외() {
        willDoNothing().given(jwtTokenProvider).validateToken("token");
        given(authRedis.isBlacklisted("token")).willReturn(true);

        assertThrows(InvalidTokenException.class, () -> authService.logout("token"));
    }

    @Test
    void logout_정상() {
        willDoNothing().given(jwtTokenProvider).validateToken("token");
        given(authRedis.isBlacklisted("token")).willReturn(false);

        authService.logout("token");

        verify(authRedis).addToBlacklist("token", "logout");
    }
}