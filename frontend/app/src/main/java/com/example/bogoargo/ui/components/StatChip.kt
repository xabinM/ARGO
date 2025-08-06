package com.example.bogoargo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = textSize,
                    color = color
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color.copy(alpha = 0.8f),
                    fontSize = textSize * 0.8f
                )
            )
        }
    }
}