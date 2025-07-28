package com.argo.backend.organization.dto.studentlist;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudentInfo {
    private final Long studentId;
    private final String studentName;
    private final LocalDateTime joinedAt;
    private final TeamInfo teamInfo;

    public static StudentInfo from(ClassApplication application) {
        User student = application.getUser();
        TeamInfo teamInfo = null;
        
        if (student.getTeam() != null) {
            teamInfo = TeamInfo.builder()
                    .teamId(student.getTeam().getTeamId())
                    .teamName(student.getTeam().getTeamName())
                    .assignedAt(student.getTeam().getCreatedAt()) // 팀 배정 시간 (임시)
                    .build();
        }

        return StudentInfo.builder()
                .studentId(student.getUserId())
                .studentName(student.getName())
                .joinedAt(application.getCreatedAt())
                .teamInfo(teamInfo)
                .build();
    }

    @Getter
    @Builder
    public static class TeamInfo {
        private final Long teamId;
        private final String teamName;
        private final LocalDateTime assignedAt;
    }
}