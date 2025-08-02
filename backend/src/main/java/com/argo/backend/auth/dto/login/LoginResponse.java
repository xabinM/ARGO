package com.argo.backend.auth.dto.login;

import com.argo.backend.auth.dto.common.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private TokenDto tokens;
    private String message;
}
