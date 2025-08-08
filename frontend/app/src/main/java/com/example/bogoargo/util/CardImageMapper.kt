package com.example.bogoargo.util

import com.example.bogoargo.R
import com.example.bogoargo.domain.model.CardTier

object CardImageMapper {
    
    // 카드 ID별 캐릭터 이미지 매핑
    private val characterImageMap = mapOf(
        1L to R.drawable.monster_1, // 불사조
        2L to R.drawable.monster_2, // 그림자 늑대
        3L to R.drawable.monster_3, // 치유의 요정
        4L to R.drawable.monster_4, // 바위 골렘
        5L to R.drawable.monster_1, // 번개 마법사
        6L to R.drawable.monster_2, // 숲의 수호자
        7L to R.drawable.monster_3, // 얼음 용
        8L to R.drawable.monster_4  // 기사
    )
    
    // 레어도별 테두리 이미지 매핑
    private val borderImageMap = mapOf(
        CardTier.COMMON to R.drawable.card_common,
        CardTier.RARE to R.drawable.card_rare,
        CardTier.EPIC to R.drawable.card_epic,
        CardTier.LEGENDARY to R.drawable.card_legendary
    )
    
    /**
     * 카드 ID를 기반으로 캐릭터 이미지 리소스 ID를 반환합니다.
     * @param cardId 카드 ID
     * @return 캐릭터 이미지의 리소스 ID, 매핑되지 않은 경우 기본 이미지 반환
     */
    fun getCharacterImage(cardId: Long): Int {
        return characterImageMap[cardId] ?: R.drawable.my_temp_icon
    }
    
    /**
     * 카드 레어도를 기반으로 테두리 이미지 리소스 ID를 반환합니다.
     * @param rarity 카드 레어도
     * @return 테두리 이미지의 리소스 ID, 매핑되지 않은 경우 기본 이미지 반환
     */
    fun getBorderImage(rarity: CardTier): Int {
        return borderImageMap[rarity] ?: R.drawable.my_temp_icon
    }
    
    /**
     * 새 카드 ID에 대한 캐릭터 이미지 매핑이 존재하는지 확인합니다.
     * @param cardId 확인할 카드 ID
     * @return 매핑이 존재하면 true, 그렇지 않으면 false
     */
    fun hasCharacterImage(cardId: Long): Boolean {
        return characterImageMap.containsKey(cardId)
    }
    
    /**
     * 현재 지원하는 모든 카드 ID 목록을 반환합니다.
     * @return 지원하는 카드 ID 목록
     */
    fun getSupportedCardIds(): Set<Long> {
        return characterImageMap.keys
    }
}