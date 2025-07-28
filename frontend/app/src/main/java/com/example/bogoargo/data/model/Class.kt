package com.example.bogoargo.data.model

import java.sql.Date

data class Class(
    val id: String = "",
    val year: Int,
    val school: String = "",
    val className: String = "",
    val description: String = "",
    val region: String = "",
    val invitationCode: String = "",
    val maxStudents: Int = 0,
    val currentStudents: Int = 0
)
