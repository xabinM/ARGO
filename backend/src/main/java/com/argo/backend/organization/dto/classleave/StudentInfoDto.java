package com.argo.backend.organization.dto.classleave;

import com.argo.backend.domain.user.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StudentInfoDto {
    
    private Long studentId;
    private String studentName;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    
    public StudentInfoDto(Long studentId, String studentName, 
                         LocalDateTime joinedAt, LocalDateTime leftAt) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
    }
    
    public static StudentInfoDto from(User student, LocalDateTime joinedAt, LocalDateTime leftAt) {
        return new StudentInfoDto(
                student.getUserId(),
                student.getName(),
                joinedAt,
                leftAt
        );
    }
}