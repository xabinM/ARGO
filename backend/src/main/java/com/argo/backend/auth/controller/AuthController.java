package com.argo.backend.auth.controller;

import com.argo.backend.auth.dto.common.Tokens;
import com.argo.backend.auth.dto.login.LoginDto;
import com.argo.backend.auth.dto.login.LoginRequest;
import com.argo.backend.auth.dto.login.LoginResponse;
import com.argo.backend.auth.dto.reissue.ReissueResponse;
import com.argo.backend.auth.dto.signup.SignupRequest;
import com.argo.backend.auth.dto.withdraw.WithdrawalRequest;
import com.argo.backend.auth.service.AuthService;
import com.argo.backend.global.enums.ResponseMessage;
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

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest request) {
        authService.signup(request);

        return ResponseEntity.ok(ResponseMessage.SIGNUP_SUCCESS.getMessage());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {

        LoginDto dto = authService.login(request);
        return ResponseEntity.ok(new LoginResponse(dto, ResponseMessage.LOGIN_SUCCESS.getMessage()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Tokens tokens = authService.refresh(request);

        return ResponseEntity.ok(new ReissueResponse(tokens));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdraw(@AuthenticationPrincipal Long userId,
                                      @RequestBody @Valid WithdrawalRequest request) {
        authService.withdraw(userId, request);

        return ResponseEntity.ok(ResponseMessage.WITHDRAW_SUCCESS.getMessage());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("RefreshToken") String refreshToken) {
        authService.logout(refreshToken);

        return ResponseEntity.ok(ResponseMessage.SUCCESS_LOGOUT.getMessage());
    }
}
