package com.example.bogoargo.ui.screens.mission.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
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
fun LoadingSection() {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = NatureColors.forestGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🎯 미션을 준비하고 있어요...",
                style = NatureTypography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}