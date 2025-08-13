package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.Application
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.response.ClassDetailResponse
import com.example.bogoargo.data.response.ClassMemberResponse
import com.example.bogoargo.domain.model.StudentClassDetail

interface IClassRepository {
    suspend fun createClass(
        className: String,
        description: String,
        location: String,
        activityDate: String,
        maxStudents: Int,
        grade: Int
    ): DataResult<Class>
    suspend fun getClasses(): DataResult<List<Class>>
    suspend fun getClassById(classId: Long): DataResult<Class>
    suspend fun updateClass(classId: Long, className: String): DataResult<Class>
    suspend fun deleteClass(classId: Long): DataResult<Unit>
    suspend fun joinClass(inviteCode: String): DataResult<Unit>
    suspend fun leaveClass(classId: Long): DataResult<Unit>
    
    // Teacher specific methods
    suspend fun getTeacherClassList(page: Int = 1, size: Int = 10, status: String? = "active"): DataResult<List<Class>>
    suspend fun getStudentClassList(page: Int = 1, size: Int = 10, status: String? = "active"): DataResult<List<Class>>

    // Student class detail
    suspend fun getStudentClassDetail(classId: Long, include: String? = null): DataResult<StudentClassDetail>
    
    // Get complete class detail response (includes teams, students, etc.)
    suspend fun getCompleteClassDetail(classId: Long): DataResult<ClassDetailResponse>

    // Application related methods
    suspend fun getApplicationList(classId: Long): DataResult<List<Application>>
    suspend fun approveApplication(classId: Long, action:String, applicationIds: List<Long>): DataResult<MessageResponseDto>
    suspend fun getClassMemberList(classId: Long, status: String, page: Int = 10, size: Int = 10): DataResult<ClassMemberResponse>
}