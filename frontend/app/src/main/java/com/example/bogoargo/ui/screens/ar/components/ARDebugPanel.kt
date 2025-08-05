package com.example.bogoargo.ui.screens.ar.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bogoargo.ui.screens.ar.model.ARDebugInfo

@Composable
fun DebugInfoPanel(
    debugInfo: ARDebugInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .heightIn(max = 400.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "🐛 AR Debug Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // GPS 정보
            DebugInfoItem("📍 Target GPS", "${String.format("%.6f", debugInfo.targetLatitude)}, ${String.format("%.6f", debugInfo.targetLongitude)}")
            DebugInfoItem("📍 Current GPS", "${String.format("%.6f", debugInfo.currentLatitude)}, ${String.format("%.6f", debugInfo.currentLongitude)}")
            DebugInfoItem("🎯 GPS Accuracy", "${String.format("%.1f", debugInfo.gpsAccuracy)}m")
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // AR 상태 정보
            DebugInfoItem("🌍 Earth Tracking", debugInfo.earthTrackingState)
            DebugInfoItem("⚓ Anchor Method", debugInfo.anchorMethod)
            DebugInfoItem("✅ Active Anchor", debugInfo.actualAnchorType)
            DebugInfoItem("🔗 Geospatial API", debugInfo.geospatialApiStatus)
            DebugInfoItem("📦 Model Loading", debugInfo.modelLoadingStatus)
            DebugInfoItem("🏔️ Terrain Anchor", debugInfo.terrainAnchorState)
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 세션 정보
            DebugInfoItem("🔧 Session Init", if (debugInfo.isSessionInitialized) "✅ Ready" else "⏳ Pending")
            DebugInfoItem("🎬 Frame Count", debugInfo.frameCount.toString())
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 시스템 상태 정보
            DebugInfoItem("📍 GPS Service", if (debugInfo.gpsEnabled) "✅ Enabled" else "❌ Disabled")
            DebugInfoItem("📍 Location Service", if (debugInfo.locationServicesEnabled) "✅ Enabled" else "❌ Disabled")
            DebugInfoItem("🌐 Network Location", if (debugInfo.networkConnected) "✅ Available" else "❌ Unavailable")
            DebugInfoItem("🛡️ Play Services", if (debugInfo.googlePlayServicesAvailable) "✅ Available" else "❌ Unavailable")
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 평면 감지 정보
            DebugInfoItem("🔍 Planes Detected", debugInfo.planesDetected.toString())
            DebugInfoItem("📐 Plane Anchor Used", if (debugInfo.planeAnchorUsed) "✅ Yes" else "❌ No")
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 애니메이션 정보
            DebugInfoItem("🎬 Animation Status", debugInfo.animationStatus)
            when (debugInfo.animationStatus) {
                "READY" -> DebugInfoItem("💡 Touch Tip", "Tap 3D object to open")
                "PLAYED" -> DebugInfoItem("📦 Box Status", "Opened ✅")
                else -> {}
            }
            
            if (debugInfo.availableModels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📁 Models:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                debugInfo.availableModels.forEach { model ->
                    Text(
                        text = "  • $model",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DebugInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
    }
}