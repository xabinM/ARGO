package com.argo.backend.auth.controller;

import com.argo.backend.auth.dto.common.Tokens;
import com.argo.backend.auth.dto.login.LoginDto;
import com.argo.backend.auth.dto.login.LoginRequest;
import com.argo.backend.auth.dto.login.LoginResponse;
import com.argo.backend.auth.dto.logout.LogoutResponse;
import com.argo.backend.auth.dto.reissue.ReissueResponse;
import com.argo.backend.auth.dto.signup.SignupRequest;
import com.argo.backend.auth.dto.signup.SignupResponse;
import com.argo.backend.auth.dto.withdraw.WithdrawResponse;
import com.argo.backend.auth.dto.withdraw.WithdrawalRequest;
import com.argo.backend.auth.service.AuthService;
import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.notification.service.FCMTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final FCMTokenService fcmTokenService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);

        return ResponseEntity.ok(new SignupResponse(true, ResponseMessage.SIGNUP_SUCCESS.getMessage()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {

        LoginDto dto = authService.login(request);
        
        // 로그인 성공 시 FCM 토큰 저장
        if (request.getFcmToken() != null && !request.getFcmToken().trim().isEmpty()) {
            fcmTokenService.registerFcmToken(dto.getUserId(), request.getFcmToken());
        }
        
        return ResponseEntity.ok(new LoginResponse(true, dto.getUserId(), dto.getName(),
                dto.getRole(), dto.getTokens(),
                ResponseMessage.LOGIN_SUCCESS.getMessage())
        );
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Tokens tokens = authService.refresh(request);

        return ResponseEntity.ok(new ReissueResponse(true, tokens));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdraw(@AuthenticationPrincipal Long userId,
                                      @RequestBody @Valid WithdrawalRequest request) {
        authService.withdraw(userId, request);

        return ResponseEntity.ok(new WithdrawResponse(true, ResponseMessage.WITHDRAW_SUCCESS.getMessage()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("RefreshToken") String refreshToken) {
        authService.logout(refreshToken);

        return ResponseEntity.ok(new LogoutResponse(true, ResponseMessage.SUCCESS_LOGOUT.getMessage()));
    }
}
