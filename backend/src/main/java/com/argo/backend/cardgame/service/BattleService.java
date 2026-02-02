package com.argo.backend.cardgame.service;

import com.argo.backend.cardgame.dto.BattleStatusChangedEvent;
import com.argo.backend.cardgame.dto.battle.BattleOpponentDto;
import com.argo.backend.cardgame.dto.battle.BattleRequestDto;
import com.argo.backend.cardgame.dto.battle.BattleResponse;
import com.argo.backend.cardgame.dto.battle.BattleResponseDto;
import com.argo.backend.cardgame.exception.types.*;
import com.argo.backend.cardgame.scheduler.BattleExpirationUtil;
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
import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.organization.exception.types.TeamNotFoundException;
import com.argo.backend.organization.exception.types.UnauthorizedClassAccessException;
import com.argo.backend.organization.exception.types.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
    private final BattleExpirationUtil battleExpirationUtil;
    private final ApplicationEventPublisher eventPublisher;

    public List<BattleOpponentDto> getBattleOpponents(Long teamId, Long userId) {

        Team currentTeam = validateTeamAccess(teamId, userId);

        List<Team> allTeams = teamRepository.findByClassRoomWithLeaderOrderByCreatedAtAsc(currentTeam.getClassRoom());

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

        Team challengerTeam = teamRepository.findById(request.challengerTeamId())
                .orElseThrow(TeamNotFoundException::new);
        Team challengedTeam = teamRepository.findById(request.challengedTeamId())
                .orElseThrow(TeamNotFoundException::new);

        validateChallengeAccess(challengerTeam, userId);

        TeamCard selectedCard = validateAndLockCard(request.selectedCard().teamCardId(), challengerTeam);

        CardGameMatch match = CardGameMatch.from(
                challengerTeam,
                challengedTeam,
                selectedCard,
                request.selectedCard().battleStance()
        );

        CardGameMatch savedMatch = cardGameMatchRepository.save(match);

        battleExpirationUtil.registerBattleExpiration(savedMatch.getMatchId(), 30000);

        User challengedLeader = challengedTeam.getLeader();
        if (challengedLeader != null && challengedLeader.getFcmToken() != null) {
            eventPublisher.publishEvent(new BattleStatusChangedEvent(
                    challengedLeader.getFcmToken(),
                    challengerTeam.getTeamName(),
                    MatchStatus.PENDING
            ));
        }

        return BattleResponse.success(ResponseMessage.BATTLE_REQUEST_SENT.getMessage());
    }

    @Transactional
    public BattleResponse respondToBattle(Long matchId, BattleResponseDto request, Long userId) {
        try {
            CardGameMatch match = cardGameMatchRepository.findById(matchId)
                    .orElseThrow(BattleNotFoundException::new);

            if (match.getStatus() != MatchStatus.PENDING) {
                throw new AlreadyProcessedBattleException();
            }

            validateChallengeAccess(match.getChallengedTeam(), userId);

            User challengerLeader = match.getChallengerTeam().getLeader();

            if ("ACCEPT".equals(request.action())) {
                TeamCard challengedCard = validateAndLockCard(request.selectedCard().teamCardId(), match.getChallengedTeam());

                match.setChallengedCard(challengedCard);
                match.setChallengedStrategy(request.selectedCard().battleStance());
                match.setStartedAt(LocalDateTime.now());
                match.setStatus(MatchStatus.COMPLETED);

                processBattleResult(match);
                cardGameMatchRepository.save(match);

                if (challengerLeader != null && challengerLeader.getFcmToken() != null) {
                    eventPublisher.publishEvent(new BattleStatusChangedEvent(
                            challengerLeader.getFcmToken(),
                            match.getChallengedTeam().getTeamName(),
                            MatchStatus.COMPLETED
                    ));
                }

                return BattleResponse.success(ResponseMessage.BATTLE_ACCEPTED.getMessage());
            } else { // REJECT
                match.setStatus(MatchStatus.CANCELLED);
                cardGameMatchRepository.save(match);
                if (match.getChallengerCard() != null) {
                    match.getChallengerCard().setIsLocked(false);
                }

                if (challengerLeader != null && challengerLeader.getFcmToken() != null) {
                    eventPublisher.publishEvent(new BattleStatusChangedEvent(
                            challengerLeader.getFcmToken(),
                            match.getChallengedTeam().getTeamName(),
                            MatchStatus.CANCELLED
                    ));
                }

                return BattleResponse.success(ResponseMessage.BATTLE_REJECTED.getMessage());
            }
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new AlreadyProcessedBattleException();
        }
    }

    @Transactional
    public synchronized void expireMatch(Long matchId) {
        CardGameMatch match = cardGameMatchRepository.findById(matchId).orElse(null);

        if (match != null && match.getStatus() == MatchStatus.PENDING) {
            match.setStatus(MatchStatus.EXPIRED);

            if (match.getChallengerCard() != null) {
                match.getChallengerCard().setIsLocked(false);
            }
            cardGameMatchRepository.save(match);
        }
    }

    private void validateChallengeAccess(Team challengedTeam, Long userId) {
        Optional<UserTeam> userTeamOpt = userTeamRepository.findActiveByUserIdAndClassId(userId, challengedTeam.getClassRoom().getClassId());
        Team userTeam = userTeamOpt.map(UserTeam::getTeam).orElse(null);

        if (userTeam == null || !userTeam.getTeamId().equals(challengedTeam.getTeamId())) {
            throw new UnauthorizedClassAccessException();
        }
    }

    private void processBattleResult(CardGameMatch match) {
        TeamCard challengerCard = match.getChallengerCard();
        TeamCard challengedCard = match.getChallengedCard();

        var challengerStrategy = match.getChallengerStrategy();
        var challengedStrategy = match.getChallengedStrategy();

        double challengerPower = calculateCardPower(challengerCard, challengerStrategy);
        double challengedPower = calculateCardPower(challengedCard, challengedStrategy);

        BattleResult result = determineBattleResult(
                challengerPower, challengedPower,
                challengerStrategy, challengedStrategy
        );

        applyBattleResult(match, result);

        match.setStatus(MatchStatus.COMPLETED);
        match.setEndedAt(LocalDateTime.now());

        challengerCard.setIsLocked(false);
        challengedCard.setIsLocked(false);
    }

    @Transactional
    public BattleResponse cancelBattle(Long matchId, Long userId) {
        try {
            CardGameMatch match = cardGameMatchRepository.findById(matchId)
                    .orElseThrow(BattleNotFoundException::new);

            if (match.getStatus() != MatchStatus.PENDING) {
                throw new ProcessedBattleCancelException();
            }

            validateChallengeAccess(match.getChallengerTeam(), userId);

            match.setStatus(MatchStatus.CANCELLED);

            if (match.getChallengerCard() != null) {
                match.getChallengerCard().setIsLocked(false);
            }
            cardGameMatchRepository.save(match);

            return BattleResponse.success(ResponseMessage.BATTLE_CANCELLED.getMessage());
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ProcessedBattleCancelException();
        }
    }

    @Transactional
    public BattleResponse viewBattleResult(Long matchId, Long userId) {
        CardGameMatch match = cardGameMatchRepository.findById(matchId)
                .orElseThrow(BattleNotFoundException::new);

        if (match.getStatus() != MatchStatus.COMPLETED) {
            throw new IncompleteBattleViewException();
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

        updateResultViewStatus(match, userTeamId);
        cardGameMatchRepository.save(match);
        return BattleResponse.success(ResponseMessage.BATTLE_RESULT_VIEWED.getMessage());
    }

    private TeamCard validateAndLockCard(Long teamCardId, Team challengerTeam) {
        TeamCard teamCard = teamCardRepository.findById(teamCardId)
                .orElseThrow(TeamCardNotFoundException::new);

        if (!teamCard.getTeam().getTeamId().equals(challengerTeam.getTeamId())) {
            throw new CardNotOwnedByTeamException();
        }

        if (teamCard.getIsLost()) {
            throw new LostCardUsageException();
        }

        if (teamCard.getIsLocked()) {
            throw new LockedCardUsageException();
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
            return card.getBaseAttack() * tierWeight;
        } else {
            return card.getBaseDefense() * tierWeight;
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
                return new BattleResult(false, 50, 50, true, true, true); // 공=공: 무승부, 양쪽 +50점, 양쪽 카드제거
            } else if (challengerWins) {
                return new BattleResult(true, 100, 0, false, true, false); // 공>공: 신청자 승리, +100점 , 패배자 카드제거
            } else {
                return new BattleResult(false, 0, 100, true, false, false); // 공<공: 피신청자 승리, +100점, 패배자 카드제거
            }
        }

        // 공격 vs 수비
        if (challengerStrategy == BattleStrategy.ATTACK && challengedStrategy == BattleStrategy.DEFENSE) {
            if (isEqual) {
                return new BattleResult(false, 50, 0, true, false, true); // 공=수: 무승부, 공격자 카드제거+50점, 수비자 0점
            } else if (challengerWins) {
                return new BattleResult(true, 100, 0, false, false, false); // 공>수: 신청자(공격) 승리, +100점
            } else {
                return new BattleResult(false, 0, 50, true, false, false); // 공<수: 피신청자(수비) 승리, +50점
            }
        }

        // 수비 vs 공격
        if (challengerStrategy == BattleStrategy.DEFENSE && challengedStrategy == BattleStrategy.ATTACK) {
            if (isEqual) {
                return new BattleResult(false, 0, 50, false, true, true); // 수=공: 무승부, 수비자 0점, 공격자 카드제거+50점
            } else if (challengerWins) {
                return new BattleResult(true, 50, 0, false, true, false); // 수>공: 신청자(수비) 승리, +50점 / 공 카드 제거
            } else {
                return new BattleResult(false, 0, 100, false, false, false); // 수<공: 피신청자(공격) 승리, +100점
            }
        }

        // 수비 vs 수비
        if (challengerStrategy == BattleStrategy.DEFENSE && challengedStrategy == BattleStrategy.DEFENSE) {
            if (isEqual) {
                return new BattleResult(false, 0, 0, true, true, true); // 수=수: 무승부, 양쪽 +0점, 양쪽 카드제거
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

        TeamCard challengerCard = match.getChallengerCard();
        TeamCard challengedCard = match.getChallengedCard();

        if (result.isDraw()) {
            updateTeamScoreForDraw(challengerTeam, result.challengerScore());
            updateTeamScoreForDraw(challengedTeam, result.challengedScore());
        } else {
            if (result.challengerScore() > 0) {
                updateTeamScore(challengerTeam, result.challengerScore(), true);
            } else {
                updateTeamScore(challengerTeam, 0, false);
            }

            if (result.challengedScore() > 0) {
                updateTeamScore(challengedTeam, result.challengedScore(), true);
            } else {
                updateTeamScore(challengedTeam, 0, false);
            }
        }

        if (result.challengerLoseCard() && match.getChallengerCard() != null) {
            match.getChallengerCard().setIsLost(true);
            teamCardRepository.save(match.getChallengerCard());
        }
        if (result.challengedLoseCard() && match.getChallengedCard() != null) {
            match.getChallengedCard().setIsLost(true);
            teamCardRepository.save(match.getChallengedCard());
        }

        if (result.isDraw()) {
            match.setWinnerTeam(null);
            match.setLoserTeam(null);
            match.setDraw(true);
        } else if (result.challengerWins()) {
            match.setWinnerTeam(challengerTeam);
            match.setWinnerCard(challengerCard);
            match.setLoserTeam(challengedTeam);
            match.setLoserCard(challengedCard);
            match.setDraw(false);
        } else {
            match.setWinnerTeam(challengedTeam);
            match.setWinnerCard(challengedCard);
            match.setLoserTeam(challengerTeam);
            match.setLoserCard(challengerCard);
            match.setDraw(false);
        }
    }

    private void updateTeamScore(Team team, int score, boolean isWinner) {
        team.initializeGameResult();
        if (isWinner) {
            team.getGameResult().addWin(score);
        } else {
            team.getGameResult().addLoss();
            team.getGameResult().setTotalPoints(team.getGameResult().getTotalPoints() + score);
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

    private record BattleResult(
            boolean challengerWins,
            int challengerScore,
            int challengedScore,
            boolean challengerLoseCard,
            boolean challengedLoseCard,
            boolean isDraw
    ) {
    }
}
