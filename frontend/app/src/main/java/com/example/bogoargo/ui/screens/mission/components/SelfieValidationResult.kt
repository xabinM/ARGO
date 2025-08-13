package com.example.bogoargo.ui.screens.mission.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography

@Composable
fun SelfieValidationResult(
    validationResult: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (validationResult) 
                NatureColors.leafGreen.copy(alpha = 0.2f) 
            else 
                Color.Red.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (validationResult) "✅ 포즈 확인!" else "❌ 포즈가 일치하지 않아요",
                style = NatureTypography.bodyMedium,
                color = NatureColors.earthBrown
            )
        }
    }
}