package com.argo.backend.organization.dto.classdetail;

import com.argo.backend.domain.team.entity.Team;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
public class TeamDetailDto {
    
    private Long teamId;
    private String teamName;
    private int memberCount;
    private int totalScore;
    private List<TeamMemberDto> members;
    private Long teamLeaderId;

    
    public TeamDetailDto(Long teamId, String teamName, int memberCount, int totalScore, List<TeamMemberDto> members, Long teamLeaderId) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.memberCount = memberCount;
        this.totalScore = totalScore;
        this.members = members;
        this.teamLeaderId = teamLeaderId;
    }
    
    public static TeamDetailDto from(Team team, List<TeamMemberDto> members) {
        return new TeamDetailDto(
                team.getTeamId(),
                team.getTeamName(),
                members.size(),
                Optional.ofNullable(team.getGameResult())
                .map(gameResult -> gameResult.getTotalPoints())
                .orElse(0),
                members,
                Optional.ofNullable(team.getLeader())
                        .map(leader -> leader.getUserId())
                        .orElse(null)
        );
    }
}