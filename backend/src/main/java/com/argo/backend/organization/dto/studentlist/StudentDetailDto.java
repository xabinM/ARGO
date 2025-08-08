package com.argo.backend.organization.dto.studentlist;

import com.argo.backend.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StudentDetailDto {
    
    private Long studentId;
    private String studentName;
    private LocalDateTime joinedAt;
    private TeamInfoDto teamInfo;
    
    public StudentDetailDto(Long studentId, String studentName,
                          LocalDateTime joinedAt, TeamInfoDto teamInfo) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.joinedAt = joinedAt;
        this.teamInfo = teamInfo;
    }
    
    public static StudentDetailDto from(User student, LocalDateTime joinedAt, TeamInfoDto teamInfo) {
        return new StudentDetailDto(
                student.getUserId(),
                student.getName(),
                joinedAt,
                teamInfo
        );
    }
}