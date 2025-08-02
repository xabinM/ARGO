package com.example.bogoargo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.data.model.Class
import com.example.bogoargo.ui.viewmodels.ClassViewModel
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassManagementScreen(
    navController: NavController,
    viewModel: ClassViewModel = viewModel()
) {
    // ViewModel에서 상태를 구독
    val classes by viewModel.classes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // 에러 처리
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // 실제 앱에서는 Snackbar나 Toast로 에러 표시
            // 지금은 콘솔에 로그만 출력
            println("Error: $errorMessage")
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "우리반 관리",
                emoji = "🏠",
                onNavigationClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("classCreate")
                },
                containerColor = NatureColors.leafGreen,
                shape = NatureShapes.large
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "반 추가",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    ) { paddingValues ->
        NatureComponents.NatureBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
            ) {
                // Statistics Card
                NatureComponents.StatsCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "반 현황",
                    emoji = "🌟"
                ) {
                    NatureComponents.StatItem(
                        label = "전체 반",
                        value = classes.size.toString(),
                        emoji = "🏠",
                        color = NatureColors.forestGreen
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                NatureComponents.SectionHeader(
                    text = "내 반 목록",
                    emoji = "🏠"
                )

                if (isLoading) {
                    NatureComponents.NatureLoadingIndicator()
                } else if (classes.isEmpty()) {
                    NatureComponents.EmptyStateCard(
                        emoji = "🏠",
                        title = "등록된 반이 없어요",
                        description = "새로운 반을 만들어\n아이들과 함께 재미있는 학습을 시작해보세요!"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(classes) { classInfo ->
                            ClassInfoCard(classInfo = classInfo) {
                                navController.navigate(
                                    "classDetail/${classInfo.id}/${classInfo.schoolName}/${classInfo.className}/${classInfo.description}/${classInfo.region}/${classInfo.invitationCode}"
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
    }
}

@Composable
fun ClassInfoCard(classInfo: Class, onClick: () -> Unit) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = NatureShapes.card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Class Icon
            NatureComponents.ProfileAvatar(
                emoji = "🏠",
                backgroundColor = NatureColors.sunnyYellow,
                size = 60.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Class Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🏠 ${classInfo.schoolName}",
                    style = NatureTypography.bodyMedium.copy(
                        color = NatureColors.earthBrown.copy(alpha = 0.8f)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = classInfo.className,
                    style = NatureTypography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NatureComponents.StatusBadge(
                        text = "📅 ${classInfo.year}년",
                        backgroundColor = NatureColors.forestGreen.copy(alpha = 0.2f),
                        textColor = NatureColors.forestGreen
                    )

                    NatureComponents.StatusBadge(
                        text = "🗺️ ${classInfo.region}",
                        backgroundColor = NatureColors.leafGreen.copy(alpha = 0.2f),
                        textColor = NatureColors.leafGreen
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewClassManagementScreen() {
    val navController = rememberNavController()
    ClassManagementScreen(navController = navController)
}