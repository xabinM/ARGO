package com.argo.backend.cardgame.service;

import com.argo.backend.cardgame.dto.stats.TeamStatsDto;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.organization.exception.types.TeamNotFoundException;
import com.argo.backend.organization.exception.types.UnauthorizedClassAccessException;
import com.argo.backend.organization.exception.types.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamStatsService {
    
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    
    public TeamStatsDto getTeamStats(Long teamId, Long userId) {
        validateUser(userId);
        Team team = validateTeamAccess(teamId, userId);
        
        String leaderName = getTeamLeaderName(team);
        
        return TeamStatsDto.from(team, leaderName);
    }
    
    private void validateUser(Long userId) {
        if (userId == null) {
            throw new UserNotFoundException();
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }
    
    private Team validateTeamAccess(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);
        
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        
        // UserTeam 기반으로 팀 접근 권한 검증
        Team userTeam = user.getActiveTeamByClass(team.getClassRoom().getClassId());
        if (userTeam == null || !userTeam.getTeamId().equals(teamId)) {
            throw new UnauthorizedClassAccessException();
        }
        
        return team;
    }
    
    private String getTeamLeaderName(Team team) {
        return team.getLeader() != null ? 
               team.getLeader().getName() : 
               "리더 없음";
    }
}