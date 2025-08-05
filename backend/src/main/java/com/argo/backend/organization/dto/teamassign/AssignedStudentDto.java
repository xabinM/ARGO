package com.argo.backend.organization.dto.teamassign;

import com.argo.backend.domain.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record AssignedStudentDto(
        Long studentId,
        String studentName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime assignedAt
) {
    public static AssignedStudentDto from(User student, LocalDateTime assignedAt) {
        return new AssignedStudentDto(
                student.getUserId(),
                student.getName(),
                assignedAt
        );
    }
}