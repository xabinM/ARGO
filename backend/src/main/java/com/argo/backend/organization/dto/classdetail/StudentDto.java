package com.argo.backend.organization.dto.classdetail;

import com.argo.backend.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StudentDto {
    
    private Long studentId;
    private String studentName;
    private Long teamId;
    private String teamName;
    private LocalDateTime joinedAt;
    
    public StudentDto(Long studentId, String studentName, 
                     Long teamId, String teamName, LocalDateTime joinedAt) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.teamId = teamId;
        this.teamName = teamName;
        this.joinedAt = joinedAt;
    }
    
    public static StudentDto from(User student, String teamName, LocalDateTime joinedAt) {
        return new StudentDto(
                student.getUserId(),
                student.getName(),
                null, // teamId는 별도 처리
                teamName,
                joinedAt
        );
    }
    
    public static StudentDto from(User student, Long teamId, String teamName, LocalDateTime joinedAt) {
        return new StudentDto(
                student.getUserId(),
                student.getName(),
                teamId,
                teamName,
                joinedAt
        );
    }
}