package com.example.bogoargo.util

import android.location.Location
import kotlin.math.*

object LocationUtils {
    
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371000.0 // 지구 반지름 (미터)
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    fun isWithinRange(
        userLat: Double,
        userLon: Double,
        targetLat: Double,
        targetLon: Double,
        rangeInMeters: Double = 50.0
    ): Boolean {
        val distance = calculateDistance(userLat, userLon, targetLat, targetLon)
        return distance <= rangeInMeters
    }
    
    fun Location.distanceTo(lat: Double, lon: Double): Double {
        return calculateDistance(this.latitude, this.longitude, lat, lon)
    }
}