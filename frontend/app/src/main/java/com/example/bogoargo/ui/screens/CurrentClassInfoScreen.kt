package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentClassInfoScreen(
    navController: NavController,
    schoolName: String = "",
    className: String = "",
    maxStudents: String = "",
    description: String = "",
    region: String = "",
    invitationCode: String = ""
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("반 생성 완료", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "완료",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "반 생성이 완료되었습니다!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "생성된 반 정보",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(text = "학교: $schoolName")
                    Text(text = "반 이름: $className")
                    Text(text = "최대 인원: ${maxStudents}명")
                    Text(text = "지역: $region")
                    if (description.isNotEmpty()) {
                        Text(text = "설명: $description")
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "초대 코드",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = invitationCode,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
            
            Button(
                onClick = {
                    navController.navigate("classManagement") {
                        popUpTo("createClass") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("반 관리로 이동")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrentClassInfoScreenPreview() {
    MaterialTheme {
        CurrentClassInfoScreen(
            navController = rememberNavController(),
            schoolName = "싸피 초등학교",
            className = "1학년 1반",
            maxStudents = "30",
            description = "즐겁게 배우는 우리 반",
            region = "서울",
            invitationCode = "ABC12DEF"
        )
    }
}