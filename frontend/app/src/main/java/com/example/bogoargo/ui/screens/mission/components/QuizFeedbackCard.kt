package com.example.bogoargo.ui.screens.mission.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography

@Composable
fun QuizFeedbackCard(
    isFirstAttemptFailed: Boolean,
    showCorrectAnswer: Boolean,
    explanation: String
) {
    when {
        // 1차 실패 시 안내 메시지
        isFirstAttemptFailed && !showCorrectAnswer -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = NatureColors.softOrange.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "😔 아쉬워요!",
                        style = NatureTypography.titleSmall,
                        color = NatureColors.earthBrown
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "다시 한번 생각해보세요. 한 번 더 기회가 있어요!",
                        style = NatureTypography.bodyMedium,
                        color = NatureColors.earthBrown,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // 2차 실패 시 해설 표시
        showCorrectAnswer && explanation.isNotEmpty() -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = NatureColors.leafGreen.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 정답 해설",
                        style = NatureTypography.titleSmall,
                        color = NatureColors.forestGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = explanation,
                        style = NatureTypography.bodyMedium,
                        color = NatureColors.earthBrown
                    )
                }
            }
        }
    }
}