package com.argo.backend.cardgame.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommonApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
        private CommonApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }

    public CommonApiResponse ok (T data,String message) {
        return new CommonApiResponse(
                this.success = true,
                this.message = message,
                this.data = data
        );
    }
}