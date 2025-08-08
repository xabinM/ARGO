package com.argo.backend.organization.dto;

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
    
    public CommonApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }
}