package com.example.bogoargo.ui.viewmodels.cardgame

import androidx.lifecycle.ViewModel
import com.example.bogoargo.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CardGameViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    val currentUserId: StateFlow<Long?> = preferencesManager.currentUserId
    val currentUserName: StateFlow<String?> = preferencesManager.currentUserName
    val currentUserRole: StateFlow<String?> = preferencesManager.currentUserRole
    
    fun isTeamLeader(leaderId: Long): Boolean {
        val currentId = currentUserId.value
        return currentId != null && currentId == leaderId
    }
}