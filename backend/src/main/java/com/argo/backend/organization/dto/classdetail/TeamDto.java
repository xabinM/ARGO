package com.argo.backend.organization.dto.classdetail;

import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamDto {
    private final Long teamId;
    private final String teamName;
    private final Integer memberCount;
    private final Integer totalScore;
    private final List<MemberDto> members;

    @Getter
    @Builder
    public static class MemberDto {
        private final Long studentId;
        private final String studentName;

        public static MemberDto from(User user) {
            return MemberDto.builder()
                    .studentId(user.getUserId())
                    .studentName(user.getName())
                    .build();
        }
    }

    public static TeamDto from(Team team, List<User> teamMembers, int totalScore) {
        return TeamDto.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .memberCount(teamMembers.size())
                .totalScore(totalScore)
                .members(teamMembers.stream()
                        .map(MemberDto::from)
                        .toList())
                .build();
    }
}