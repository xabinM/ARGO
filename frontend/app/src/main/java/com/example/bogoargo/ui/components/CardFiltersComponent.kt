package com.example.bogoargo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bogoargo.domain.model.GameCard
import com.example.bogoargo.domain.model.CardTier
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography

// CardStatusFilter enum 정의 (CardCollectionScreen에서 이동)
enum class CardStatusFilter(val displayName: String) {
    ALL("전체"),
    ACTIVE("활성"),      // !isLost && !isLocked
    LOCKED("사용중"),    // isLocked == true
    LOST("제거됨")       // isLost == true
}

@Composable
fun CardFiltersSection(
    cards: List<GameCard>,
    selectedRarity: CardTier? = null,
    selectedStatus: CardStatusFilter? = null,
    showStatusFilter: Boolean = false,
    onRaritySelected: ((CardTier?) -> Unit)? = null,
    onStatusSelected: ((CardStatusFilter) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 상태 필터 (컬렉션 화면에서만 표시)
        if (showStatusFilter && onStatusSelected != null && selectedStatus != null) {
            StatusFilterRow(
                selectedStatus = selectedStatus,
                onStatusSelected = onStatusSelected,
                cards = cards
            )
        }
        
        // 레어도 필터 (공통)
        onRaritySelected?.let { handler ->
            RarityFilterRow(
                selectedRarity = selectedRarity,
                onRaritySelected = handler
            )
        }
    }
}

@Composable
fun StatusFilterRow(
    selectedStatus: CardStatusFilter,
    onStatusSelected: (CardStatusFilter) -> Unit,
    cards: List<GameCard>
) {
    val activeCount = cards.count { it.isActive }
    val lockedCount = cards.count { it.isLocked }
    val lostCount = cards.count { it.isLost }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CardStatusFilter.entries.forEach { status ->
            val count = when (status) {
                CardStatusFilter.ALL -> cards.size
                CardStatusFilter.ACTIVE -> activeCount
                CardStatusFilter.LOCKED -> lockedCount
                CardStatusFilter.LOST -> lostCount
            }
            
            FilterChip(
                onClick = { onStatusSelected(status) },
                label = { 
                    Text(
                        "${status.displayName} ($count)", 
                        style = NatureTypography.labelMedium
                    ) 
                },
                selected = selectedStatus == status,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (status) {
                        CardStatusFilter.ALL -> NatureColors.forestGreen
                        CardStatusFilter.ACTIVE -> Color(0xFF4CAF50)
                        CardStatusFilter.LOCKED -> Color(0xFFFF9800)
                        CardStatusFilter.LOST -> Color.Gray
                    },
                    selectedLabelColor = Color.White,
                    containerColor = NatureColors.whiteTransparent,
                    labelColor = NatureColors.earthBrown
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedStatus == status,
                    borderColor = when (status) {
                        CardStatusFilter.ALL -> NatureColors.forestGreen.copy(alpha = 0.3f)
                        CardStatusFilter.ACTIVE -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                        CardStatusFilter.LOCKED -> Color(0xFFFF9800).copy(alpha = 0.3f)
                        CardStatusFilter.LOST -> Color.Gray.copy(alpha = 0.3f)
                    },
                    selectedBorderColor = when (status) {
                        CardStatusFilter.ALL -> NatureColors.forestGreen
                        CardStatusFilter.ACTIVE -> Color(0xFF4CAF50)
                        CardStatusFilter.LOCKED -> Color(0xFFFF9800)
                        CardStatusFilter.LOST -> Color.Gray
                    }
                )
            )
        }
    }
}

@Composable
fun RarityFilterRow(
    selectedRarity: CardTier?,
    onRaritySelected: (CardTier?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            onClick = { onRaritySelected(null) },
            label = { Text("전체", style = NatureTypography.labelMedium) },
            selected = selectedRarity == null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NatureColors.forestGreen,
                selectedLabelColor = Color.White,
                containerColor = NatureColors.whiteTransparent,
                labelColor = NatureColors.earthBrown
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selectedRarity == null,
                borderColor = NatureColors.forestGreen.copy(alpha = 0.3f),
                selectedBorderColor = NatureColors.forestGreen
            )
        )
        CardTier.entries.forEach { rarity ->
            FilterChip(
                onClick = { onRaritySelected(rarity) },
                label = { Text(rarity.displayName, style = NatureTypography.labelMedium) },
                selected = selectedRarity == rarity,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(android.graphics.Color.parseColor(rarity.color)),
                    selectedLabelColor = Color.White,
                    containerColor = NatureColors.whiteTransparent,
                    labelColor = NatureColors.earthBrown
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedRarity == rarity,
                    borderColor = Color(android.graphics.Color.parseColor(rarity.color)).copy(alpha = 0.3f),
                    selectedBorderColor = Color(android.graphics.Color.parseColor(rarity.color))
                )
            )
        }
    }
}