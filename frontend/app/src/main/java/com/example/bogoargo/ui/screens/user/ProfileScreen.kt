package com.example.bogoargo.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.user.ProfileViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    /* TODO: 프로필 페이지
    val uiState by viewModel.uiState.collectAsState()
    var editedName by remember { mutableStateOf("") }
    var editedPassword by remember { mutableStateOf("") }
    
    LaunchedEffect(uiState) {
        editedName = uiState.name
        editedPassword = uiState.password
    }
    
    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "내 프로필",
                emoji = "👤",
                onNavigationClick = { navController.popBackStack() },
                actions = {
                    if (!uiState.isEditing) {
                        IconButton(
                            onClick = { viewModel.toggleEditMode() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "프로필 수정",
                                tint = NatureColors.earthBrown
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (uiState.isLoading) {
                    NatureComponents.NatureLoadingIndicator(
                        modifier = Modifier.height(200.dp)
                    )
                } else {
                    // 프로필 헤더
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            NatureComponents.ProfileAvatar(
                                emoji = "👤",
                                backgroundColor = NatureColors.sunnyYellow,
                                size = 80.dp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "📝 프로필 정보",
                                style = NatureTypography.titleMedium
                            )
                        }
                    }
                    
                    // 프로필 정보 카드
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (uiState.isEditing) {
                                Text(
                                    text = "✏️ 정보 수정하기",
                                    style = NatureTypography.titleMedium
                                )
                                
                                OutlinedTextField(
                                    value = editedUsername,
                                    onValueChange = { editedUsername = it },
                                    label = { 
                                        Text(
                                            "사용자명", 
                                            style = NatureTypography.bodyMedium
                                        ) 
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = NatureShapes.medium,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NatureColors.forestGreen,
                                        focusedLabelColor = NatureColors.forestGreen,
                                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                                    )
                                )
                                
                                OutlinedTextField(
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    label = { 
                                        Text(
                                            "이름", 
                                            style = NatureTypography.bodyMedium
                                        ) 
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = NatureShapes.medium,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NatureColors.forestGreen,
                                        focusedLabelColor = NatureColors.forestGreen,
                                        unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                                    )
                                )
                            } else {
                                Text(
                                    text = "👤 내 정보",
                                    style = NatureTypography.titleMedium
                                )
                                
                                // 이름
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "👤",
                                        style = NatureTypography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "이름",
                                            style = NatureTypography.labelMedium.copy(
                                                color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                            )
                                        )
                                        Text(
                                            text = uiState.name,
                                            style = NatureTypography.titleMedium
                                        )
                                    }
                                }
                                
                                // 사용자명
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🏷️",
                                        style = NatureTypography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "사용자명",
                                            style = NatureTypography.labelMedium.copy(
                                                color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                            )
                                        )
                                        Text(
                                            text = "@${uiState.name}",
                                            style = NatureTypography.bodyMedium
                                        )
                                    }
                                }
                                
                                // 역할
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⭐",
                                        style = NatureTypography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "역할",
                                            style = NatureTypography.labelMedium.copy(
                                                color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                            )
                                        )
                                        NatureComponents.StatusBadge(
                                            text = if(uiState.user?.role.toString().equals("ROLE_TEACHER")) {"선생"} else {"학생"},
                                            backgroundColor = NatureColors.leafGreen.copy(alpha = 0.2f),
                                            textColor = NatureColors.leafGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 액션 버튼들
                    if (uiState.isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NatureComponents.NatureButton(
                                onClick = { viewModel.toggleEditMode() },
                                text = "취소",
                                modifier = Modifier.weight(1f),
                                backgroundColor = NatureColors.earthBrown.copy(alpha = 0.3f)
                            )
                            NatureComponents.NatureButton(
                                onClick = { 
                                    viewModel.updateProfile(editedUsername, editedName) 
                                },
                                text = "💾 저장",
                                modifier = Modifier.weight(1f),
                                backgroundColor = NatureColors.forestGreen,
                                enabled = editedUsername.isNotEmpty() && editedName.isNotEmpty()
                            )
                        }
                    } else {
                        // 계정 관리 버튼
                        NatureComponents.NatureCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = NatureColors.softOrange.copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "⚙️ 계정 관리",
                                    style = NatureTypography.titleMedium
                                )
                                
                                NatureComponents.NatureButton(
                                    onClick = { 
                                        viewModel.withdraw()
                                    },
                                    text = "🚪 계정 탈퇴",
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = NatureColors.softOrange
                                )
                            }
                        }
                    }
                    
                    // 에러 메시지
                    uiState.error?.let { error ->
                        NatureComponents.NatureCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = NatureColors.softOrange.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "⚠️ $error",
                                modifier = Modifier.padding(16.dp),
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.earthBrown
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    */

}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen(navController = rememberNavController())
    }
}