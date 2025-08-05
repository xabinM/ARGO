package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val welcomeMessage: String = "Welcome to BogoArgo",
    val items: List<String> = emptyList(),
    val selectedItemIndex: Int? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simulate loading data
            kotlinx.coroutines.delay(1000)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                items = listOf(
                    "Item 1",
                    "Item 2", 
                    "Item 3",
                    "Item 4",
                    "Item 5"
                )
            )
        }
    }

    fun selectItem(index: Int) {
        _uiState.value = _uiState.value.copy(selectedItemIndex = index)
    }

    fun refreshData() {
        loadItems()
    }
}