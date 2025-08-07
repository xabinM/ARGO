package com.argo.backend.organization.dto.classroomcreate;

/**
 * 반 생성 응답 데이터를 담는 DTO record
 * 반 생성 성공 시 클라이언트에게 반환할 정보들을 포함하는 응답 객체
 */


import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ClassCreateResponse(
        Long classId,
        
        String className,
        
        String inviteCode,
        
        Long teacherId,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime createdAt
) {}
