package com.argo.backend.auth.dto.login;

import com.argo.backend.auth.dto.common.Tokens;
import com.argo.backend.domain.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private Role role;
    private Tokens tokens;
    private String message;
}
