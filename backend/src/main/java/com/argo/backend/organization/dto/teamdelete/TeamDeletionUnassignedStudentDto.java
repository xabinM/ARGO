package com.argo.backend.organization.dto.teamdelete;

import com.argo.backend.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TeamDeletionUnassignedStudentDto {
    
    private Long studentId;
    private String studentName;
    private LocalDateTime unassignedAt;
    
    public TeamDeletionUnassignedStudentDto(Long studentId, String studentName, LocalDateTime unassignedAt) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.unassignedAt = unassignedAt;
    }
    
    public static TeamDeletionUnassignedStudentDto from(User student, LocalDateTime unassignedAt) {
        return new TeamDeletionUnassignedStudentDto(
                student.getUserId(),
                student.getName(),
                unassignedAt
        );
    }
}