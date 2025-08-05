package com.example.bogoargo.ui.viewmodels

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.MissionSpot
import com.example.bogoargo.data.repository.MissionRepositoryImpl
import com.example.bogoargo.util.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val missionRepository: MissionRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    // 성능 최적화를 위한 캐시 변수들
    private var previousLocation: Location? = null
    private var lastCalculationTime = 0L
    private val calculationInterval = 2000L // 2초마다만 계산

    fun loadMissionSpots(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // 테스트용 임시 데이터
            val testSpots = listOf(
                MissionSpot(
                    spotId = 1,
                    spotName = "테스트 미션 지점",
                    latitude = 37.501365,
                    longitude = 127.039478
                ),
                MissionSpot(
                    spotId = 2,
                    spotName = "광화문",
                    latitude = 37.571,
                    longitude = 126.976
                ),
                MissionSpot(
                    spotId = 3,
                    spotName = "덕수궁",
                    latitude = 37.565,
                    longitude = 126.975
                ),
                MissionSpot(
                    spotId = 4,
                    spotName = "명동",
                    latitude = 37.563,
                    longitude = 126.982
                ),
                MissionSpot(
                    spotId = 5,
                    spotName = "남산타워",
                    latitude = 37.551,
                    longitude = 126.988
                ),
                MissionSpot(
                    spotId = 6,
                    spotName = "서울 시청",
                    latitude = 37.5664,
                    longitude = 126.9779
                )
            )
            
            // 실제 API 호출 대신 임시 데이터 사용
            kotlinx.coroutines.delay(1000) // 로딩 시뮬레이션
            _uiState.value = _uiState.value.copy(
                missionSpots = testSpots,
                isLoading = false
            )
            
            // 실제 API 호출 코드 (주석 처리)
            /*
            missionRepository.getMissionSpots(classId)
                .onSuccess { spots ->
                    _uiState.value = _uiState.value.copy(
                        missionSpots = spots,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            */
        }
    }

    fun updateUserLocation(location: Location) {
        _uiState.value = _uiState.value.copy(userLocation = location)
        checkNearbyMissions()
    }
    
    private fun checkNearbyMissions() {
        val currentState = _uiState.value
        val userLocation = currentState.userLocation ?: return
        
        // 성능 최적화 1: 이동 거리 기반 캐싱
        val currentTime = System.currentTimeMillis()
        previousLocation?.let { prevLoc ->
            val moveDistance = userLocation.distanceTo(prevLoc)
            val timeSinceLastCalc = currentTime - lastCalculationTime
            
            // 10m 미만 이동하고 2초 이내라면 계산 스킵
            if (moveDistance < 10f && timeSinceLastCalc < calculationInterval) {
                android.util.Log.d("MapViewModel", "Calculation skipped - moved ${String.format("%.1f", moveDistance)}m")
                return
            }
        }
        
        val userLat = userLocation.latitude
        val userLon = userLocation.longitude
        var calculatedCount = 0
        var skippedCount = 0
        
        val nearbySpots = currentState.missionSpots.filter { spot ->
            // 성능 최적화 2: 사전 필터링 (대략적 거리 체크)
            val latDiff = kotlin.math.abs(userLat - spot.latitude)
            val lonDiff = kotlin.math.abs(userLon - spot.longitude)
            
            // 위도/경도 0.0005도 차이 = 약 50m (사전 필터링)
            if (latDiff > 0.0005 || lonDiff > 0.0005) {
                skippedCount++
                android.util.Log.v("MapViewModel", 
                    "${spot.spotName} skipped - rough distance check (lat: ${String.format("%.6f", latDiff)}, lon: ${String.format("%.6f", lonDiff)})"
                )
                return@filter false
            }
            
            // 사전 필터링 통과한 지점만 정밀 계산
            calculatedCount++
            val distance = LocationUtils.calculateDistance(userLat, userLon, spot.latitude, spot.longitude)
            
            android.util.Log.d("MapViewModel", 
                "Distance to ${spot.spotName}: ${String.format("%.1f", distance)}m [CALCULATED]"
            )
            
            distance <= 50.0 // 50미터 범위
        }
        
        // 성능 로그 출력
        android.util.Log.i("MapViewModel", 
            "Performance: calculated=$calculatedCount, skipped=$skippedCount spots"
        )
        
        // 이전 상태와 비교해서 새로 근처에 들어온 미션이 있으면 로그 출력
        if (nearbySpots.size > currentState.nearbyMissionSpots.size) {
            val newSpots = nearbySpots - currentState.nearbyMissionSpots.toSet()
            newSpots.forEach { spot ->
                android.util.Log.i("MapViewModel", "미션 범위 진입: ${spot.spotName}")
            }
        }
        
        // 캐시 업데이트
        previousLocation = userLocation
        lastCalculationTime = currentTime
        
        _uiState.value = currentState.copy(nearbyMissionSpots = nearbySpots)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class MapUiState(
    val missionSpots: List<MissionSpot> = emptyList(),
    val nearbyMissionSpots: List<MissionSpot> = emptyList(),
    val userLocation: Location? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)