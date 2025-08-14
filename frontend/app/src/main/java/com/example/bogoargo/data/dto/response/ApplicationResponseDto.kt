package com.example.bogoargo.data.dto.response

import com.example.bogoargo.data.response.ClassDataDto
import com.example.bogoargo.data.response.ClassInfo
import java.time.LocalDateTime

data class ApplicationDataDto(
    val applicationId: Long,
    val studentId: Long,
    val studentName: String,
    val status: String,
    val appliedAt: String,
    val processedAt: String?
)

data class ApplicationResponseDto(
    val success: Boolean,
    val message: String,
    val data: ApplicationListData?
)

data class ApplicationListData(
    val classInfo: ClassInfo,
    val applications: List<ApplicationDataDto>,
    val statistics: StatisticsDto,
    val pagination: PaginationDto
)

data class StatisticsDto(
    val totalApplications:Int,
    val pendingCount:Int,
    val approvedCount:Int,
    val rejectedCount:Int
)

data class PaginationDto (
    val totalCount:Int,
    val currentPage:Int,
    val totalPages:Int,
    val pageSize:Int,
    val hasNext:Boolean,
    val hasPrev:Boolean
)
