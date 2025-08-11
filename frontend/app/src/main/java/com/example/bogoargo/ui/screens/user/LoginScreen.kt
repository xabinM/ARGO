package com.example.bogoargo.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.data.preferences.UserPreferences
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.viewmodels.user.LoginViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    
    // NavigationEvent 처리
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { screen ->
            navController.navigate(screen.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
    
    NatureComponents.NatureBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            // 헤더 카드
            NatureComponents.NatureCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NatureComponents.ProfileAvatar(
                        emoji = "🌱",
                        backgroundColor = NatureColors.leafGreen,
                        size = 80.dp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "로그인",
                        style = NatureTypography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "게임과 함께하는 즐거운 학습",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.8f)
                        )
                    )
                }
            }
            
            // 로그인 폼 카드
            NatureComponents.NatureCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔑 계정 정보를 입력해주세요",
                        style = NatureTypography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = viewModel::updateUsername,
                        label = { 
                            Text(
                                text = "아이디",
                                style = NatureTypography.bodyMedium
                            ) 
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        enabled = !uiState.isLoading,
                        shape = NatureShapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NatureColors.forestGreen,
                            focusedLabelColor = NatureColors.forestGreen,
                            unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                        )
                    )
                    
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        label = { 
                            Text(
                                "비밀번호", 
                                style = NatureTypography.bodyMedium
                            ) 
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff
                            
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                                    tint = NatureColors.earthBrown
                                )
                            }
                        },
                        enabled = !uiState.isLoading,
                        shape = NatureShapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NatureColors.forestGreen,
                            focusedLabelColor = NatureColors.forestGreen,
                            unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = NatureColors.forestGreen
                            )
                        }
                    } else {
                        NatureComponents.NatureButton(
                            onClick = viewModel::login,
                            text = "🌱 로그인",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = uiState.username.isNotEmpty() && uiState.password.isNotEmpty(),
                            backgroundColor = NatureColors.forestGreen
                        )
                    }
                }
            }
            
            // 에러 메시지
            uiState.errorMessage?.let { errorMessage ->
                Spacer(modifier = Modifier.height(16.dp))
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = NatureColors.softOrange.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ $errorMessage",
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = viewModel::clearErrorMessage,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = NatureColors.earthBrown
                            )
                        ) {
                            Text("닫기")
                        }
                    }
                }
            }
            
            // 회원가입 링크
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "아직 계정이 없으신가요?",
                    style = NatureTypography.bodyMedium.copy(
                        color = NatureColors.earthBrown.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        navController.navigate("signUp")
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = NatureColors.forestGreen
                    )
                ) {
                    Text(
                        "회원가입",
                        style = NatureTypography.bodyMedium
                    )
                }
            }
            
            // 하단 여백
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(navController = rememberNavController())
    }
}