package com.argo.backend.cardgame.service;

import com.argo.backend.cardgame.dto.battle.BattleOpponentDto;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BattleService {
    
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    
    public List<BattleOpponentDto> getBattleOpponents(Long teamId, Long userId) {
        validateUser(userId);
        Team currentTeam = validateTeamAccess(teamId, userId);
        
        List<Team> allTeams = teamRepository.findByClassRoomOrderByCreatedAtAsc(currentTeam.getClassRoom());
        
        return allTeams.stream()
                .filter(team -> !team.getTeamId().equals(teamId))
                .map(team -> {
                    String leaderName = getTeamLeaderName(team);
                    return BattleOpponentDto.from(team, leaderName);
                })
                .collect(Collectors.toList());
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
        
        User user = userRepository.findById(userId).get();
        
        if (user.getTeam() == null || !user.getTeam().getTeamId().equals(teamId)) {
            throw new UnauthorizedClassAccessException();
        }
        
        return team;
    }
    
    private String getTeamLeaderName(Team team) {
        return team.getUsers().stream()
                .findFirst()
                .map(User::getName)
                .orElse("리더 없음");
    }
}