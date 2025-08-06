package com.argo.backend.cardgame.service;

import com.argo.backend.cardgame.dto.battle.BattleOpponentDto;
import com.argo.backend.cardgame.dto.battle.BattleRequestDto;
import com.argo.backend.cardgame.dto.battle.BattleResponse;
import com.argo.backend.cardgame.exception.types.CardNotFoundException;
import com.argo.backend.cardgame.exception.types.CardValidationException;
import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import com.argo.backend.domain.cardgame.entity.TeamCard;
import com.argo.backend.domain.cardgame.enums.MatchStatus;
import com.argo.backend.domain.cardgame.repository.CardGameMatchRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BattleService {
    
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamCardRepository teamCardRepository;
    private final CardGameMatchRepository cardGameMatchRepository;
    
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
    
    @Transactional
    public BattleResponse createBattle(BattleRequestDto request, Long userId) {
        validateUser(userId);
        
        Team challengerTeam = teamRepository.findById(request.challengerTeamId())
                .orElseThrow(TeamNotFoundException::new);
        Team challengedTeam = teamRepository.findById(request.challengedTeamId())
                .orElseThrow(TeamNotFoundException::new);
        
        validateChallengerAccess(challengerTeam, userId);
        
        TeamCard selectedCard = validateAndLockCard(request.selectedCard().teamCardId(), challengerTeam);
        
        CardGameMatch match = CardGameMatch.from(
                challengerTeam,
                challengedTeam,
                selectedCard,
                request.selectedCard().battleStance()
        );
        
        cardGameMatchRepository.save(match);
        
        return BattleResponse.success("대전 신청이 성공적으로 전송되었습니다");
    }
    
    private void validateChallengerAccess(Team challengerTeam, Long userId) {
        User user = userRepository.findById(userId).get();
        
        if (user.getTeam() == null || !user.getTeam().getTeamId().equals(challengerTeam.getTeamId())) {
            throw new UnauthorizedClassAccessException();
        }
    }
    
    private TeamCard validateAndLockCard(Long teamCardId, Team challengerTeam) {
        TeamCard teamCard = teamCardRepository.findById(teamCardId)
                .orElseThrow(() -> new CardNotFoundException("해당 팀 카드를 찾을 수 없습니다"));
        
        if (!teamCard.getTeam().getTeamId().equals(challengerTeam.getTeamId())) {
            throw new CardValidationException("해당 카드는 이 팀의 소유가 아닙니다");
        }
        
        if (teamCard.getIsLost()) {
            throw new CardValidationException("잃어버린 카드는 사용할 수 없습니다");
        }
        
        if (teamCard.getIsLocked()) {
            throw new CardValidationException("이미 사용 중인 카드입니다");
        }
        
        teamCard.setIsLocked(true);
        return teamCard;
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