package com.argo.backend.organization.dto;

/**
 * API 응답의 표준 형식을 정의하는 공통 DTO 클래스
 * 성공/실패 여부, 메시지, 데이터를 포함하는 통일된 응답 구조를 제공
 */

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