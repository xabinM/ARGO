package com.example.bogoargo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.data.model.User
import com.example.bogoargo.data.model.UserRole
import com.example.bogoargo.ui.viewmodels.ClassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassMemberManagementScreen(
    navController: NavController,
    classId: String = "",
    viewModel: ClassViewModel = viewModel()
) {
    // ViewModel에서 상태를 구독
    val members by viewModel.members.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // 자연스러운 색상 정의
    val warmBeige = Color(0xFFF5E6D3)
    val forestGreen = Color(0xFF7CB342)
    val sunnyYellow = Color(0xFFFFD54F)
    val earthBrown = Color(0xFF8D6E63)
    val leafGreen = Color(0xFF66BB6A)
    val softOrange = Color(0xFFFFB74D)
    
    // 페이지 로드 시 구성원 데이터 불러오기
    LaunchedEffect(classId) {
        if (classId.isNotEmpty()) {
            viewModel.loadMembersByClassId(classId)
        }
    }
    
    // 에러 처리
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            println("Error: $errorMessage")
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "🌱 우리반 친구들", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = forestGreen
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "뒤로가기",
                            tint = forestGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = warmBeige
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            warmBeige,
                            Color(0xFFF8F3E8)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = forestGreen,
                            strokeWidth = 5.dp,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                } else if (members.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            ),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🌿",
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "아직 친구들이 없어요",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = earthBrown
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "곧 재미있는 친구들이\n여기에 나타날 거예요!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = earthBrown.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // 구성원 통계 카드
                    MemberStatsCard(
                        totalMembers = members.size,
                        studentsCount = members.count { it.role == UserRole.STUDENT },
                        teamsCount = members.mapNotNull { it.teamId }.distinct().size,
                        forestGreen = forestGreen,
                        sunnyYellow = sunnyYellow,
                        leafGreen = leafGreen
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 구성원 리스트 헤더
                    Text(
                        text = "🎒 친구 목록",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = earthBrown,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // 구성원 리스트
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(members) { member ->
                            MemberCard(
                                member = member,
                                forestGreen = forestGreen,
                                sunnyYellow = sunnyYellow,
                                leafGreen = leafGreen,
                                earthBrown = earthBrown,
                                softOrange = softOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberStatsCard(
    totalMembers: Int,
    studentsCount: Int,
    teamsCount: Int,
    forestGreen: Color,
    sunnyYellow: Color,
    leafGreen: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "🌟 우리반 현황",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = forestGreen,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    "전체", 
                    totalMembers.toString(), 
                    "👥",
                    forestGreen
                )
                StatItem(
                    "학생", 
                    studentsCount.toString(), 
                    "🎒",
                    sunnyYellow
                )
                StatItem(
                    "팀", 
                    teamsCount.toString(), 
                    "🏆",
                    leafGreen
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String, 
    value: String, 
    emoji: String,
    color: Color
) {
    Card(
        modifier = Modifier.size(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MemberCard(
    member: User,
    forestGreen: Color,
    sunnyYellow: Color,
    leafGreen: Color,
    earthBrown: Color,
    softOrange: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 아이콘
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (member.role == UserRole.TEACHER) 
                            earthBrown 
                        else 
                            when ((member.userName.length + member.studentId.length) % 4) {
                                0 -> forestGreen
                                1 -> sunnyYellow
                                2 -> leafGreen
                                else -> softOrange
                            }
                    ),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (member.role == UserRole.TEACHER) "👩‍🏫" else "👦",
                            fontSize = 24.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // 사용자 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 이름 (강조)
                Text(
                    text = member.userName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = earthBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // 학번/교번
                if (member.studentId.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = forestGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "🎓 ${member.studentId}",
                            fontSize = 14.sp,
                            color = forestGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // 역할
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (member.role) {
                            UserRole.TEACHER -> earthBrown.copy(alpha = 0.15f)
                            UserRole.STUDENT -> sunnyYellow.copy(alpha = 0.2f)
                        }
                    )
                ) {
                    Text(
                        text = when (member.role) {
                            UserRole.TEACHER -> "📚 선생님"
                            UserRole.STUDENT -> "✏️ 학생"
                        },
                        fontSize = 12.sp,
                        color = when (member.role) {
                            UserRole.TEACHER -> earthBrown
                            UserRole.STUDENT -> sunnyYellow.copy(alpha = 0.8f)
                        },
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            // 소속 팀 (강조)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (member.teamId != null) 
                            leafGreen.copy(alpha = 0.2f)
                        else 
                            Color.Gray.copy(alpha = 0.15f)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (member.teamId != null) "🏆" else "⏳",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (member.teamId != null) "팀 소속" else "대기중",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (member.teamId != null) 
                                leafGreen
                            else 
                                Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        
                        if (member.teamId != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = leafGreen.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "ID: ${member.teamId}",
                                    fontSize = 10.sp,
                                    color = leafGreen,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewClassMemberManagementScreen() {
    MaterialTheme {
        ClassMemberManagementScreen(
            navController = rememberNavController(),
            classId = "class_1"
        )
    }
}