package com.argo.backend.cardgame.dto.history;

import org.springframework.data.domain.Page;

import java.util.List;

public record BattleHistoryResponse(
        List<BattleHistoryDto> battles,
        long totalElements,
        int totalPages,
        int currentPage
) {
    public static BattleHistoryResponse from(Page<BattleHistoryDto> page) {
        return new BattleHistoryResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }
}