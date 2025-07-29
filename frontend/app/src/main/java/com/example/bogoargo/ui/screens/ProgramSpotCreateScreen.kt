package com.example.bogoargo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramSpotCreateScreen(
    navController: NavController,
    classId: String = ""
) {
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf("") }
    var isMapReady by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 자연스러운 색상 정의 (초등학생 친화적)
    val warmBeige = androidx.compose.ui.graphics.Color(0xFFF5E6D3)
    val forestGreen = androidx.compose.ui.graphics.Color(0xFF7CB342)
    val sunnyYellow = androidx.compose.ui.graphics.Color(0xFFFFD54F)
    val earthBrown = androidx.compose.ui.graphics.Color(0xFF8D6E63)
    val leafGreen = androidx.compose.ui.graphics.Color(0xFF66BB6A)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "🗺️ 장소 선택하기", 
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
        },
        bottomBar = {
            if (selectedLocation != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "선택된 위치",
                                tint = leafGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "선택된 위치",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = earthBrown
                            )
                        }
                        
                        Text(
                            text = if (selectedAddress.isNotEmpty()) selectedAddress else 
                                "위도: ${String.format("%.6f", selectedLocation!!.latitude)}, 경도: ${String.format("%.6f", selectedLocation!!.longitude)}",
                            fontSize = 14.sp,
                            color = earthBrown.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Button(
                            onClick = {
                                // 선택된 위치 정보를 이전 화면으로 전달
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selectedLocation", selectedLocation
                                )
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selectedAddress", selectedAddress
                                )
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = leafGreen
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "확인",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "이 위치로 선택하기",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Google Map
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        onCreate(null)
                        onResume()
                        getMapAsync { googleMap ->
                            // 지도 초기 설정
                            googleMap.uiSettings.isZoomControlsEnabled = true
                            googleMap.uiSettings.isMyLocationButtonEnabled = true
                            
                            // 서울 중심으로 초기 위치 설정
                            val seoul = LatLng(37.5665, 126.9780)
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12f))
                            
                            // 지도 클릭 리스너
                            googleMap.setOnMapClickListener { latLng ->
                                selectedLocation = latLng
                                
                                // 기존 마커 제거하고 새 마커 추가
                                googleMap.clear()
                                googleMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title("선택된 위치")
                                )
                                
                                // 역지오코딩으로 주소 얻기 (실제 구현에서는 Geocoder 사용)
                                selectedAddress = "위도: ${String.format("%.6f", latLng.latitude)}, 경도: ${String.format("%.6f", latLng.longitude)}"
                            }
                            
                            isMapReady = true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // 안내 메시지
            if (isMapReady && selectedLocation == null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📍",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "지도를 터치해서\n장소를 선택해주세요!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = earthBrown,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "원하는 곳을 터치하면\n그 위치가 선택됩니다",
                            fontSize = 14.sp,
                            color = earthBrown.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // 로딩 인디케이터
            if (!isMapReady) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = forestGreen,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "지도를 불러오는 중...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = earthBrown
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgramSpotCreateScreenPreview() {
    MaterialTheme {
        ProgramSpotCreateScreen(navController = rememberNavController())
    }
}