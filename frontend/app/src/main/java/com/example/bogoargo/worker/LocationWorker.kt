package com.example.bogoargo.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bogoargo.data.storage.TokenStorage
import com.example.bogoargo.domain.repository.ILocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationRepository: ILocationRepository,
    private val tokenStorage: TokenStorage
) : CoroutineWorker(context, workerParams) {

    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)

    override suspend fun doWork(): Result {
        Log.d(TAG, "LocationWorker started")

        // 위치 권한 확인
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permission not granted")
            return Result.failure()
        }

        // 토큰 확인 (로그인 상태 확인)
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken == null) {
            Log.d(TAG, "User not logged in, skipping location update")
            return Result.success()
        }

        return try {
            // 현재 위치 획득 (최대 30초 대기)
            val location = withTimeoutOrNull(30000L) {
                getCurrentLocation()
            }

            if (location != null) {
                // 서버로 위치 전송
                val result = locationRepository.sendLocationToServer(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )

                if (result.isSuccess) {
                    Log.d(TAG, "Location sent successfully: ${location.latitude}, ${location.longitude}")
                    Result.success()
                } else {
                    Log.e(TAG, "Failed to send location: ${result.exceptionOrNull()?.message}")
                    Result.retry()
                }
            } else {
                Log.w(TAG, "Could not get current location")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in LocationWorker", e)
            Result.retry()
        }
    }

    private suspend fun getCurrentLocation(): Location? {
        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when getting location", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception when getting location", e)
            null
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || 
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "LocationWorker"
        const val WORK_NAME = "location_tracking_work"
    }
}