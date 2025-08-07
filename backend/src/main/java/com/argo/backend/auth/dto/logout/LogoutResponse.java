package com.argo.backend.auth.dto.logout;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LogoutResponse {

    private boolean success;
    private String message;
}
