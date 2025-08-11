package com.argo.backend.global.exception;

import com.argo.backend.auth.exception.AuthorizationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 * 기존 기능 + Python API 연동 관련 예외 처리 추가
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ======================== 기존 예외 처리 (유지) ========================
    
    /**
     * 비즈니스 예외 처리 (기존)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("🚨 비즈니스 예외: {} - {}", e.getCode(), e.getMessage());
        ErrorResponse error = new ErrorResponse(e.getCode(), e.getMessage());
        return new ResponseEntity<>(error, e.getHttpStatus());
    }

    /**
     * 인증/인가 예외 처리 (기존)
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(AuthorizationException e) {
        log.warn("🔒 인증/인가 예외: {} - {}", e.getCode(), e.getMessage());
        ErrorResponse error = new ErrorResponse(e.getCode(), e.getMessage());
        return new ResponseEntity<>(error, e.getHttpStatus());
    }

    /**
     * 잘못된 파라미터 예외 처리 (기존)
     * InvalidParameterException은 BusinessException을 상속하지만 별도 처리 가능
     */
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameterException(InvalidParameterException e) {
        log.warn("⚠️ 잘못된 파라미터: {} - {}", e.getCode(), e.getMessage());
        ErrorResponse error = new ErrorResponse(e.getCode(), e.getMessage());
        return new ResponseEntity<>(error, e.getHttpStatus());
    }

    // ======================== Python API 예외 처리 (신규 추가) ========================
    
    /**
     * Python API 통신 예외 처리
     * 🚨 임시 주석: PythonApiClient 생성 후 주석 해제
     */
    /*
    @ExceptionHandler(com.argo.backend.mission.api.PythonApiClient.PythonApiException.class)
    public ResponseEntity<Map<String, Object>> handlePythonApiException(
            com.argo.backend.mission.api.PythonApiClient.PythonApiException ex, WebRequest request) {
        
        log.error("🤖 Python API 예외 발생: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.put("error", "Python API Error");
        errorResponse.put("message", "AI 문제 생성 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
        errorResponse.put("details", ex.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
    */

    /**
     * 스팟 관련 IllegalArgumentException 처리
     * 일반적인 잘못된 요청도 함께 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("⚠️ 잘못된 요청: {}", ex.getMessage());
        
        // 스팟 관련 오류인지 확인
        if (ex.getMessage().contains("스팟") || ex.getMessage().contains("Spot") || 
            ex.getMessage().contains("spot") || ex.getMessage().contains("찾을 수 없습니다")) {
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", "Spot Not Found");
            errorResponse.put("message", "요청하신 스팟을 찾을 수 없습니다. 스팟명을 확인해주세요.");
            errorResponse.put("details", ex.getMessage());
            errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
            
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
        
        // 일반적인 잘못된 요청
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", "잘못된 요청입니다. 요청 내용을 확인해주세요.");
        errorResponse.put("details", ex.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ======================== 일반 예외 처리 (최종 catch-all) ========================
    
    /**
     * 처리되지 않은 모든 예외의 최종 핸들러
     * 위에서 처리되지 않은 예외들을 위한 안전망
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("💥 예상치 못한 예외 발생: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "예상치 못한 오류가 발생했습니다. 관리자에게 문의하세요.");
        errorResponse.put("details", ex.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}