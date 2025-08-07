package com.argo.backend.cardgame.service;

import com.argo.backend.cardgame.dto.teamcard.TeamCardCollectionResponse;
import com.argo.backend.cardgame.dto.teamcard.TeamCardDto;
import com.argo.backend.domain.cardgame.entity.TeamCard;
import com.argo.backend.domain.cardgame.enums.CardTier;
import com.argo.backend.domain.cardgame.repository.TeamCardRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamCardService {
    
    private final TeamCardRepository teamCardRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    
    public TeamCardCollectionResponse getTeamCardCollection(Long teamId, Long userId) {
        validateUser(userId);
        Team team = validateTeamAccess(teamId, userId);
        
        List<TeamCard> teamCards = teamCardRepository.findByTeamOrderByTierAsc(team);
        
        List<TeamCardDto> teamCardDtos = teamCards.stream()
                .map(TeamCardDto::from)
                .collect(Collectors.toList());
        
        Map<String, Integer> tierStats = calculateTierStats(teamCards);
        
        return TeamCardCollectionResponse.of(teamId, teamCardDtos, tierStats);
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
    
    private Map<String, Integer> calculateTierStats(List<TeamCard> teamCards) {
        Map<CardTier, Long> tierCounts = teamCards.stream()
                .collect(Collectors.groupingBy(
                        TeamCard::getTier,
                        Collectors.counting()
                ));
        
        return Map.of(
                "COMMON", tierCounts.getOrDefault(CardTier.COMMON, 0L).intValue(),
                "RARE", tierCounts.getOrDefault(CardTier.RARE, 0L).intValue(),
                "EPIC", tierCounts.getOrDefault(CardTier.EPIC, 0L).intValue(),
                "LEGENDARY", tierCounts.getOrDefault(CardTier.LEGENDARY, 0L).intValue()
        );
    }
}