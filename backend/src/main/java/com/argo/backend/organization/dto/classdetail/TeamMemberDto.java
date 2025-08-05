package com.argo.backend.organization.dto.classdetail;

import com.argo.backend.domain.user.entity.User;
import lombok.Getter;

@Getter
public class TeamMemberDto {
    
    private Long studentId;
    private String studentName;
    
    public TeamMemberDto(Long studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
    }
    
    public static TeamMemberDto from(User student) {
        return new TeamMemberDto(
                student.getUserId(),
                student.getName()
        );
    }
}