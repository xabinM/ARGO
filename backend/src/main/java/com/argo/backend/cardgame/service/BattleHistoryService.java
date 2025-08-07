package com.argo.backend.cardgame.service;

import com.argo.backend.cardgame.dto.history.BattleHistoryDto;
import com.argo.backend.cardgame.dto.history.BattleHistoryResponse;
import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import com.argo.backend.domain.cardgame.repository.CardGameMatchRepository;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.organization.exception.types.TeamNotFoundException;
import com.argo.backend.organization.exception.types.UnauthorizedClassAccessException;
import com.argo.backend.organization.exception.types.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BattleHistoryService {
    
    private final CardGameMatchRepository cardGameMatchRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    
    public BattleHistoryResponse getBattleHistory(Long teamId, Long userId, Pageable pageable) {
        validateUser(userId);
        Team team = validateTeamAccess(teamId, userId);
        
        Page<CardGameMatch> matchPage = cardGameMatchRepository.findByTeamIdOrderByCreatedAtDesc(teamId, pageable);
        
        Page<BattleHistoryDto> battleHistoryPage = matchPage.map(match -> 
            BattleHistoryDto.from(match, teamId)
        );
        
        return BattleHistoryResponse.from(battleHistoryPage);
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
}