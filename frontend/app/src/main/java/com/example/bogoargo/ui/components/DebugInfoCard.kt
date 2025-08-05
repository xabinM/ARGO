package com.example.bogoargo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun DebugInfoCard(
    modifier: Modifier = Modifier,
    spotId: String,
    latitude: Double,
    longitude: Double,
    distanceToObject: Float?,
    geospatialError: String?,
    terrainAnchorError: Boolean,
    isFallbackMode: Boolean,
    hasCameraPermission: Boolean,
    hasFineLocationPermission: Boolean,
    hasCoarseLocationPermission: Boolean,
    isArSessionReady: Boolean,
    isEarthTracking: Boolean,
    currentLatitude: Double,
    currentLongitude: Double,
    currentAltitude: Double,
    currentAccuracy: Double
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🐛 DEBUG INFO",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            HorizontalDivider()
            
            // 미션 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("미션 ID:", fontWeight = FontWeight.Medium)
                Text(spotId, fontSize = 12.sp)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("목표 위치:", fontWeight = FontWeight.Medium)
                Text(
                    "${String.format(Locale.US, "%.6f", latitude)}, ${String.format(Locale.US, "%.6f", longitude)}",
                    fontSize = 10.sp
                )
            }
            
            // 거리 정보
            distanceToObject?.let { distance ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("객체 거리:", fontWeight = FontWeight.Medium)
                    Text(
                        "${String.format(Locale.US, "%.1f", distance)}m",
                        fontSize = 12.sp,
                        color = when {
                            distance <= 2.0f -> androidx.compose.ui.graphics.Color.Green
                            distance <= 5.0f -> androidx.compose.ui.graphics.Color.Blue
                            else -> androidx.compose.ui.graphics.Color.Red
                        }
                    )
                }
            }
            
            HorizontalDivider()
            
            // 권한 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("카메라 권한:", fontWeight = FontWeight.Medium)
                Text(
                    if (hasCameraPermission) "✅ 허용" else "❌ 거부",
                    fontSize = 12.sp,
                    color = if (hasCameraPermission) androidx.compose.ui.graphics.Color.Green 
                           else androidx.compose.ui.graphics.Color.Red
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("위치 권한:", fontWeight = FontWeight.Medium)
                Text(
                    when {
                        hasFineLocationPermission -> "✅ 정확한 위치"
                        hasCoarseLocationPermission -> "🟡 대략적 위치"
                        else -> "❌ 권한 없음"
                    },
                    fontSize = 12.sp,
                    color = when {
                        hasFineLocationPermission -> androidx.compose.ui.graphics.Color.Green
                        hasCoarseLocationPermission -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
                        else -> androidx.compose.ui.graphics.Color.Red
                    }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("AR 세션:", fontWeight = FontWeight.Medium)
                Text(
                    if (isArSessionReady) "✅ 준비됨" else "⏳ 로딩중",
                    fontSize = 12.sp,
                    color = if (isArSessionReady) androidx.compose.ui.graphics.Color.Green 
                           else androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
                )
            }
            
            // GPS 상태 정보
            if (isEarthTracking) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("현재 GPS:", fontWeight = FontWeight.Medium)
                    Text(
                        "${String.format(Locale.US, "%.6f", currentLatitude)}, ${String.format(Locale.US, "%.6f", currentLongitude)}",
                        fontSize = 10.sp
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("고도/정확도:", fontWeight = FontWeight.Medium)
                    Text(
                        "${String.format(Locale.US, "%.1fm", currentAltitude)} / ${String.format(Locale.US, "%.1fm", currentAccuracy)}",
                        fontSize = 10.sp
                    )
                }
            }
            
            HorizontalDivider()
            
            // 앵커 모드
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("앵커 모드:", fontWeight = FontWeight.Medium)
                Text(
                    if (isFallbackMode) "FALLBACK" else "GEOSPATIAL",
                    fontSize = 12.sp,
                    color = if (isFallbackMode) androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
                           else androidx.compose.ui.graphics.Color.Green
                )
            }
            
            // 지형 앵커 오류
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("지형 앵커:", fontWeight = FontWeight.Medium)
                Text(
                    if (terrainAnchorError) "❌ ERROR_UNSUPPORTED_LOCATION" else "✅ OK",
                    fontSize = 12.sp,
                    color = if (terrainAnchorError) androidx.compose.ui.graphics.Color.Red 
                           else androidx.compose.ui.graphics.Color.Green
                )
            }
            
            // Geospatial 오류
            geospatialError?.let { error ->
                HorizontalDivider()
                Text(
                    text = "⚠️ $error",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            HorizontalDivider()
            
            // 미션 상태
            Text(
                text = when {
                    distanceToObject == null -> "📡 AR 객체를 찾고 있습니다..."
                    distanceToObject!! <= 2.0f -> "🎯 터치하여 미션을 완료하세요!"
                    distanceToObject!! <= 5.0f -> "🚶‍♂️ 객체에 더 가까이 가세요"
                    distanceToObject!! <= 10.0f -> "👀 AR 객체를 찾아보세요"
                    else -> "🗺️ 목표 지점으로 이동하세요"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}