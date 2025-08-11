package com.example.bogoargo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.bogoargo.navigation.AppNavigation
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.ui.theme.BogoArgoTheme
import com.example.bogoargo.data.event.TokenExpiredEvent
import com.example.bogoargo.data.storage.SecureStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var tokenExpiredEvent: TokenExpiredEvent
    
    @Inject 
    lateinit var secureStorage: SecureStorage
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 필요에 따라 유지하거나 제거하세요
        
        // 토큰 만료 이벤트 구독
        observeTokenExpiredEvents()
        
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
    
    private fun observeTokenExpiredEvents() {
        tokenExpiredEvent.tokenExpiredFlow
            .onEach {
                Log.d("MainActivity", "Token expired event received - clearing data and recreating activity")
                secureStorage.clearAll()
                // 액티비티 재생성하여 Splash 화면으로 돌아가기
                recreate()
            }
            .launchIn(lifecycleScope)
    }
}
