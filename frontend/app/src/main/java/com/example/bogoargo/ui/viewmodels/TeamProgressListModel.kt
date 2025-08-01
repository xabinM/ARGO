package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import com.example.bogoargo.ui.screens.TeamProgress
import com.example.bogoargo.ui.theme.NatureColors

// API Response 데이터 클래스
data class TeamProgressResponse(
    val success: Boolean,
    val message: String,
    val data: List<TeamProgressData>?
)

data class TeamProgressData(
    val teamId: String,
    val teamName: String,
    val stage: String,
    val progress: Int,
    val colorIndex: Int // 색상 인덱스 (0-4)
)

// API 인터페이스
interface TeamProgressApiService {
    @GET("api/classes/{classId}/teams/progress")
    suspend fun getTeamProgress(@Path("classId") classId: String): Response<TeamProgressResponse>
}

class TeamProgressListModel : ViewModel() {
    
    // Retrofit 인스턴스 (임시 base URL)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val apiService = retrofit.create(TeamProgressApiService::class.java)
    
    // 팀 색상 팔레트
    private val teamColorPalette = listOf(
        NatureColors.forestGreen,
        NatureColors.sunnyYellow,
        NatureColors.leafGreen,
        NatureColors.softOrange,
        NatureColors.earthBrown
    )
    
    // StateFlow로 상태 관리
    private val _teamProgressList = MutableStateFlow<List<TeamProgress>>(emptyList())
    val teamProgressList: StateFlow<List<TeamProgress>> = _teamProgressList.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 팀별 진행도 데이터를 서버에서 로드
     */
    fun loadTeamProgress(classId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getTeamProgress(classId)
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.success == true && responseBody.data != null) {
                        // 서버 데이터를 UI 모델로 변환
                        _teamProgressList.value = mapToTeamProgressList(responseBody.data)
                    } else {
                        _error.value = responseBody?.message ?: "팀 진행도 데이터를 불러올 수 없습니다."
                    }
                } else {
                    _error.value = "서버 오류: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 서버 데이터를 UI 모델로 변환
     */
    private fun mapToTeamProgressList(dataList: List<TeamProgressData>): List<TeamProgress> {
        return dataList.map { data ->
            TeamProgress(
                teamId = data.teamId,
                teamName = data.teamName,
                stage = data.stage,
                progress = data.progress.coerceIn(0, 100), // 0-100 범위로 제한
                color = getColorByIndex(data.colorIndex)
            )
        }
    }
    
    /**
     * 색상 인덱스에 따른 색상 반환
     */
    private fun getColorByIndex(colorIndex: Int): androidx.compose.ui.graphics.Color {
        return teamColorPalette.getOrElse(colorIndex % teamColorPalette.size) { 
            NatureColors.forestGreen 
        }
    }
    
    /**
     * 에러 상태 초기화
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * 팀 진행도 새로고침
     */
    fun refreshTeamProgress(classId: String) {
        loadTeamProgress(classId)
    }
    
    /**
     * 특정 팀의 진행도 업데이트 (옵션)
     */
    fun updateTeamProgress(teamId: String, newProgress: Int) {
        val currentList = _teamProgressList.value.toMutableList()
        val index = currentList.indexOfFirst { it.teamId == teamId }
        
        if (index != -1) {
            currentList[index] = currentList[index].copy(
                progress = newProgress.coerceIn(0, 100)
            )
            _teamProgressList.value = currentList
        }
    }
    
    /**
     * 팀 진행도 통계 정보 반환
     */
    fun getProgressStatistics(): ProgressStatistics {
        val teams = _teamProgressList.value
        if (teams.isEmpty()) {
            return ProgressStatistics(0, 0.0, 0, 0)
        }
        
        val totalProgress = teams.sumOf { it.progress }
        val averageProgress = totalProgress.toDouble() / teams.size
        val completedTeams = teams.count { it.progress >= 100 }
        val totalTeams = teams.size
        
        return ProgressStatistics(
            totalTeams = totalTeams,
            averageProgress = averageProgress,
            completedTeams = completedTeams,
            inProgressTeams = totalTeams - completedTeams
        )
    }
}

// 진행도 통계 데이터 클래스
data class ProgressStatistics(
    val totalTeams: Int,
    val averageProgress: Double,
    val completedTeams: Int,
    val inProgressTeams: Int
)