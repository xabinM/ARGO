package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class TeacherMainViewModel : ViewModel() {

    private val _userName = mutableStateOf("홍길동")
    val userName: State<String> = _userName

    private val _email = mutableStateOf("hong@example.com")
    val email: State<String> = _email

}
