package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.Coordinates
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Location
import com.example.bogoargo.domain.use_case.classroom.CreateClassUseCase
import com.example.bogoargo.domain.use_case.location.getLocationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject

data class ClassCreateUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val createdClass: Class? = null,
    val locations: List<Location> = emptyList(),
    val isLoadingLocations: Boolean = false
)

@HiltViewModel
class ClassCreateViewModel @Inject constructor(
    private val createClassUseCase: CreateClassUseCase,
    private val getLocationsUseCase: getLocationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassCreateUiState())
    val uiState: StateFlow<ClassCreateUiState> = _uiState

    init {
        loadLocations()
    }

    private fun loadLocations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocations = true)

//            val dummyLocations = listOf(
//                Location(locationId = 1, name = "강남 캠퍼스", Coordinates(latitude = BigDecimal("10.03"), longitude = BigDecimal("20.00"))),
//            )
//
//            _uiState.value = _uiState.value.copy(
//                locations = dummyLocations,
//                isLoadingLocations = false
//            )


            try {
                val locations = getLocationsUseCase()
                _uiState.value = _uiState.value.copy(
                    locations = locations,
                    isLoadingLocations = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingLocations = false,
                    errorMessage = "위치 정보를 불러올 수 없습니다: ${e.message}"
                )
            }

        }
    }

    fun createClass(
        className: String,
        description: String,
        location: String,
        activityDate: String,
        maxStudents: Int,
        grade: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = createClassUseCase(className, description, location, activityDate, maxStudents, grade)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        createdClass = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun clearState() {
        _uiState.value = ClassCreateUiState()
    }
}