package com.example.bogoargo.ui.screens.mission.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureTypography

@Composable
fun ErrorSection(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️",
                style = NatureTypography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "미션 생성 실패",
                style = NatureTypography.titleMedium,
                color = NatureColors.earthBrown
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = errorMessage,
                style = NatureTypography.bodyMedium,
                color = NatureColors.earthBrown.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NatureComponents.NatureButton(
                    onClick = onRetry,
                    text = "다시 시도",
                    modifier = Modifier.weight(1f),
                    backgroundColor = NatureColors.forestGreen
                )
                NatureComponents.NatureButton(
                    onClick = onDismiss,
                    text = "확인",
                    modifier = Modifier.weight(1f),
                    backgroundColor = NatureColors.softOrange
                )
            }
        }
    }
}