package com.argo.backend.organization.dto.classdetail;

import com.argo.backend.domain.team.entity.Team;
import lombok.Getter;

import java.util.List;

@Getter
public class TeamDetailDto {
    
    private Long teamId;
    private String teamName;
    private int memberCount;
    private int totalScore;
    private List<TeamMemberDto> members;
    
    public TeamDetailDto(Long teamId, String teamName, int memberCount, int totalScore, List<TeamMemberDto> members) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.memberCount = memberCount;
        this.totalScore = totalScore;
        this.members = members;
    }
    
    public static TeamDetailDto from(Team team, List<TeamMemberDto> members) {
        return new TeamDetailDto(
                team.getTeamId(),
                team.getTeamName(),
                members.size(),
                0, // TODO: 점수 계산 로직 필요
                members
        );
    }
}