package com.argo.backend.cardgame.service;

import com.argo.backend.cardgame.dto.battle.BattleOpponentDto;
import com.argo.backend.cardgame.dto.battle.BattleRequestDto;
import com.argo.backend.cardgame.dto.battle.BattleResponse;
import com.argo.backend.cardgame.dto.battle.BattleResponseDto;
import com.argo.backend.cardgame.exception.types.CardNotFoundException;
import com.argo.backend.cardgame.exception.types.CardValidationException;
import com.argo.backend.domain.cardgame.entity.Card;
import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import com.argo.backend.domain.cardgame.entity.TeamCard;
import com.argo.backend.domain.cardgame.enums.BattleStrategy;
import com.argo.backend.domain.cardgame.enums.MatchStatus;
import com.argo.backend.domain.cardgame.enums.ResultView;
import com.argo.backend.domain.cardgame.repository.CardGameMatchRepository;
import com.argo.backend.domain.cardgame.repository.TeamCardRepository;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.entity.UserTeam;
import com.argo.backend.domain.user.repository.UserRepository;
import com.argo.backend.domain.user.repository.UserTeamRepository;
import com.argo.backend.organization.exception.types.TeamNotFoundException;
import com.argo.backend.organization.exception.types.UnauthorizedClassAccessException;
import com.argo.backend.organization.exception.types.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BattleService {
    
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final UserTeamRepository userTeamRepository;
    private final TeamCardRepository teamCardRepository;
    private final CardGameMatchRepository cardGameMatchRepository;
    
    public List<BattleOpponentDto> getBattleOpponents(Long teamId, Long userId) {
        User user = validateAndGetUser(userId);
        Team currentTeam = validateTeamAccess(teamId, userId);
        
        // N+1 문제 해결: 팀과 리더를 한 번에 조회 (FETCH JOIN)
        List<Team> allTeams = teamRepository.findByClassRoomWithLeaderOrderByCreatedAtAsc(currentTeam.getClassRoom());
        
        return allTeams.stream()
                .filter(team -> !team.getTeamId().equals(teamId))
                .map(team -> {
                    String leaderName = getTeamLeaderName(team); // 이미 FETCH JOIN으로 로딩됨 (추가 쿼리 없음)
                    return BattleOpponentDto.from(team, leaderName);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public BattleResponse createBattle(BattleRequestDto request, Long userId) {
        User user = validateAndGetUser(userId);
        
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
        
        // 이 떄 FCM으로 요청 날려야함
        
        return BattleResponse.success("대전 신청이 성공적으로 전송되었습니다");
    }
    
    @Transactional
    public BattleResponse respondToBattle(Long matchId, BattleResponseDto request, Long userId) {
        User user = validateAndGetUser(userId);
        
        CardGameMatch match = cardGameMatchRepository.findById(matchId)
                .orElseThrow(() -> new CardNotFoundException("해당 대전을 찾을 수 없습니다"));
        
        if (match.getStatus() != MatchStatus.PENDING) {
            throw new CardValidationException("이미 처리된 대전입니다");
        }
        
        validateChallengedAccess(match.getChallengedTeam(), userId);
        
        if ("ACCEPT".equals(request.action())) {
            TeamCard challengedCard = validateAndLockCard(request.selectedCard().teamCardId(), match.getChallengedTeam());
            
            match.setChallengedCard(challengedCard);
            match.setChallengedStrategy(request.selectedCard().battleStance());
            match.setStartedAt(LocalDateTime.now());
            match.setStatus(MatchStatus.COMPLETED);

            processBattleResult(match);

            // 여기 FCM 알릶 보내야함 //

            return BattleResponse.success("대전 수락이 완료되었습니다");
        } else {
            match.setStatus(MatchStatus.CANCELLED);
            
            // 신청자 카드 잠금 해제
            if (match.getChallengerCard() != null) {
                match.getChallengerCard().setIsLocked(false);
            }
            
            return BattleResponse.success("대전을 거절하였습니다");
        }
    }
    
    private void validateChallengedAccess(Team challengedTeam, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        
        // N+1 문제 해결: Repository 메서드로 대체 (FETCH JOIN 사용)
        Optional<UserTeam> userTeamOpt = userTeamRepository.findActiveByUserIdAndClassId(userId, challengedTeam.getClassRoom().getClassId());
        Team userTeam = userTeamOpt.map(UserTeam::getTeam).orElse(null);
        
        if (userTeam == null || !userTeam.getTeamId().equals(challengedTeam.getTeamId())) {
            throw new UnauthorizedClassAccessException();
        }
    }
    
    private void processBattleResult(CardGameMatch match) {
        TeamCard challengerCard = match.getChallengerCard();
        TeamCard challengedCard = match.getChallengedCard();
        
        // 전략 가져오기
        var challengerStrategy = match.getChallengerStrategy();
        var challengedStrategy = match.getChallengedStrategy();
        
        // 전략에 따른 카드 능력치 계산
        double challengerPower = calculateCardPower(challengerCard, challengerStrategy);
        double challengedPower = calculateCardPower(challengedCard, challengedStrategy);
        
        // 대전 결과 계산
        BattleResult result = determineBattleResult(
            challengerPower, challengedPower, 
            challengerStrategy, challengedStrategy
        );
        
        // 점수 및 카드 상태 업데이트
        applyBattleResult(match, result);
        
        // 매치 상태 완료로 변경
        match.setStatus(MatchStatus.COMPLETED);
        match.setEndedAt(LocalDateTime.now());


        // 카드 잠금 해제
        if (challengerCard != null) {
            challengerCard.setIsLocked(false);
        }
        if (challengedCard != null) {
            challengedCard.setIsLocked(false);
        }
    }
    
    @Transactional
    public BattleResponse cancelBattle(Long matchId, Long userId) {
        User user = validateAndGetUser(userId);
        
        CardGameMatch match = cardGameMatchRepository.findById(matchId)
                .orElseThrow(() -> new CardNotFoundException("해당 대전을 찾을 수 없습니다"));
        
        if (match.getStatus() != MatchStatus.PENDING) {
            throw new CardValidationException("이미 처리된 대전은 취소할 수 없습니다");
        }
        
        validateChallengerAccess(match.getChallengerTeam(), userId);
        
        match.setStatus(MatchStatus.CANCELLED);
        
        // 신청자 카드 잠금 해제
        if (match.getChallengerCard() != null) {
            match.getChallengerCard().setIsLocked(false);
        }
        
        return BattleResponse.success("대전 신청이 취소되었습니다");
    }
    
    @Transactional
    public BattleResponse viewBattleResult(Long matchId, Long userId) {
        User user = validateAndGetUser(userId);
        
        CardGameMatch match = cardGameMatchRepository.findById(matchId)
                .orElseThrow(() -> new CardNotFoundException("해당 대전을 찾을 수 없습니다"));
        
        if (match.getStatus() != MatchStatus.COMPLETED) {
            throw new CardValidationException("완료된 대전만 결과를 확인할 수 있습니다");
        }
        
        Long classId = match.getChallengerTeam().getClassRoom().getClassId();
        Optional<UserTeam> userTeamOpt = userTeamRepository.findActiveByUserIdAndClassId(userId, classId);
        Team userTeam = userTeamOpt.map(UserTeam::getTeam).orElse(null);
        
        if (userTeam == null || 
            (!userTeam.getTeamId().equals(match.getChallengerTeam().getTeamId()) && 
             !userTeam.getTeamId().equals(match.getChallengedTeam().getTeamId()))) {
            throw new UnauthorizedClassAccessException();
        }
        
        Long userTeamId = userTeam.getTeamId();
        
        // resultView 상태 업데이트 로직
        updateResultViewStatus(match, userTeamId);
        
        return BattleResponse.success("대전 결과 확인이 처리되었습니다");
    }
    
    private void validateChallengerAccess(Team challengerTeam, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        
        // N+1 문제 해결: Repository 메서드로 대체 (FETCH JOIN 사용)
        Optional<UserTeam> userTeamOpt = userTeamRepository.findActiveByUserIdAndClassId(userId, challengerTeam.getClassRoom().getClassId());
        Team userTeam = userTeamOpt.map(UserTeam::getTeam).orElse(null);
        
        if (userTeam == null || !userTeam.getTeamId().equals(challengerTeam.getTeamId())) {
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
    
    private User validateAndGetUser(Long userId) {
        if (userId == null) {
            throw new UserNotFoundException();
        }
        
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }
    
    private Team validateTeamAccess(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);
        
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        
        // N+1 문제 해결: Repository 메서드로 대체 (FETCH JOIN 사용)
        Optional<UserTeam> userTeamOpt = userTeamRepository.findActiveByUserIdAndClassId(userId, team.getClassRoom().getClassId());
        Team userTeam = userTeamOpt.map(UserTeam::getTeam).orElse(null);
        
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
    
    private double calculateCardPower(TeamCard teamCard, BattleStrategy strategy) {
        Card card = teamCard.getCard();
        double tierWeight = teamCard.getTier().getWeight();
        
        if (strategy == BattleStrategy.ATTACK) {
            return card.getBaseAttack() * tierWeight;  // 최종 공격력
        } else {
            return card.getBaseDefense() * tierWeight; // 최종 방어력
        }
    }
    
    private BattleResult determineBattleResult(double challengerPower, double challengedPower, 
                                             BattleStrategy challengerStrategy, BattleStrategy challengedStrategy) {
        
        boolean challengerWins = challengerPower > challengedPower;
        boolean challengedWins = challengedPower > challengerPower;
        boolean isEqual = challengerPower == challengedPower;
        
        // 공격 vs 공격
        if (challengerStrategy == BattleStrategy.ATTACK && challengedStrategy == BattleStrategy.ATTACK) {
            if (isEqual) {
                return new BattleResult(false, 100, 100, true, true, true); // 공=공: 무승부, 양쪽 +100점, 양쪽 카드제거
            } else if (challengerWins) {
                return new BattleResult(true, 100, 0, false, false, false); // 공>공: 신청자 승리, +100점
            } else {
                return new BattleResult(false, 0, 100, false, false, false); // 공<공: 피신청자 승리, +100점
            }
        }
        
        // 공격 vs 수비
        if (challengerStrategy == BattleStrategy.ATTACK && challengedStrategy == BattleStrategy.DEFENSE) {
            if (isEqual) {
                return new BattleResult(false, 100, 0, true, false, true); // 공=수: 무승부, 공격자 카드제거+100점, 수비자 0점
            } else if (challengerWins) {
                return new BattleResult(true, 100, 0, false, false, false); // 공>수: 신청자(공격) 승리, +100점
            } else {
                return new BattleResult(false, 0, 50, false, false, false); // 공<수: 피신청자(수비) 승리, +50점
            }
        }
        
        // 수비 vs 공격  
        if (challengerStrategy == BattleStrategy.DEFENSE && challengedStrategy == BattleStrategy.ATTACK) {
            if (isEqual) {
                return new BattleResult(false, 0, 100, false, true, true); // 수=공: 무승부, 수비자 0점, 공격자 카드제거+100점
            } else if (challengerWins) {
                return new BattleResult(true, 50, 0, false, false, false); // 수>공: 신청자(수비) 승리, +50점
            } else {
                return new BattleResult(false, 0, 100, false, false, false); // 수<공: 피신청자(공격) 승리, +100점
            }
        }
        
        // 수비 vs 수비
        if (challengerStrategy == BattleStrategy.DEFENSE && challengedStrategy == BattleStrategy.DEFENSE) {
            if (isEqual) {
                return new BattleResult(false, 50, 50, true, true, true); // 수=수: 무승부, 양쪽 +50점, 양쪽 카드제거
            } else if (challengerWins) {
                return new BattleResult(true, 50, 0, false, false, false); // 수>수: 신청자 승리, +50점
            } else {
                return new BattleResult(false, 0, 50, false, false, false); // 수<수: 피신청자 승리, +50점
            }
        }
        
        return new BattleResult(false, 0, 0, false, false, false); // 기본값
    }
    
    private void applyBattleResult(CardGameMatch match, BattleResult result) {
        Team challengerTeam = match.getChallengerTeam();
        Team challengedTeam = match.getChallengedTeam();
        
        // 카드 능력치 비교 (무승부 판단용)
        TeamCard challengerCard = match.getChallengerCard();
        TeamCard challengedCard = match.getChallengedCard();

        // 팀 점수 및 통계 업데이트
        if (result.isDraw()) {
            // 무승부 처리
            updateTeamScoreForDraw(challengerTeam, result.challengerScore());
            updateTeamScoreForDraw(challengedTeam, result.challengedScore());
        } else {
            // 승부 결과에 따른 처리
            // 점수가 0인 팀은 패배자
            if (result.challengerScore() > 0) {
                updateTeamScore(challengerTeam, result.challengerScore(), true); // 승리자
            } else {
                updateTeamScore(challengerTeam, 0, false); // 패배자
            }
            
            if (result.challengedScore() > 0) {
                updateTeamScore(challengedTeam, result.challengedScore(), true); // 승리자
            } else {
                updateTeamScore(challengedTeam, 0, false); // 패배자
            }
        }
        
        // 카드 제거 처리
        if (result.challengerLoseCard() && match.getChallengerCard() != null) {
            match.getChallengerCard().setIsLost(true);
            teamCardRepository.save(match.getChallengerCard());
        }
        if (result.challengedLoseCard() && match.getChallengedCard() != null) {
            match.getChallengedCard().setIsLost(true);
            teamCardRepository.save(match.getChallengedCard());
        }
        
        // 승부 결과 설정
        if (result.isDraw()) {
            // 무승부
            match.setWinnerTeam(null);
            match.setLoserTeam(null);
            match.setDraw(true);
        } else if (result.challengerWins()) {
            match.setWinnerTeam(challengerTeam);
            match.setLoserTeam(challengedTeam);
            match.setDraw(false);
        } else {
            match.setWinnerTeam(challengedTeam);
            match.setLoserTeam(challengerTeam);
            match.setDraw(false);
        }

    }
    
    private void updateTeamScore(Team team, int score, boolean isWinner) {
        if (score > 0) {
            team.initializeGameResult();
            
            if (isWinner) {
                team.getGameResult().addWin(score);
            } else {
                // 점수는 받지만 패배한 경우
                team.getGameResult().addLoss();
                team.getGameResult().setTotalPoints(team.getGameResult().getTotalPoints() + score);
            }
        } else {
            // 점수를 못받은 패배자
            team.initializeGameResult();
            team.getGameResult().addLoss();
        }
    }
    
    private void updateTeamScoreForDraw(Team team, int score) {
        team.initializeGameResult();
        team.getGameResult().addDraw();
        team.getGameResult().setTotalPoints(team.getGameResult().getTotalPoints() + score);
    }
    
    private void updateResultViewStatus(CardGameMatch match, Long viewerTeamId) {
        Long challengerTeamId = match.getChallengerTeam().getTeamId();
        Long challengedTeamId = match.getChallengedTeam().getTeamId();
        
        boolean isChallengerViewing = viewerTeamId.equals(challengerTeamId);
        boolean isChallengedViewing = viewerTeamId.equals(challengedTeamId);
        
        ResultView currentStatus = match.getResultView();
        
        switch (currentStatus) {
            case BOTH_NOT_SEE:
                if (isChallengerViewing) {
                    match.setResultView(ResultView.SEE_CHALLENGER);
                } else if (isChallengedViewing) {
                    match.setResultView(ResultView.SEE_CHALLENGED);
                }
                break;
                
            case SEE_CHALLENGER:
                if (isChallengedViewing) {
                    match.setResultView(ResultView.BOTH_SEE);
                }
                break;
                
            case SEE_CHALLENGED:
                if (isChallengerViewing) {
                    match.setResultView(ResultView.BOTH_SEE);
                }
                break;
                
            case BOTH_SEE:
                break;
        }
    }
    
    // 대전 결과를 담는 내부 클래스
    private record BattleResult(
        boolean challengerWins,
        int challengerScore, 
        int challengedScore,
        boolean challengerLoseCard,
        boolean challengedLoseCard,
        boolean isDraw
    ) {}
}