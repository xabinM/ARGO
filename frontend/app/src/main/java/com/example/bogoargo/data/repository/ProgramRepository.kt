package com.example.bogoargo.data.repository

import com.example.bogoargo.data.model.Program
import com.example.bogoargo.data.model.ProgramStatus
import kotlinx.coroutines.delay

class ProgramRepository {
    
    suspend fun getProgramsByClassId(classId: String): List<Program> {
        delay(1000) // 서버 통신 시뮬레이션
        // TODO: 실제 서버 API 호출로 교체
        return emptyList()
    }
    
    suspend fun createProgram(program: Program): Program {
        delay(1000) // 서버 통신 시뮬레이션
        // TODO: 실제 서버 API 호출로 교체
        return program.copy(
            id = generateProgramId(),
            createdAt = getCurrentTimestamp(),
            updatedAt = getCurrentTimestamp()
        )
    }
    
    suspend fun updateProgram(program: Program): Program {
        delay(1000) // 서버 통신 시뮬레이션
        // TODO: 실제 서버 API 호출로 교체
        return program.copy(updatedAt = getCurrentTimestamp())
    }
    
    suspend fun deleteProgram(programId: String): Boolean {
        delay(1000) // 서버 통신 시뮬레이션
        // TODO: 실제 서버 API 호출로 교체
        return true
    }
    
    suspend fun getProgramById(programId: String): Program? {
        delay(1000) // 서버 통신 시뮬레이션
        // TODO: 실제 서버 API 호출로 교체
        return null
    }
    
    suspend fun updateProgramStatus(programId: String, status: ProgramStatus): Program? {
        delay(1000) // 서버 통신 시뮬레이션
        // TODO: 실제 서버 API 호출로 교체
        return null
    }
    
    suspend fun joinProgram(programId: String, userId: String): Boolean {
        delay(1000) // 서버 통신 시뮬레이션
        // TODO: 실제 서버 API 호출로 교체
        return true
    }
    
    suspend fun leaveProgram(programId: String, userId: String): Boolean {
        delay(1000) // 서버 통신 시뮬레이션
        // TODO: 실제 서버 API 호출로 교체
        return true
    }
    
    suspend fun getProgramParticipants(programId: String): List<String> {
        delay(1000) // 서버 통신 시뮬레이션
        // TODO: 실제 서버 API 호출로 교체
        return emptyList()
    }
    
    private fun generateProgramId(): String {
        return "program_${System.currentTimeMillis()}"
    }
    
    private fun getCurrentTimestamp(): String {
        return System.currentTimeMillis().toString()
    }
}