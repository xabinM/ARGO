package com.example.bogoargo.data.model

data class Settings(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "English"
)