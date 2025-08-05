package com.argo.backend.auth.dto.login;

import com.argo.backend.auth.dto.common.Tokens;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private LoginDto dto;
    private String message;
}
