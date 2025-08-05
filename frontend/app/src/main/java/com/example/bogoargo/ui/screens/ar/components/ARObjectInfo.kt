package com.example.bogoargo.ui.screens.ar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bogoargo.data.model.AR3DObject

/**
 * AR 화면에서 현재 표시 중인 객체의 정보를 보여주는 컴포넌트
 */
@Composable
fun ARObjectInfo(
    arObject: AR3DObject?,
    distance: Float,
    isInteractable: Boolean,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible && arObject != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        arObject?.let { obj ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 썸네일 이미지 또는 플레이스홀더
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (obj.thumbnailPath != null) {
                            AsyncImage(
                                model = "file:///android_asset/${obj.thumbnailPath}",
                                contentDescription = obj.displayName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // 기본 아이콘
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 객체 정보
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // 객체 이름
                        Text(
                            text = obj.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 카테고리 표시
                        obj.category?.let { category ->
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 거리 및 상호작용 가능 여부
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isInteractable) Color.Green else Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", distance)}m",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isInteractable) Color.Green else Color.White.copy(alpha = 0.7f),
                                fontWeight = if (isInteractable) FontWeight.Bold else FontWeight.Normal
                            )
                            
                            if (isInteractable) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "터치 가능!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Green,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                }
                
                // 설명이 있는 경우 추가 표시
                obj.description?.let { desc ->
                    Divider(
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

