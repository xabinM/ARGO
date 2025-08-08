package com.argo.backend.auth.dto.withdraw;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WithdrawResponse {

    private boolean success;
    private String message;
}
