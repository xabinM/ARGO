package com.example.bogoargo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramManagementScreen(navController: NavController) {
    // Mock data for programs
    val programs = remember {
        listOf(
            Program("1", "🌳 숲 탐험 프로그램", "자연 속에서 신나는 모험", "2024-01-15", true),
            Program("2", "🌊 강가 체험 학습", "바다와 강에서 배우는 자연 과학", "2024-02-20", false),
            Program("3", "🌻 밑밭 농장 경험", "직접 기르고 수확하는 즐거움", "2024-03-10", true),
            Program("4", "🦋 동물 박물관 견학", "생명의 신비를 알아가요", "2024-04-05", false)
        )
    }
    
    Scaffold(
        topBar = {
            NatureComponents.NatureTopAppBar(
                title = "체험 학습 관리",
                emoji = "🌿",
                onNavigationClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    navController.navigate("programCreate")
                },
                containerColor = NatureColors.leafGreen,
                shape = NatureShapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "프로그램 추가", tint = androidx.compose.ui.graphics.Color.White)
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
                    title = "프로그램 현황",
                    emoji = "🌟"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NatureComponents.StatItem(
                            label = "전체",
                            value = programs.size.toString(),
                            emoji = "🌿",
                            color = NatureColors.forestGreen
                        )
                        NatureComponents.StatItem(
                            label = "활성",
                            value = programs.count { it.isActive }.toString(),
                            emoji = "🌱",
                            color = NatureColors.leafGreen
                        )
                        NatureComponents.StatItem(
                            label = "비활성",
                            value = programs.count { !it.isActive }.toString(),
                            emoji = "🍂",
                            color = NatureColors.earthBrown
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                NatureComponents.SectionHeader(
                    text = "프로그램 목록",
                    emoji = "🌿"
                )
                
                if (programs.isEmpty()) {
                    NatureComponents.EmptyStateCard(
                        emoji = "🌿",
                        title = "등록된 프로그램이 없어요",
                        description = "새로운 체험 학습 프로그램을\n만들어 아이들과 함께 자연을 탐험해보세요!"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                    ) {
                        items(programs) { program ->
                            ProgramCard(
                                program = program,
                                onClick = {
                                    navController.navigate("programDetail/${program.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data class for Program
data class Program(
    val id: String,
    val name: String,
    val description: String,
    val createdAt: String,
    val isActive: Boolean
)

@Composable
fun ProgramCard(
    program: Program,
    onClick: () -> Unit
) {
    NatureComponents.NatureCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = NatureShapes.card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Program Icon
            NatureComponents.ProfileAvatar(
                emoji = "🌿",
                backgroundColor = if (program.isActive) NatureColors.leafGreen else NatureColors.earthBrown,
                size = 60.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Program Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = program.name,
                    style = NatureTypography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = program.description,
                    style = NatureTypography.bodySmall.copy(
                        color = NatureColors.earthBrown.copy(alpha = 0.8f)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NatureComponents.StatusBadge(
                        text = "📅 ${program.createdAt}",
                        backgroundColor = NatureColors.sunnyYellow.copy(alpha = 0.2f),
                        textColor = NatureColors.earthBrown
                    )
                    
                    NatureComponents.StatusBadge(
                        text = if (program.isActive) "🟢 활성" else "🔴 비활성",
                        backgroundColor = if (program.isActive) 
                            NatureColors.leafGreen.copy(alpha = 0.2f)
                        else 
                            NatureColors.earthBrown.copy(alpha = 0.2f),
                        textColor = if (program.isActive) 
                            NatureColors.leafGreen
                        else 
                            NatureColors.earthBrown
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgramManagementScreenPreview() {
    MaterialTheme {
        ProgramManagementScreen(navController = rememberNavController())
    }
}