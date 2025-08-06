package com.argo.backend.cardgame.dto.history;

import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import com.argo.backend.domain.cardgame.enums.MatchStatus;
import com.argo.backend.domain.cardgame.enums.ResultView;

import java.time.LocalDateTime;

public record BattleHistoryDto(
        Long matchId,
        Long challengerTeamId,
        Long challengedTeamId,
        String challengerTeamName,
        String challengedTeamName,
        MatchStatus status,
        ResultView resultView,
        Long winnerTeamId,
        Long loserTeamId,
        boolean isDraw,
        BattleCardDto myCard,
        BattleCardDto opponentCard,
        LocalDateTime createdAt,
        LocalDateTime endedAt
) {
    public static BattleHistoryDto from(CardGameMatch match, Long currentTeamId) {
        boolean isChallenger = match.getChallengerTeam().getTeamId().equals(currentTeamId);
        
        // 대전 진행 상태에 따른 카드 정보 처리
        // PENDING: 신청자 카드만 있음 (상대방이 아직 응답 안함)
        // COMPLETED/CANCELLED: 모든 카드 정보 있음
        BattleCardDto myCard = null;
        BattleCardDto opponentCard = null;
        
        if (isChallenger && match.getChallengerCard() != null) {
            // 내가 신청자인 경우: 신청자 카드가 내 카드
            myCard = BattleCardDto.from(match.getChallengerCard(), match.getChallengerStrategy());
        } else if (!isChallenger && match.getChallengedCard() != null) {
            // 내가 도전받은 팀인 경우: 도전받은 카드가 내 카드 (응답 후에만 존재)
            myCard = BattleCardDto.from(match.getChallengedCard(), match.getChallengedStrategy());
        }
        
        if (isChallenger && match.getChallengedCard() != null) {
            // 내가 신청자인 경우: 도전받은 카드가 상대방 카드 (응답 후에만 존재)
            opponentCard = BattleCardDto.from(match.getChallengedCard(), match.getChallengedStrategy());
        } else if (!isChallenger && match.getChallengerCard() != null) {
            // 내가 도전받은 팀인 경우: 신청자 카드가 상대방 카드
            opponentCard = BattleCardDto.from(match.getChallengerCard(), match.getChallengerStrategy());
        }
        
        return new BattleHistoryDto(
                match.getMatchId(),
                match.getChallengerTeam().getTeamId(),
                match.getChallengedTeam().getTeamId(),
                match.getChallengerTeam().getTeamName(),
                match.getChallengedTeam().getTeamName(),
                match.getStatus(),
                ResultView.BOTH_NOT_SEE, // 기본값
                match.getWinnerTeam() != null ? match.getWinnerTeam().getTeamId() : null,
                match.getLoserTeam() != null ? match.getLoserTeam().getTeamId() : null,
                match.isDraw(),
                myCard,
                opponentCard,
                match.getCreatedAt(),
                match.getEndedAt()
        );
    }
}