package com.example.bogoargo.data.dto.response


data class StudentsLocationResponse(
    val classId: Long,
    val className: String,
    val students: List<StudentLocationInfo>,
    val timestamp: Long
)

data class StudentLocationInfo(
    val userId: Long,
    val userName: String,
    val latitude: Double,
    val longitude: Double,
    val lastUpdated: Long
)