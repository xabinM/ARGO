package com.example.bogoargo.ui.screens.mission.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography

@Composable
fun QuizAnswerCard(
    choice: String,
    index: Int,
    isSelected: Boolean,
    isCorrectAnswer: Boolean,
    isAnswerSubmitted: Boolean,
    showCorrectAnswer: Boolean,
    canSubmitAnswer: Boolean,
    onAnswerSelected: (Int) -> Unit
) {
    val backgroundColor = when {
        // 2차 실패 시에만 정답을 초록색으로 표시
        showCorrectAnswer && isCorrectAnswer -> NatureColors.leafGreen.copy(alpha = 0.3f)
        // 1차든 2차든 선택한 오답은 빨간색으로 표시
        isAnswerSubmitted && isSelected && !isCorrectAnswer -> Color.Red.copy(alpha = 0.3f)
        // 기본 선택 상태
        isSelected -> NatureColors.forestGreen.copy(alpha = 0.2f)
        else -> NatureColors.lightBeige.copy(alpha = 0.1f)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { if (canSubmitAnswer && !isAnswerSubmitted) onAnswerSelected(index) },
                role = Role.RadioButton,
                enabled = canSubmitAnswer && !isAnswerSubmitted
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                enabled = canSubmitAnswer && !isAnswerSubmitted,
                colors = RadioButtonDefaults.colors(
                    selectedColor = NatureColors.forestGreen
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = choice,
                style = NatureTypography.bodyMedium,
                color = NatureColors.earthBrown
            )
            
            if (isAnswerSubmitted) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = when {
                        // 2차 실패 시에만 정답에 ✅ 표시
                        showCorrectAnswer && isCorrectAnswer -> "✅"
                        // 1차든 2차든 선택한 오답에는 ❌ 표시
                        isSelected && !isCorrectAnswer -> "❌"
                        else -> ""
                    },
                    style = NatureTypography.bodyLarge
                )
            }
        }
    }
}