package com.example.bogoargo.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.user.SignUpViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("STUDENT") }
    var agreeTerms by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "회원가입",
                emoji = "🌱",
                onNavigationClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.errorMessage != null) {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NatureColors.softOrange.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "⚠️ ${uiState.errorMessage}",
                            modifier = Modifier.padding(16.dp),
                            style = NatureTypography.bodyMedium.copy(color = NatureColors.earthBrown)
                        )
                    }
                }
                
                if (uiState.signUpResponse != null) {
                    NatureComponents.NatureCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = NatureColors.leafGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "✅ ${uiState.signUpResponse!!.message}",
                            modifier = Modifier.padding(16.dp),
                            style = NatureTypography.bodyMedium.copy(color = NatureColors.forestGreen)
                        )
                    }
                }
                
                // 회원가입 폼 카드
                NatureComponents.NatureCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📝 회원 정보를 입력해주세요",
                            style = NatureTypography.titleMedium
                        )
                        
                        // 사용자명
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { 
                                Text(
                                    "사용자명", 
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
                            ),
                            supportingText = {
                                Text(
                                    "로그인에 사용할 아이디를 입력해주세요",
                                    style = NatureTypography.bodySmall.copy(
                                        color = NatureColors.earthBrown.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        )
                        
                        // 이름
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { 
                                Text(
                                    "이름", 
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
                        
                        // 비밀번호
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
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
                        
                        // 비밀번호 확인
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { 
                                Text(
                                    "비밀번호 확인", 
                                    style = NatureTypography.bodyMedium
                                ) 
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image = if (confirmPasswordVisible)
                                    Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff
                                
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = image,
                                        contentDescription = if (confirmPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                                        tint = NatureColors.earthBrown
                                    )
                                }
                            },
                            enabled = !uiState.isLoading,
                            shape = NatureShapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (password == confirmPassword) NatureColors.forestGreen else NatureColors.softOrange,
                                focusedLabelColor = if (password == confirmPassword) NatureColors.forestGreen else NatureColors.softOrange,
                                unfocusedBorderColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                            ),
                            isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                            supportingText = {
                                if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                                    Text(
                                        "비밀번호가 일치하지 않습니다",
                                        style = NatureTypography.bodySmall.copy(
                                            color = NatureColors.softOrange
                                        )
                                    )
                                }
                            }
                        )
                        
                        // 역할 선택
                        Text(
                            text = "👤 역할 선택",
                            style = NatureTypography.titleSmall
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NatureComponents.NatureButton(
                                onClick = { selectedRole = "STUDENT" },
                                text = "👶 학생",
                                modifier = Modifier.weight(1f),
                                backgroundColor = if (selectedRole == "STUDENT") NatureColors.forestGreen else NatureColors.earthBrown.copy(alpha = 0.3f),
                                enabled = !uiState.isLoading
                            )
                            NatureComponents.NatureButton(
                                onClick = { selectedRole = "TEACHER" },
                                text = "👩‍🏫 선생님",
                                modifier = Modifier.weight(1f),
                                backgroundColor = if (selectedRole == "TEACHER") NatureColors.forestGreen else NatureColors.earthBrown.copy(alpha = 0.3f),
                                enabled = !uiState.isLoading
                            )
                        }
                        
                        // 약관 동의
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = agreeTerms,
                                onCheckedChange = { agreeTerms = it },
                                enabled = !uiState.isLoading,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NatureColors.forestGreen,
                                    uncheckedColor = NatureColors.earthBrown.copy(alpha = 0.5f)
                                )
                            )
                            Text(
                                text = "서비스 이용약관에 동의합니다",
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.earthBrown
                                )
                            )
                        }
                    }
                }
                
                // 회원가입 버튼
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
                        onClick = {
                            viewModel.signUp(
                                username = username,
                                password = password,
                                name = name,
                                role = selectedRole,
                                agreeTerms = agreeTerms
                            )
                        },
                        text = "🌱 회원가입 완료",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = username.isNotEmpty() && 
                                 password.isNotEmpty() && 
                                 confirmPassword.isNotEmpty() &&
                                 name.isNotEmpty() &&
                                 password == confirmPassword &&
                                 agreeTerms,
                        backgroundColor = NatureColors.leafGreen
                    )
                }
                
                // 로그인 링크
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "이미 계정이 있으신가요?",
                        style = NatureTypography.bodyMedium.copy(
                            color = NatureColors.earthBrown.copy(alpha = 0.8f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = NatureColors.forestGreen
                        )
                    ) {
                        Text(
                            "로그인",
                            style = NatureTypography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme {
        SignUpScreen(navController = rememberNavController())
    }
}