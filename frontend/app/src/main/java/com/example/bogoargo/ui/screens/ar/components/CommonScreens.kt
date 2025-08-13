package com.example.bogoargo.ui.screens.ar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography

@Composable
fun LoadingScreen(
    message: String,
    modifier: Modifier = Modifier
) {
    NatureComponents.NatureBackground {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            NatureComponents.NatureCard {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = NatureColors.forestGreen
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "🌿 $message",
                        color = NatureColors.earthBrown,
                        style = NatureTypography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    NatureComponents.NatureBackground {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            NatureComponents.NatureCard(
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ 오류가 발생했어요",
                        style = NatureTypography.headlineSmall,
                        color = NatureColors.earthBrown,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = NatureTypography.bodyMedium,
                        color = NatureColors.earthBrown.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    NatureComponents.NatureButton(
                        onClick = onRetry,
                        text = "🔄 다시 시도",
                        backgroundColor = NatureColors.forestGreen
                    )
                }
            }
        }
    }
}