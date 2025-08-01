package com.example.bogoargo.ui.screens.ar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bogoargo.ui.screens.ar.model.ARDebugInfo
import com.example.bogoargo.ui.screens.ar.utils.formatDistance

@Composable
fun AROverlay(
    isSessionInitialized: Boolean,
    missionCompleted: Boolean,
    spotId: Long,
    onMissionComplete: () -> Unit,
    modifier: Modifier = Modifier,
    debugInfo: ARDebugInfo = ARDebugInfo()
) {
    Box(modifier = modifier) {
        // 상단 정보 카드
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isSessionInitialized) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AR 세션을 초기화하는 중...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else if (missionCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "완료",
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "미션 완료!",
                            color = Color.Green,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // GPS 상태에 따른 안내 메시지
                    when {
                        !debugInfo.locationServicesEnabled -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⚠️ 위치 서비스 필요",
                                    color = Color.Yellow,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "설정에서 위치 서비스를 활성화해주세요",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        !debugInfo.gpsEnabled -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "📍 GPS 필요",
                                    color = Color.Yellow,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "정확한 위치 측정을 위해 GPS를 활성화해주세요",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        debugInfo.earthTrackingState == "STOPPED" -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🌍 위치 정보 확인 중...",
                                    color = Color(0xFFFFA500),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "잠시 기다려주세요. 네트워크 연결을 확인해주세요.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        debugInfo.distanceToObject != Float.MAX_VALUE -> {
                            // 객체가 감지된 경우 거리 정보 표시
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (debugInfo.isObjectInteractable) {
                                    // 상호작용 가능한 거리 (2m 이내)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.TouchApp,
                                            contentDescription = "터치",
                                            tint = Color.Green,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "터치하여 미션 완료!",
                                            color = Color.Green,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "거리: ${formatDistance(debugInfo.distanceToObject)}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else {
                                    // 객체가 너무 멀어서 상호작용 불가능
                                    Text(
                                        text = "🎯 객체를 발견했습니다!",
                                        color = Color(0xFFFFA500),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "거리: ${formatDistance(debugInfo.distanceToObject)} (2m 이내로 접근하세요)",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = "🎯 미션 지점을 찾아보세요",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "카메라를 천천히 움직여 주변을 스캔하세요",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // 거리 정보 표시 (하단 중앙)
        if (isSessionInitialized && !missionCompleted && debugInfo.distanceToObject != Float.MAX_VALUE) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (debugInfo.isObjectInteractable) 
                        Color.Green.copy(alpha = 0.8f) 
                    else 
                        Color(0xFFFFA500).copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = if (debugInfo.isObjectInteractable) 
                        "🎯 터치하여 상호작용!" 
                    else 
                        "📍 ${formatDistance(debugInfo.distanceToObject)} - 더 가까이 접근하세요",
                    modifier = Modifier.padding(12.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}