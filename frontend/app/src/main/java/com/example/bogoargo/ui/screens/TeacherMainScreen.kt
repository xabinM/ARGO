package com.example.bogoargo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.viewmodels.TeacherMainViewModel
// R.drawable.profile_placeholder와 같은 리소스 ID를 사용하려면
// res/drawable 폴더에 이미지를 추가해야 합니다.
// 예시를 위해 임시로 안드로이드 아이콘을 사용합니다. 실제 앱에서는 자신의 이미지를 사용하세요.
//import android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMainScreen(
    navController: NavController,
    viewModel: TeacherMainViewModel = viewModel()
) {
    val user by viewModel.user
    val isLoading by viewModel.isLoading
    val uiState by viewModel.uiState
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("메인 페이지") } // 앱 바 타이틀, 필요 없으면 제거 가능
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold의 패딩 적용
                .padding(16.dp), // 전체 콘텐츠의 여백
            horizontalAlignment = Alignment.CenterHorizontally // 자식 요소들을 가운데 정렬
        ) {
            // 1. 상단 유저 정보 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp), // 아래쪽 여백 추가
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 사진 (원형)
                // import android.R // 이 줄을 제거합니다.

// ...

                Image(
                    painter = painterResource(id = com.example.bogoargo.R.drawable.my_temp_icon), // ✨ 이렇게 변경합니다.
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(16.dp)) // 프로필 사진과 텍스트 사이 여백

                // 사용자 이름 정보
                Column {
                    Text(
                        text = "교사",
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = user?.userName?.let { "$it 님" } ?: "로딩중...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // 2. 두 개의 버튼 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp), // 아래쪽 여백 추가
                horizontalArrangement = Arrangement.SpaceAround, // 버튼 간 균등한 공간 배분
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        navController.navigate("classManagement")
                    },
                    modifier = Modifier
                        .weight(1f) // 남은 공간을 균등하게 차지
                        .height(100.dp) // 버튼 높이 크게
                        .padding(horizontal = 8.dp), // 버튼 사이 여백
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)) // 예시 색상
                ) {
                    Text(text = "우리 반 관리", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        // "체험 학습 관리" 페이지로 이동하는 로직
                        navController.navigate("experienceLearningManagement")
                    },
                    modifier = Modifier
                        .weight(1f) // 남은 공간을 균등하게 차지
                        .height(100.dp) // 버튼 높이 크게
                        .padding(horizontal = 8.dp), // 버튼 사이 여백
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5)) // 예시 색상
                ) {
                    Text(text = "체험 학습 관리", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 3. 하단 상세 정보 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 남은 공간을 최대한 차지
                    .background(Color(0xFFF0F0F0)) // 배경색 추가
                    .padding(16.dp)
            ) {
                Text(
                    text = "현재 유저 상세 정보",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Divider(thickness = 1.dp, color = Color.Gray) // 구분선
                Spacer(modifier = Modifier.height(8.dp))
                when (uiState) {
                    is TeacherMainViewModel.UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is TeacherMainViewModel.UiState.Authenticated -> {
                        Text(text = "이메일: ${user?.email ?: "정보 없음"}", fontSize = 16.sp)
                        Text(text = "학번: ${user?.studentId?.takeIf { it.isNotEmpty() } ?: "정보 없음"}", fontSize = 16.sp)
                        Text(text = "연락처: ${user?.phoneNumber?.takeIf { it.isNotEmpty() } ?: "정보 없음"}", fontSize = 16.sp)
                    }
                    is TeacherMainViewModel.UiState.Error -> {
                        Text(
                            text = "오류: ${uiState.message}",
                            fontSize = 16.sp,
                            color = Color.Red
                        )
                    }
                    else -> {
                        Text(text = "사용자 정보를 불러올 수 없습니다.", fontSize = 16.sp)
                    }
                }
                // 여기에 더 많은 상세 정보를 추가할 수 있습니다.
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainTeacherScreen() {
    val navController = rememberNavController() // ✨ 여기에 rememberNavController()를 사용하여 인자를 전달합니다.
    TeacherMainScreen(navController = navController)
}