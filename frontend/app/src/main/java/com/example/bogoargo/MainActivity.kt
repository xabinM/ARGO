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
import com.example.bogoargo.navigation.AppNavigation
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
        }
    }
}
