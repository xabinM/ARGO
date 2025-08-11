package com.example.bogoargo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation

@Composable
fun BattleResultDialog(
    isVisible: Boolean,
    isSuccess: Boolean,
    message: String,
    isResponse: Boolean = false, // true = 대전 응답, false = 대전 신청
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null, // 실패 시 재시도 콜백
    onNavigateBack: () -> Unit // CardGameScreen으로 돌아가기
) {
    if (!isVisible) return
    
    val emoji = if (isSuccess) {
        if (isResponse) "🤝" else "⚔️"
    } else {
        "❌"
    }
    
    val title = if (isSuccess) {
        if (isResponse) "대전 응답 완료" else "대전 신청 완료"
    } else {
        if (isResponse) "대전 응답 실패" else "대전 신청 실패"
    }
    
    val description = if (isSuccess) {
        if (isResponse) "대전 응답이 성공적으로 전송되었습니다." else "대전 신청이 성공적으로 전송되었습니다."
    } else {
        "네트워크 오류나 서버 문제로 요청이 실패했습니다.\n다시 시도해 보시겠습니까?"
    }

    Dialog(onDismissRequest = onDismiss) {
        NatureComponents.NatureCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = NatureShapes.extraLarge,
            containerColor = NatureColors.whiteTransparent,
            elevation = NatureElevation.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 이모지
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = 48.sp
                )
                
                // 제목
                Text(
                    text = title,
                    style = NatureTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isSuccess) NatureColors.forestGreen else Color(0xFFF44336),
                    textAlign = TextAlign.Center
                )
                
                // 메시지
                Text(
                    text = message.ifEmpty { description },
                    style = NatureTypography.bodyLarge,
                    color = NatureColors.earthBrown,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 버튼들
                if (isSuccess) {
                    // 성공 시: 확인 버튼만
                    NatureComponents.NatureButton(
                        onClick = {
                            onDismiss()
                            onNavigateBack()
                        },
                        text = "확인",
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = NatureColors.forestGreen
                    )
                } else {
                    // 실패 시: 재시도 버튼과 돌아가기 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 돌아가기 버튼
                        NatureComponents.NatureOutlinedButton(
                            onClick = {
                                onDismiss()
                                onNavigateBack()
                            },
                            text = "돌아가기",
                            modifier = Modifier.weight(1f)
                        )
                        
                        // 재시도 버튼 (콜백이 있을 때만 표시)
                        if (onRetry != null) {
                            NatureComponents.NatureButton(
                                onClick = {
                                    onDismiss()
                                    onRetry()
                                },
                                text = "재시도",
                                modifier = Modifier.weight(1f),
                                backgroundColor = NatureColors.leafGreen
                            )
                        }
                    }
                }
            }
        }
    }
}