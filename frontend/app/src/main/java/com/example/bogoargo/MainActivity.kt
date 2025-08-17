package com.example.bogoargo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
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
    
    // 알림 권한 요청 런처
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "알림 권한 허용됨")
        } else {
            Log.d("MainActivity", "알림 권한 거부됨")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 필요에 따라 유지하거나 제거하세요
        
        // 토큰 만료 이벤트 구독
        observeTokenExpiredEvents()
        
        // 앱 최초 실행시 알림 권한 요청
        requestNotificationPermissionIfNeeded()
        
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
    
    /**
     * Android 13+ 에서 알림 권한 요청
     */
    private fun requestNotificationPermissionIfNeeded() {
        // Android 13(API 33) 미만에서는 알림 권한이 자동으로 허용됨
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Log.d("MainActivity", "Android 12 이하 - 알림 권한 자동 허용")
            return
        }
        
        // 이미 권한이 있는지 확인
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            Log.d("MainActivity", "알림 권한 이미 허용됨")
            return
        }
        
        // 권한 요청
        Log.d("MainActivity", "알림 권한 요청")
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
