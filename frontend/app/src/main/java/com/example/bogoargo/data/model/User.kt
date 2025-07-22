package com.example.bogoargo.data.model

data class User(
    val id: String = "",
    val userName: String = "",
    val email: String = "",
    val bio: String = "",
    val profilePictureUrl: String? = null
)