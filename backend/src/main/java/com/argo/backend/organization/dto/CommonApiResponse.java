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
    
    public static <T> CommonApiResponse<T> success(String message, T data) {
        return new CommonApiResponse<>(true, message, data);
    }
    
    public static CommonApiResponse<Void> success(String message) {
        return new CommonApiResponse<>(true, message);
    }
    
    public static <T> CommonApiResponse<T> error(String message) {
        return new CommonApiResponse<>(false, message, null);
    }
    
    // ResponseMessage enum 사용 오버로드 메서드들
    public static <T> CommonApiResponse<T> success(com.argo.backend.organization.message.ResponseMessage responseMessage, T data) {
        return new CommonApiResponse<>(true, responseMessage.getMessage(), data);
    }
    
    public static CommonApiResponse<Void> success(com.argo.backend.organization.message.ResponseMessage responseMessage) {
        return new CommonApiResponse<>(true, responseMessage.getMessage());
    }
}