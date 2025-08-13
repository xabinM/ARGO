package com.example.bogoargo.ui.screens.ar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bogoargo.domain.model.AR3DObject
import com.example.bogoargo.ui.screens.ar.utils.isObjectInteractable
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureTypography

@Composable
fun AROverlay(
    isSessionInitialized: Boolean,
    missionCompleted: Boolean,
    spotId: Long,
    onMissionComplete: () -> Unit,
    modifier: Modifier = Modifier,
    arObject: AR3DObject? = null,
    objectDistance: Float = Float.MAX_VALUE
) {
    Box(modifier = modifier) {
        // 상단 정보 카드
        NatureComponents.NatureCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
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
                            color = NatureColors.forestGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🌿 AR 세션을 초기화하는 중...",
                            color = NatureColors.earthBrown,
                            style = NatureTypography.bodyMedium
                        )
                    }
                } else if (missionCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "완료",
                            tint = NatureColors.leafGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎉 미션 발견!",
                            color = NatureColors.forestGreen,
                            style = NatureTypography.titleMedium
                        )
                    }
                } else {
                    // AR 객체를 찾기 위한 안내 메시지
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (arObject != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TouchApp,
                                    contentDescription = "터치",
                                    tint = NatureColors.leafGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✨ 터치하여 미션 발견!",
                                    color = NatureColors.forestGreen,
                                    style = NatureTypography.titleMedium
                                )
                            }
                        } else {
                            Text(
                                text = "🎯 미션 지점을 찾아보세요",
                                color = NatureColors.earthBrown,
                                style = NatureTypography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "카메라를 천천히 움직여 주변을 탐험하세요",
                                color = NatureColors.earthBrown.copy(alpha = 0.8f),
                                style = NatureTypography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // AR 객체 정보 표시 (하단 위쪽)
        if (isSessionInitialized && !missionCompleted && arObject != null) {
            val isInteractable = isObjectInteractable(objectDistance)
            ARObjectInfo(
                arObject = arObject,
                distance = objectDistance,
                isInteractable = isInteractable,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            )
        }
        
        // 하단 안내 메시지
        if (isSessionInitialized && !missionCompleted) {
            NatureComponents.NatureCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = if (arObject != null) 
                        "🌟 3D 객체를 터치해보세요!" 
                    else 
                        "🔍 주변을 둘러보며 미션 객체를 찾아보세요",
                    modifier = Modifier.padding(12.dp),
                    color = NatureColors.earthBrown,
                    style = NatureTypography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}