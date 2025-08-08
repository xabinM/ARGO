package com.example.bogoargo.data.dto.request

// 대전 신청 요청 DTO (백엔드 BattleRequestDto와 일치)
data class BattleRequestDto(
    val challengerTeamId: Long,
    val challengedTeamId: Long,
    val selectedCard: SelectedCardDto
)

// 대전 응답 요청 DTO (백엔드 BattleResponseDto와 일치)  
data class BattleResponseDto(
    val action: String, // "accept" 또는 "reject"
    val selectedCard: SelectedCardDto?
)

// 선택한 카드 DTO (백엔드 SelectedCardDto와 일치)
data class SelectedCardDto(
    val teamCardId: Long,
    val battleStance: String // BattleStrategy enum string ("ATTACK", "DEFENSE")
)