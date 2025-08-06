package com.argo.backend.cardgame.controller;

import com.argo.backend.cardgame.dto.CommonApiResponse;
import com.argo.backend.cardgame.dto.battle.BattleOpponentDto;
import com.argo.backend.cardgame.dto.battle.BattleRequestDto;
import com.argo.backend.cardgame.dto.battle.BattleResponse;
import com.argo.backend.cardgame.dto.battle.BattleResponseDto;
import com.argo.backend.cardgame.dto.card.CardInfoResponse;
import com.argo.backend.cardgame.dto.teamcard.TeamCardCollectionResponse;
import com.argo.backend.cardgame.service.BattleService;
import com.argo.backend.cardgame.service.CardService;
import com.argo.backend.cardgame.service.TeamCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}