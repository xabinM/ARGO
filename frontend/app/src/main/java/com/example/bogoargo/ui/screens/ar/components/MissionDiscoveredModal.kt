package com.example.bogoargo.ui.screens.ar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography

@Composable
fun MissionDiscoveredModal(
    onNavigateToMission: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        NatureComponents.NatureCard(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 아이콘
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "미션 발견",
                    modifier = Modifier.size(64.dp),
                    tint = NatureColors.softOrange
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 제목
                Text(
                    text = "🎯 미션 발견!",
                    style = NatureTypography.headlineMedium,
                    color = NatureColors.forestGreen,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 설명
                Text(
                    text = "새로운 미션을 발견했습니다!\n미션을 확인하고 완료해보세요. 🌱",
                    style = NatureTypography.bodyMedium,
                    color = NatureColors.earthBrown,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 닫기 버튼
                    NatureComponents.NatureButton(
                        onClick = onDismiss,
                        text = "나중에",
                        modifier = Modifier.weight(1f),
                        backgroundColor = NatureColors.lightBeige
                    )
                    
                    // 미션 페이지로 이동 버튼
                    NatureComponents.NatureButton(
                        onClick = {
                            onNavigateToMission()
                            onDismiss()
                        },
                        text = "🌟 미션 보기",
                        modifier = Modifier.weight(1f),
                        backgroundColor = NatureColors.forestGreen
                    )
                }
            }
        }
    }
}