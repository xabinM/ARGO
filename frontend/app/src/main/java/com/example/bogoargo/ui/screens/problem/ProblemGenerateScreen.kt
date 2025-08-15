package com.example.bogoargo.ui.screens.problem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bogoargo.data.dto.response.ProblemDataQuizDto
import com.example.bogoargo.data.dto.response.ProblemData
import com.example.bogoargo.domain.model.Spot
import com.example.bogoargo.ui.theme.NatureColors
import com.example.bogoargo.ui.theme.NatureComponents
import com.example.bogoargo.ui.theme.NatureShapes
import com.example.bogoargo.ui.theme.NatureTypography
import com.example.bogoargo.ui.viewmodels.problem.ProblemGenerateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemGenerateScreen(
    classId: Long,
    navController: NavController,
    viewModel: ProblemGenerateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(classId) {
        viewModel.loadInitialData(classId)
    }

    NatureComponents.NatureBackground {
        Scaffold(
            topBar = {
                NatureComponents.NatureTopAppBar(
                    title = "문제 생성",
                    emoji = "🌟",
                    onNavigationClick = { navController.popBackStack() }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading -> {
                        LoadingIndicator()
                    }
                    uiState.showProblemCards && uiState.generatedProblems.isNotEmpty() -> {
                        ProblemCardView(
                            problems = uiState.generatedProblems,
                            currentIndex = uiState.currentProblemIndex,
                            registeredCount = uiState.registeredProblemsCount,
                            isRegistering = uiState.isRegisteringProblem,
                            onRegisterProblem = viewModel::registerCurrentProblem,
                            onSkipProblem = viewModel::skipCurrentProblem,
                            onComplete = {
                                viewModel.resetGeneration()
                                navController.popBackStack()
                            }
                        )
                    }
                    else -> {
                        ProblemSetupView(
                            spots = uiState.spots,
                            selectedSpot = uiState.selectedSpot,
                            problemCount = uiState.problemCount,
                            isGenerating = uiState.isGeneratingProblems,
                            onSpotSelected = viewModel::selectSpot,
                            onProblemCountChanged = viewModel::updateProblemCount,
                            onGenerateProblems = viewModel::generateProblems
                        )
                    }
                }

                // Error Snackbar
                uiState.errorMessage?.let { errorMessage ->
                    LaunchedEffect(errorMessage) {
                        kotlinx.coroutines.delay(3000)
                        viewModel.clearError()
                    }
                    
                    NatureComponents.NatureCard(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        containerColor = NatureColors.softOrange.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "⚠️ $errorMessage",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    NatureComponents.NatureLoadingIndicator()
}

@Composable
private fun ProblemSetupView(
    spots: List<Spot>,
    selectedSpot: Spot?,
    problemCount: Int,
    isGenerating: Boolean,
    onSpotSelected: (Spot) -> Unit,
    onProblemCountChanged: (Int) -> Unit,
    onGenerateProblems: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            "🗺️ 스팟을 선택하고 문제를 생성해보세요!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NatureColors.forestGreen,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Spots List
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "📍 스팟 선택",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NatureColors.forestGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(spots) { spot ->
                        SpotItem(
                            spot = spot,
                            isSelected = selectedSpot?.spotId == spot.spotId,
                            onSelected = { onSpotSelected(spot) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Problem Count Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🎯 문제 개수 선택 (1~5개)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NatureColors.forestGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(5) { index ->
                        val count = index + 1
                        ProblemCountButton(
                            count = count,
                            isSelected = problemCount == count,
                            onSelected = { onProblemCountChanged(count) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generate Button
        Button(
            onClick = onGenerateProblems,
            enabled = selectedSpot != null && !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NatureColors.forestGreen
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("생성 중...", fontSize = 16.sp)
            } else {
                Text("🎲 문제 생성하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SpotItem(
    spot: Spot,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NatureColors.leafGreen.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.1f)
        ),
        border = if (isSelected) BorderStroke(2.dp, NatureColors.forestGreen) else null
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "📍",
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                spot.spotName,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) NatureColors.forestGreen else Color.Black
            )
        }
    }
}

@Composable
private fun ProblemCardView(
    problems: List<ProblemDataQuizDto>,
    currentIndex: Int,
    registeredCount: Int,
    isRegistering: Boolean,
    onRegisterProblem: () -> Unit,
    onSkipProblem: () -> Unit,
    onComplete: () -> Unit
) {
    if (currentIndex >= problems.size) {
        CompletionView(
            totalProblems = problems.size,
            registeredCount = registeredCount,
            onComplete = onComplete
        )
        return
    }

    val currentProblem = problems[currentIndex]
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "문제 ${currentIndex + 1}/${problems.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NatureColors.forestGreen
            )
            Text(
                "등록됨: $registeredCount",
                fontSize = 14.sp,
                color = NatureColors.earthBrown
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Problem Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "❓ 문제",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NatureColors.forestGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    currentProblem.question,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    "📝 선택지",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NatureColors.forestGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                currentProblem.choices.forEachIndexed { index, choice ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            "${index + 1}. ",
                            fontSize = 16.sp,
                            fontWeight = if (index == currentProblem.correctIndex) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == currentProblem.correctIndex) NatureColors.forestGreen else Color.Black
                        )
                        Text(
                            choice,
                            fontSize = 16.sp,
                            fontWeight = if (index == currentProblem.correctIndex) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == currentProblem.correctIndex) NatureColors.forestGreen else Color.Black
                        )
                        if (index == currentProblem.correctIndex) {
                            Text(" ✅", fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "💡 해설",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NatureColors.forestGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    currentProblem.explanation,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Skip Button
            Button(
                onClick = onSkipProblem,
                enabled = !isRegistering,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("건너뛰기", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Register Button
            Button(
                onClick = onRegisterProblem,
                enabled = !isRegistering,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NatureColors.forestGreen
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("등록하기", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun CompletionView(
    totalProblems: Int,
    registeredCount: Int,
    onComplete: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎉 완료!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NatureColors.forestGreen
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "총 $totalProblems 개 문제 중",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    "$registeredCount 개 문제를 등록했습니다!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NatureColors.forestGreen,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NatureColors.forestGreen
                    )
                ) {
                    Text("완료", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProblemCountButton(
    count: Int,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) NatureColors.forestGreen else Color.Gray.copy(alpha = 0.2f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) NatureColors.forestGreen else Color.Gray,
                shape = CircleShape
            )
            .clickable { onSelected() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}
