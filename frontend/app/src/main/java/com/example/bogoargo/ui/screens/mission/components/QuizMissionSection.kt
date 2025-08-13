package com.example.bogoargo.ui.screens.mission.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bogoargo.domain.model.QuizProblem
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureTypography

@Composable
fun QuizMissionSection(
    problem: QuizProblem,
    selectedAnswer: Int?,
    isAnswerSubmitted: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onRetryQuiz: () -> Unit,
    isLoading: Boolean,
    attemptCount: Int,
    showRetryButton: Boolean,
    canSubmitAnswer: Boolean,
    showCorrectAnswer: Boolean
) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header with title and attempt count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📚 퀴즈 미션",
                    style = NatureTypography.titleLarge,
                    color = NatureColors.forestGreen
                )
                
                if (attemptCount > 0) {
                    Text(
                        text = "${attemptCount}/2 시도",
                        style = NatureTypography.bodySmall,
                        color = NatureColors.earthBrown.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Question
            Text(
                text = problem.question,
                style = NatureTypography.bodyLarge,
                color = NatureColors.earthBrown
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Answer choices
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                problem.choices.forEachIndexed { index, choice ->
                    QuizAnswerCard(
                        choice = choice,
                        index = index,
                        isSelected = selectedAnswer == index,
                        isCorrectAnswer = index == problem.correctIndex,
                        isAnswerSubmitted = isAnswerSubmitted,
                        showCorrectAnswer = showCorrectAnswer,
                        canSubmitAnswer = canSubmitAnswer,
                        onAnswerSelected = onAnswerSelected
                    )
                }
            }
            
            // Feedback section
            if (isAnswerSubmitted) {
                Spacer(modifier = Modifier.height(16.dp))
                QuizFeedbackCard(
                    isFirstAttemptFailed = attemptCount == 1 && !showCorrectAnswer,
                    showCorrectAnswer = showCorrectAnswer,
                    explanation = problem.explanation
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            if (showRetryButton) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NatureComponents.NatureButton(
                        onClick = onRetryQuiz,
                        text = "💪 재도전",
                        modifier = Modifier.weight(1f),
                        backgroundColor = NatureColors.forestGreen
                    )
                }
            } else {
                val buttonText = when {
                    isLoading -> "처리 중..."
                    attemptCount == 0 -> "답안 확인"
                    else -> "답안 확인"
                }
                
                NatureComponents.NatureButton(
                    onClick = onSubmitAnswer,
                    text = buttonText,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswer != null && canSubmitAnswer && !isLoading,
                    backgroundColor = NatureColors.forestGreen
                )
            }
        }
    }
}