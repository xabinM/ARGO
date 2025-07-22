package com.argo.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private TokenDto tokens;
    private String message;
}
