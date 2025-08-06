package com.argo.backend.cardgame.controller;

import com.argo.backend.cardgame.dto.CommonApiResponse;
import com.argo.backend.cardgame.dto.card.CardInfoResponse;
import com.argo.backend.cardgame.dto.teamcard.TeamCardCollectionResponse;
import com.argo.backend.cardgame.service.CardService;
import com.argo.backend.cardgame.service.TeamCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CardController {
    
    private final CardService cardService;
    private final TeamCardService teamCardService;
    
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
}