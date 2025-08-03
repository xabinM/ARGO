package com.argo.backend.organization.dto.teamautoassign;

import com.argo.backend.domain.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UnassignedStudentDto {
    
    private final Long studentId;
    private final String studentName;
    private final String studentNickname;
    private final String reason;
    
    public static UnassignedStudentDto of(User user, String reason) {
        return new UnassignedStudentDto(
                user.getUserId(),
                user.getName(),
                user.getUsername(),
                reason
        );
    }
}