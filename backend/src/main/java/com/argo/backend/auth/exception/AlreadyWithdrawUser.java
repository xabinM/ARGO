package com.argo.backend.auth.exception;

import com.argo.backend.global.enums.ResponseMessage;

public class AlreadyWithdrawUser extends RuntimeException {

    public AlreadyWithdrawUser() {
        super(ResponseMessage.ALREADY_WITHDRAW_USER.getMessage());
    }
}
