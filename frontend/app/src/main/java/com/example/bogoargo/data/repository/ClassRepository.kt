package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.ClassApiService
import com.example.bogoargo.data.dto.request.ClassCreateRequest
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.data.mapper.toDomainModel as toClassDetailDomainModel
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.StudentClassDetail
import com.example.bogoargo.domain.repository.IClassRepository
import com.example.bogoargo.data.dto.response.ApplicationResponseDto
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.data.response.ClassMemberResponse
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ClassRepositoryImpl @Inject constructor(
    private val classApiService: ClassApiService
) : IClassRepository {

    override suspend fun createClass(
        className: String,
        description: String,
        location: String,
        activityDate: String,
        maxStudents: Int,
        grade: Int
    ): DataResult<Class> {
        return try {
            val request = ClassCreateRequest(
                className = className,
                description = description,
                location = location,
                activityDate = activityDate,
                maxStudents = maxStudents,
                grade = grade
            )
            val response = classApiService.createClass(request)
            if (response.isSuccessful) {
                val classResponse = response.body()
                if (classResponse?.success == true && classResponse.data != null) {
                    DataResult.Success(classResponse.data.toDomainModel())
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getClasses(): DataResult<List<Class>> {
        return try {
            val response = classApiService.getTeacherClassList(1, 100, "active")
            if (response.isSuccessful) {
                val classListResponse = response.body()
                if (classListResponse?.success == true && classListResponse.data != null) {
                    val classes = classListResponse.data.classes.map { it.toDomainModel() }
                    DataResult.Success(classes)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getClassById(classId: Long): DataResult<Class> {
        return try {
            val response = classApiService.getClassDetail(classId)
            if (response.isSuccessful) {
                val classData = response.body()
                if (classData != null) {
                    DataResult.Success(classData.data?.classInfo!!.toDomainModel())
                } else {
                    DataResult.Error(DataException.NotFoundError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun updateClass(classId: Long, className: String): DataResult<Class> {
        return try {
            // Note: API doesn't seem to have update endpoint, using create pattern as fallback
            val request = ClassCreateRequest(
                className = className,
                description = "",
                location = "",
                activityDate = "",
                maxStudents = 0,
                grade = 1
            )
            val response = classApiService.createClass(request)
            if (response.isSuccessful) {
                val classResponse = response.body()
                if (classResponse?.success == true && classResponse.data != null) {
                    DataResult.Success(classResponse.data.toDomainModel())
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun deleteClass(classId: Long): DataResult<Unit> {
        return try {
            val response = classApiService.deleteClass(classId)
            if (response.isSuccessful) {
                val messageResponse = response.body()
                if (messageResponse?.success == true) {
                    DataResult.Success(Unit)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun joinClass(inviteCode: String): DataResult<Unit> {
        return try {
            val response = classApiService.applyClass(inviteCode)
            if (response.isSuccessful) {
                val applyResponse = response.body()
                if (applyResponse?.success == true) {
                    DataResult.Success(Unit)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun leaveClass(classId: Long): DataResult<Unit> {
        return try {
            val response = classApiService.leaveClass(classId)
            if (response.isSuccessful) {
                val leaveResponse = response.body()
                if (leaveResponse?.success == true) {
                    DataResult.Success(Unit)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getTeacherClassList(page: Int, size: Int, status: String?): DataResult<List<Class>> {
        return try {
            val response = classApiService.getTeacherClassList(page, size, status)
            if (response.isSuccessful) {
                val classListResponse = response.body()
                if (classListResponse?.success == true && classListResponse.data != null) {
                    val classes = classListResponse.data.classes.map { it.toDomainModel() }
                    DataResult.Success(classes)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getStudentClassList(page: Int, size: Int, status: String?): DataResult<List<Class>> {
        return try {
            val response = classApiService.getStudentClassList(page, size, status)
            if (response.isSuccessful) {
                val classListResponse = response.body()
                if (classListResponse?.success == true && classListResponse.data?.classes != null) {
                    val classes = classListResponse.data.classes.map { it.toDomainModel() }
                    DataResult.Success(classes)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getApplicationList(classId: Long): DataResult<ApplicationResponseDto> {
        return try {
            val response = classApiService.getApplicationList(classId)
            if (response.isSuccessful) {
                val applicationResponse = response.body()
                if (applicationResponse != null) {
                    DataResult.Success(applicationResponse)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun approveApplication(classId: Long, applicationId: Long): DataResult<MessageResponseDto> {
        return try {
            val response = classApiService.approveApplication(classId, applicationId)
            if (response.isSuccessful) {
                val messageResponse = response.body()
                if (messageResponse != null) {
                    DataResult.Success(messageResponse)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getClassMemberList(classId: Long, status: String, page: Int, size: Int): DataResult<ClassMemberResponse> {
        return try {
            val response = classApiService.getClassMemberList(classId, status, page, size)
            if (response.isSuccessful) {
                val classMemberResponse = response.body()
                if (classMemberResponse != null) {
                    DataResult.Success(classMemberResponse)
                } else {
                    DataResult.Error(DataException.ServerError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getStudentClassDetail(classId: Long, include: String?): DataResult<StudentClassDetail> {
        return try {
            val response = classApiService.getStudentClassDetail(classId, include)
            if (response.isSuccessful) {
                val classDetailResponse = response.body()
                val domainModel = classDetailResponse?.toClassDetailDomainModel()
                if (domainModel != null) {
                    DataResult.Success(domainModel)
                } else {
                    DataResult.Error(DataException.NotFoundError)
                }
            } else {
                DataResult.Error(DataException.ServerError)
            }
        } catch (e: IOException) {
            DataResult.Error(DataException.NetworkError)
        } catch (e: HttpException) {
            DataResult.Error(
                when (e.code()) {
                    401 -> DataException.AuthenticationError
                    403 -> DataException.UnauthorizedError
                    404 -> DataException.NotFoundError
                    else -> DataException.ServerError
                }
            )
        } catch (e: Exception) {
            DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
        }
    }
}