package com.argo.backend.cardgame.controller;

import com.argo.backend.cardgame.dto.CommonApiResponse;
import com.argo.backend.cardgame.dto.battle.BattleOpponentDto;
import com.argo.backend.cardgame.dto.battle.BattleRequestDto;
import com.argo.backend.cardgame.dto.battle.BattleResponse;
import com.argo.backend.cardgame.dto.battle.BattleResponseDto;
import com.argo.backend.cardgame.dto.card.CardInfoResponse;
import com.argo.backend.cardgame.dto.history.BattleHistoryResponse;
import com.argo.backend.cardgame.dto.stats.TeamStatsDto;
import com.argo.backend.cardgame.dto.teamcard.TeamCardCollectionResponse;
import com.argo.backend.cardgame.service.BattleHistoryService;
import com.argo.backend.cardgame.service.BattleService;
import com.argo.backend.cardgame.service.CardService;
import com.argo.backend.cardgame.service.TeamCardService;
import com.argo.backend.cardgame.service.TeamStatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CardController {
    
    private final CardService cardService;
    private final TeamCardService teamCardService;
    private final BattleService battleService;
    private final BattleHistoryService battleHistoryService;
    private final TeamStatsService teamStatsService;
    
    /**
     * 카드 기본 정보 조회 (유효성 검증 목적)
     * GET /api/cards/{cardId}
     */
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<CommonApiResponse<CardInfoResponse>> getCardInfo(
            @PathVariable Long cardId,
            @AuthenticationPrincipal Long userId) {
        
        CardInfoResponse response = cardService.getCardInfo(cardId, userId);
        
        return ResponseEntity.ok(new CommonApiResponse<>(true, "카드 정보 조회 성공", response));
    }
    
    /**
     * 팀 카드 컬렉션 조회 (전체)
     * GET /api/teams/{teamId}/cards
     */
    @GetMapping("/teams/{teamId}/cards")
    public ResponseEntity<CommonApiResponse<TeamCardCollectionResponse>> getTeamCardCollection(
            @PathVariable Long teamId,
            @AuthenticationPrincipal Long userId) {
        
        TeamCardCollectionResponse response = teamCardService.getTeamCardCollection(teamId, userId);
        
        return ResponseEntity.ok(new CommonApiResponse<>(true, "팀 카드 컬렉션 조회 성공", response));
    }
    
    /**
     * 대전 가능한 팀 목록 조회
     * GET /api/teams/{teamId}/battle-opponents
     */
    @GetMapping("/teams/{teamId}/battle-opponents")
    public ResponseEntity<CommonApiResponse<List<BattleOpponentDto>>> getBattleOpponents(
            @PathVariable Long teamId,
            @AuthenticationPrincipal Long userId) {
        
        List<BattleOpponentDto> response = battleService.getBattleOpponents(teamId, userId);
        
        return ResponseEntity.ok(new CommonApiResponse<>(true, "대전 가능한 팀 목록 조회 성공", response));
    }
    
    /**
     * 대전 신청
     * POST /api/battles
     */
    @PostMapping("/battles")
    public ResponseEntity<CommonApiResponse<BattleResponse>> createBattle(
            @Valid @RequestBody BattleRequestDto request,
            @AuthenticationPrincipal Long userId) {
        
        BattleResponse response = battleService.createBattle(request, userId);
        
        return ResponseEntity.ok(new CommonApiResponse<>(true, response.message(), response));
    }
    
    /**
     * 대전 응답 (수락/거절)
     * PUT /api/battles/{matchId}/respond
     */
    @PutMapping("/battles/{matchId}/respond")
    public ResponseEntity<CommonApiResponse<BattleResponse>> respondToBattle(
            @PathVariable Long matchId,
            @Valid @RequestBody BattleResponseDto request,
            @AuthenticationPrincipal Long userId) {
        
        BattleResponse response = battleService.respondToBattle(matchId, request, userId);
        
        return ResponseEntity.ok(new CommonApiResponse<>(true, response.message(), response));
    }
    
    /**
     * 대전 신청 취소
     * DELETE /api/battles/{matchId}
     */
    @DeleteMapping("/battles/{matchId}")
    public ResponseEntity<CommonApiResponse<BattleResponse>> cancelBattle(
            @PathVariable Long matchId,
            @AuthenticationPrincipal Long userId) {
        
        BattleResponse response = battleService.cancelBattle(matchId, userId);
        
        return ResponseEntity.ok(new CommonApiResponse<>(true, response.message(), response));
    }
    
    /**
     * 팀 대전 기록 조회
     * GET /api/teams/{teamId}/battle-history
     */
    @GetMapping("/teams/{teamId}/battle-history")
    public ResponseEntity<CommonApiResponse<BattleHistoryResponse>> getBattleHistory(
            @PathVariable Long teamId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId) {
        
        Pageable pageable = PageRequest.of(page, size);
        BattleHistoryResponse response = battleHistoryService.getBattleHistory(teamId, userId, pageable);
        
        return ResponseEntity.ok(new CommonApiResponse<>(true, "대전 기록 조회 성공", response));
    }
    
    /**
     * 대전 결과 확인
     * PUT /api/battles/{matchId}/view-result
     */
    @PutMapping("/battles/{matchId}/view-result")
    public ResponseEntity<CommonApiResponse<BattleResponse>> viewBattleResult(
            @PathVariable Long matchId,
            @AuthenticationPrincipal Long userId) {
        
        BattleResponse response = battleService.viewBattleResult(matchId, userId);
        
        return ResponseEntity.ok(new CommonApiResponse<>(true, response.message(), response));
    }
    
    /**
     * 팀 카드 게임 통계 조회
     * GET /api/teams/{teamId}/stats
     */
    @GetMapping("/teams/{teamId}/stats")
    public ResponseEntity<CommonApiResponse<TeamStatsDto>> getTeamStats(
            @PathVariable Long teamId,
            @AuthenticationPrincipal Long userId) {
        
        TeamStatsDto response = teamStatsService.getTeamStats(teamId, userId);
        
        return ResponseEntity.ok(new CommonApiResponse<>(true, "팀 통계 조회 성공", response));
    }
}