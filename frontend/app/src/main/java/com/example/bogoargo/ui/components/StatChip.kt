package com.example.bogoargo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatChip(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
    textSize: androidx.compose.ui.unit.TextUnit = 12.sp
) {
    NatureComponents.NatureCard(
        modifier = modifier,
        containerColor = NatureColors.forestGreen.copy(alpha = 0.8f),
        shape = NatureShapes.medium,
        elevation = NatureElevation.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = NatureTypography.labelSmall.copy(
                    color = color.copy(alpha = 0.8f),
                    fontSize = textSize * 0.8f
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value.toString(),
                style = NatureTypography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = textSize,
                    color = color
                )
            )
        }
    }
}