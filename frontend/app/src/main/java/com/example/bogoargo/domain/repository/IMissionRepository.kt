package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Mission

interface IMissionRepository {
    suspend fun getMissions(): DataResult<List<Mission>>
    suspend fun getMissionById(missionId: Long): DataResult<Mission>
    suspend fun startMission(missionId: Long): DataResult<Unit>
    suspend fun completeMission(missionId: Long): DataResult<Unit>
    suspend fun getMissionProgress(missionId: Long): DataResult<Int>
}