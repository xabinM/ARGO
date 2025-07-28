package com.argo.backend.organization.dto.classdetail;

import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudentDto {
    private final Long studentId;
    private final String studentName;
    private final Long teamId;
    private final String teamName;
    private final LocalDateTime joinedAt;

    public static StudentDto from(User user, Team team, LocalDateTime joinedAt) {
        return StudentDto.builder()
                .studentId(user.getUserId())
                .studentName(user.getName())
                .teamId(team != null ? team.getTeamId() : null)
                .teamName(team != null ? team.getTeamName() : null)
                .joinedAt(joinedAt)
                .build();
    }
}