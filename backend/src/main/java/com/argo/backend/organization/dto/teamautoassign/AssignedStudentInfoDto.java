package com.argo.backend.organization.dto.teamautoassign;

import com.argo.backend.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssignedStudentInfoDto {
    
    private final Long studentId;
    private final String studentName;
    private final String studentNickname;
    
    public static AssignedStudentInfoDto from(User user) {
        return new AssignedStudentInfoDto(
                user.getUserId(),
                user.getName(),
                user.getUsername()
        );
    }
}