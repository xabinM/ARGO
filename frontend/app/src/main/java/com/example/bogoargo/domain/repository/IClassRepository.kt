package com.example.bogoargo.domain.repository

import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult

interface IClassRepository {
    suspend fun createClass(
        className: String,
        description: String,
        location: String,
        activityDate: String,
        maxStudents: Int
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
}