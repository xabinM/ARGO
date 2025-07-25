package com.example.bogoargo.ui.screens // 실제 패키지 경로에 맞게 변경하세요

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


// 데이터 클래스: 반 정보를 정의합니다.
data class ClassInfo(
    val id: Int,
    val year: Int,
    val school: String,
    val className: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassManagementScreen(navController: NavController) {
    // 더미 데이터 (실제 앱에서는 ViewModel 등에서 데이터를 가져올 것입니다.)
    val classList = remember {
        mutableStateListOf(
            ClassInfo(1, 2023, "싸피 초등학교", "1학년 1반"),
            ClassInfo(2, 2024, "싸피 중학교", "2학년 3반"),
            ClassInfo(3, 2023, "싸피 고등학교", "3학년 5반"),
            ClassInfo(4, 2025, "싸피 초등학교", "4학년 2반"),
            ClassInfo(5, 2024, "싸피 중학교", "1학년 1반"),
            ClassInfo(6, 2025, "싸피 고등학교", "2학년 4반"),
            ClassInfo(7, 2023, "행복 초등학교", "5학년 3반")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("우리반 관리", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = @androidx.compose.runtime.Composable { // ✨ bottomBar 슬롯에 커스텀 버튼을 넣습니다.
            BottomAppBar( // BottomAppBar를 사용하여 버튼 영역을 구성
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh // 바닥 색상 (테마에 맞게 조정)
            ) {
                Button(
                    onClick = {
                        navController.navigate("createGroupScreen") // "반 생성 페이지"로 이동
                    },
                    modifier = Modifier
                        .fillMaxWidth() // 바닥 바의 전체 너비를 차지
                        .height(56.dp) // 버튼의 높이 지정
                        .padding(horizontal = 16.dp), // 좌우 패딩으로 버튼이 화면 끝에 붙지 않도록
                    shape = RoundedCornerShape(8.dp), // 버튼 모서리 둥글게
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start, // ✨ 왼쪽 정렬
                        modifier = Modifier.fillMaxWidth() // Row가 버튼의 전체 너비를 차지하도록
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "새로운 그룹 생성",
                            modifier = Modifier.size(24.dp) // 아이콘 크기
                        )
                        Spacer(Modifier.width(8.dp)) // 아이콘과 텍스트 사이 간격
                        Text(
                            text = "새로운 반 추가", // ✨ 설명 텍스트
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold의 패딩 적용
                .padding(horizontal = 16.dp) // 좌우 여백
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                                   //.fillMaxHeight(0.85f),
                contentPadding = PaddingValues(vertical = 20.dp), // 리스트 상하 여백
                verticalArrangement = Arrangement.spacedBy(12.dp) // 아이템 간 간격
            ) {
                items(classList) { classInfo ->
                    ClassInfoCard(classInfo = classInfo) {
                        // 각 반 카드를 클릭했을 때 해당 반의 상세 페이지로 이동
                        navController.navigate("classDetail/${classInfo.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun ClassInfoCard(classInfo: ClassInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // 한 화면에 5개 정도 보이도록 높이 조절 (대략적인 값)
            .clickable(onClick = onClick), // 클릭 가능하게 설정
        shape = RoundedCornerShape(12.dp), // 둥근 모서리 사각형 프레임
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // 그림자 효과
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // 카드 배경색
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // 카드 내부 여백
            verticalArrangement = Arrangement.Center // 내용 중앙 정렬
        ) {
            // 생성년도 (상단에 작게)
            Text(
                text = "${classInfo.year}년 생성",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End) // 오른쪽 정렬
            )
            Spacer(modifier = Modifier.height(4.dp)) // 년도와 학교/반 사이 간격

            // 학교 + 반 정보
            Text(
                text = "${classInfo.school} ${classInfo.className}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // 여기에 추가적인 반 정보를 표시할 수 있습니다.
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewClassManagementScreen() {
    val navController = rememberNavController()
    // preview에서 remember mutableStateListOf를 사용하려면 remember를 import해야 합니다.
    // preview에서 mutableStateListOf를 import 해야 합니다.
    ClassManagementScreen(navController = navController)
}