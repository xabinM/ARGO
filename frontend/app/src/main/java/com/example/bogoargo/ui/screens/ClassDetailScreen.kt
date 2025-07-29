package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.data.model.Program
import com.example.bogoargo.data.model.ProgramStatus
import com.example.bogoargo.ui.viewmodels.ClassViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.theme.NatureElevation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    navController: NavController,
    classId: String = "class_1",
    schoolName: String = "싸피 초등학교",
    className: String = "1학년 1반",
    description: String = "우리 반은 체험 학습을 통해 다양한 경험을 쌓고 있습니다. 과학, 역사, 문화 등 다양한 분야의 프로그램에 참여하며 즐겁게 학습하고 있어요!",
    region: String = "서울",
    invitationCode: String = "ABC12DEF",
    viewModel: ClassViewModel = viewModel()
) {
    // ViewModel에서 프로그램 데이터를 가져옴
    val programs by viewModel.programs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // 페이지 진입 시 프로그램 목록 로드
    LaunchedEffect(classId) {
        viewModel.loadProgramsByClassId(classId)
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
            NatureComponents.NatureTopAppBar(
                title = "우리반 정보",
                emoji = "🏫"
            ) { 
                navController.popBackStack() 
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    navController.navigate("programCreate/$classId")
                },
                containerColor = NatureColors.leafGreen,
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = NatureShapes.medium
            ) {
                Icon(Icons.Default.Add, contentDescription = "프로그램 추가")
            }
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 헤더 카드 (학교 + 반 정보)
                item {
                    NatureComponents.NatureCard(
                        elevation = NatureElevation.extraLarge,
                        shape = NatureShapes.extraLarge
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            // 학교 + 반 제목
                            Text(
                                text = "$schoolName $className",
                                style = NatureTypography.titleLarge.copy(fontSize = 24.sp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // 지역 정보
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = "📍",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = region,
                                    style = NatureTypography.bodyMedium
                                )
                            }
                            
                            // 설명
                            Text(
                                text = description,
                                style = NatureTypography.bodyMedium.copy(
                                    color = NatureColors.earthBrown.copy(alpha = 0.8f),
                                    lineHeight = 22.sp
                                ),
                                modifier = Modifier.padding(bottom = 20.dp)
                            )
                            
                            // 초대 코드
                            NatureComponents.NatureCard(
                                shape = NatureShapes.medium,
                                containerColor = NatureColors.forestGreen.copy(alpha = 0.1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🎫 초대 코드",
                                        style = NatureTypography.labelMedium.copy(
                                            color = NatureColors.forestGreen
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = invitationCode,
                                        style = NatureTypography.titleMedium.copy(
                                            fontSize = 20.sp,
                                            letterSpacing = 2.sp,
                                            color = NatureColors.forestGreen
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 관리 버튼들
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 구성원 관리 버튼
                        NatureComponents.NatureCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            shape = NatureShapes.large,
                            containerColor = NatureColors.sunnyYellow.copy(alpha = 0.2f)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("classMemberManagement/$classId")
                                },
                                modifier = Modifier.fillMaxSize(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    contentColor = NatureColors.earthBrown
                                ),
                                shape = NatureShapes.large
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "👥",
                                        fontSize = 28.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "구성원 관리",
                                        style = NatureTypography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        // 팀 관리 버튼
                        NatureComponents.NatureCard(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            shape = NatureShapes.large,
                            containerColor = NatureColors.leafGreen.copy(alpha = 0.2f)
                        ) {
                            Button(
                                onClick = {
                                    navController.navigate("teamManagement/$classId")
                                },
                                modifier = Modifier.fillMaxSize(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    contentColor = NatureColors.earthBrown
                                ),
                                shape = NatureShapes.large
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🏆",
                                        fontSize = 28.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "팀 관리",
                                        style = NatureTypography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 프로그램 목록 섹션 헤더
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NatureComponents.SectionHeader(
                            text = "체험 프로그램",
                            emoji = "🎒"
                        )
                        NatureComponents.StatusBadge(
                            text = "${programs.size}개",
                            backgroundColor = NatureColors.forestGreen.copy(alpha = 0.15f),
                            textColor = NatureColors.forestGreen
                        )
                    }
                }
                
                // 로딩 또는 프로그램 목록 표시
                if (isLoading) {
                    item {
                        NatureComponents.NatureLoadingIndicator(
                            modifier = Modifier.height(200.dp)
                        )
                    }
                } else if (programs.isEmpty()) {
                    item {
                        NatureComponents.EmptyStateCard(
                            emoji = "🌱",
                            title = "아직 프로그램이 없어요",
                            description = "새로운 체험 프로그램을\n추가해서 시작해보세요!"
                        )
                    }
                } else {
                    // 프로그램 목록
                    items(programs) { program ->
                        ProgramCard(
                            program = program,
                            onClick = {
                                navController.navigate("programDetail/${program.id}")
                            }
                        )
                    }
                }
                
                // 빈 공간 추가 (FAB와의 겹침 방지)
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun ProgramCard(
    program: Program,
    onClick: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier.fillMaxWidth(),
        shape = NatureShapes.card
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = NatureColors.earthBrown
            ),
            shape = NatureShapes.card
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = program.title,
                            style = NatureTypography.bodyLarge.copy(fontSize = 18.sp),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = program.description,
                            style = NatureTypography.bodyMedium.copy(
                                color = NatureColors.earthBrown.copy(alpha = 0.7f)
                            ),
                            textAlign = TextAlign.Start
                        )
                    }
                    
                    // 상태 배지
                    StatusBadge(status = program.status)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 위치 정보
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📍",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = program.location,
                        style = NatureTypography.bodySmall.copy(
                            color = NatureColors.forestGreen
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 날짜 및 참가자 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NatureComponents.StatusBadge(
                        text = "📅 ${program.date}",
                        backgroundColor = NatureColors.sunnyYellow.copy(alpha = 0.2f),
                        textColor = NatureColors.earthBrown
                    )
                    NatureComponents.StatusBadge(
                        text = "👥 ${program.participants}/${program.maxParticipants}명",
                        backgroundColor = NatureColors.leafGreen.copy(alpha = 0.2f),
                        textColor = NatureColors.leafGreen
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ProgramStatus) {
    val (text, emoji, backgroundColor, textColor) = when (status) {
        ProgramStatus.UPCOMING -> Tuple4(
            "예정",
            "⏰",
            NatureColors.sunnyYellow.copy(alpha = 0.2f),
            NatureColors.earthBrown
        )
        ProgramStatus.ONGOING -> Tuple4(
            "진행중",
            "🚀",
            NatureColors.leafGreen.copy(alpha = 0.2f),
            NatureColors.leafGreen
        )
        ProgramStatus.COMPLETED -> Tuple4(
            "완료",
            "✅",
            NatureColors.forestGreen.copy(alpha = 0.2f),
            NatureColors.forestGreen
        )
    }
    
    NatureComponents.StatusBadge(
        text = "$emoji $text",
        backgroundColor = backgroundColor,
        textColor = textColor
    )
}

// Helper data class for multiple return values
data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Preview(showBackground = true)
@Composable
fun ClassDetailScreenPreview() {
    MaterialTheme {
        ClassDetailScreen(navController = rememberNavController())
    }
}