package com.example.bogoargo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.bogoargo.navigation.AppNavigation
import com.example.bogoargo.ui.theme.BogoArgoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
