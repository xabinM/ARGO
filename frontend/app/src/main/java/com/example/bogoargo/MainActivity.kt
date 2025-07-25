package com.example.bogoargo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.ui.screens.TeacherMainScreen
import com.example.bogoargo.ui.theme.BogoArgoTheme // 실제 프로젝트 테마 경로로 변경하세요

// 이전에 제공했던 MainTeacherScreen 함수를 TeacherMainScreen으로 이름을 변경했다고 가정합니다.
// TeacherMainScreen 함수는 아래와 같이 navController를 인자로 받아야 합니다.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 필요에 따라 유지하거나 제거하세요
        setContent {
            BogoArgoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 모든 화면 탐색은 AppNavigation 컴포저블 안에서 관리합니다.
                    AppNavigation()
                }
            }
            // TeacherMainScreen() // 여기서 직접 호출하지 않습니다. AppNavigation 안에서 NavHost에 의해 호출됩니다.
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController() // 앱의 최상위 NavController 생성

    NavHost(navController = navController, startDestination = "teacherMainScreen") { // 시작 화면을 "teacherMainScreen"으로 설정
        composable("teacherMainScreen") {
            // TeacherMainScreen 컴포저블에 navController를 전달합니다.
            TeacherMainScreen(navController = navController)
        }
        composable("ourClassManagement") {
            // "우리 반 관리" 화면 Composable
            // Text("우리 반 관리 화면입니다.") // 실제 화면 Composable로 대체
        }
        composable("experienceLearningManagement") {
            // "체험 학습 관리" 화면 Composable
            // Text("체험 학습 관리 화면입니다.") // 실제 화면 Composable로 대체
        }
        // 다른 화면들도 여기에 composable(...) 블록으로 추가합니다.
    }
}