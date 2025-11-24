package com.argo.backend.organization.dto.teamautoassign;

import com.argo.backend.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AutoAssignUnassignedStudentDto {
    
    private final Long studentId;
    private final String studentName;
    private final String studentNickname;
    private final String reason;
    
    public static AutoAssignUnassignedStudentDto of(User user, String reason) {
        return new AutoAssignUnassignedStudentDto(
                user.getUserId(),
                user.getName(),
                user.getUsername(),
                reason
        );
    }
}