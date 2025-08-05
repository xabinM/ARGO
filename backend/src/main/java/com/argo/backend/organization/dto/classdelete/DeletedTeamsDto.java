package com.argo.backend.organization.dto.classdelete;

import com.argo.backend.domain.team.entity.Team;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class DeletedTeamsDto {
    
    private int count;
    private List<TeamDetailDto> details;
    
    public DeletedTeamsDto(int count, List<TeamDetailDto> details) {
        this.count = count;
        this.details = details;
    }
    
    public static DeletedTeamsDto of(List<Team> teams, Map<Long, Integer> memberCounts) {
        List<TeamDetailDto> details = teams.stream()
                .map(team -> TeamDetailDto.from(team, memberCounts.getOrDefault(team.getTeamId(), 0)))
                .toList();
        return new DeletedTeamsDto(teams.size(), details);
    }
}

@Getter
class TeamDetailDto {
    
    private Long teamId;
    private String teamName;
    private int memberCount;
    
    public TeamDetailDto(Long teamId, String teamName, int memberCount) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.memberCount = memberCount;
    }
    
    public static TeamDetailDto from(Team team, int memberCount) {
        return new TeamDetailDto(
                team.getTeamId(),
                team.getTeamName(),
                memberCount
        );
    }
}