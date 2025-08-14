package com.example.bogoargo.domain.model

import java.time.LocalDateTime

data class StudentLocationData(
    val classId: Long,
    val className: String,
    val students: List<StudentLocation>,
    val timestamp: LocalDateTime
)

data class StudentLocation(
    val userId: Long,
    val userName: String,
    val latitude: Double,
    val longitude: Double,
    val lastUpdated: LocalDateTime
) {
    fun getLocationString(): String {
        return "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
    }
    
    fun getDistanceFrom(otherLocation: StudentLocation): Double {
        return calculateDistance(latitude, longitude, otherLocation.latitude, otherLocation.longitude)
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
}

data class UserCoordinates(
    val userId: Long,
    val latitude: Double,
    val longitude: Double
)