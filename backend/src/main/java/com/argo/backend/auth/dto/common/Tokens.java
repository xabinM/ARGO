package com.argo.backend.auth.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Tokens {
    private String accessToken;
    private String refreshToken;
}
