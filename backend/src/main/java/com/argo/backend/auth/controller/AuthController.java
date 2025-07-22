package com.argo.backend.auth.controller;

import com.argo.backend.auth.dto.LoginRequest;
import com.argo.backend.auth.dto.LoginResponse;
import com.argo.backend.auth.dto.SignupRequest;
import com.argo.backend.auth.dto.TokenDto;
import com.argo.backend.auth.service.AuthService;
import com.argo.backend.global.enums.ResponseMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        TokenDto tokens = authService.login(request);
        return ResponseEntity.ok(new LoginResponse(tokens, ResponseMessage.LOGIN_SUCCESS.getMessage()));
    }
}
