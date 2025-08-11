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
import com.argo.backend.cardgame.util.TeamAccessValidator;
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
    private final TeamAccessValidator teamAccessValidator;
    
    public BattleHistoryResponse getBattleHistory(Long teamId, Long userId, Pageable pageable) {
        Team team = teamAccessValidator.validateTeamAccess(teamId, userId);
        
        Page<CardGameMatch> matchPage = cardGameMatchRepository.findByTeamIdOrderByCreatedAtDescWithTeams(teamId, pageable);
        
        Page<BattleHistoryDto> battleHistoryPage = matchPage.map(match -> 
            BattleHistoryDto.from(match, teamId) // 이제 FETCH JOIN으로 Team들이 이미 로딩됨 (쿼리 없음)
        );
        
        return BattleHistoryResponse.from(battleHistoryPage);
    }
}