package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.request.ClassCreateRequest
import com.example.bogoargo.data.dto.response.ApplicationResponseDto
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.response.ClassCreateResponse
import com.example.bogoargo.data.response.ClassDataDto
import com.example.bogoargo.data.response.ClassDetailResponse
import com.example.bogoargo.data.response.ClassLeaveResponse
import com.example.bogoargo.data.response.ClassListResponse
import com.example.bogoargo.data.response.ClassMemberResponse
import com.example.bogoargo.data.response.applyClassResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ClassApiService {

    // 반 리스트 조회
    @GET("api/teacher/classes")
    suspend fun getTeacherClassList(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10,
        @Query("status") status: String? = "active"
    ): Response<ClassListResponse>

    @GET("api/student/classes")
    suspend fun getStudentClassList(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10,
        @Query("status") status: String? = "active"
    ): Response<ClassListResponse>


    // 반 상세 정보 조회 (교사 기능)
    @GET("api/teacher/classes/{classId}")
    suspend fun getClassDetail(
        @Path("classId") classId: Long
    ): Response<ClassDetailResponse>

    // 반 상세 정보 조회 (학생)
    @GET("api/student/classes/{classId}")
    suspend fun getStudentClassDetail(
        @Path("classId") classId: Long
    ): Response<ClassDataDto>

    // 반 생성 (교사 기능)
    @POST("api/teacher/classes")
    suspend fun createClass(
        @Body  classCreateRequest: ClassCreateRequest
    ) : Response<ClassCreateResponse>

    // 참여 신청한 학생 목록 조회
    @GET("api/teacher/classes/{classId}/applications")
    suspend fun getApplicationList(
        @Path("classId") classId: Long
    ): Response<ApplicationResponseDto>

    // 참여 신청 승인, 거절 (교사 기능) //TODO: 신청 리스트로 전달
    @PUT("api/teacher/classes/{classId}/applications/{applicationId}")
    suspend fun approveApplication(
        @Path("classId") classId: Long,
        @Path("applicationId") applicationId: Long
    ): Response<MessageResponseDto>

    // 반에 소속된 학생 목록 조회 (교사 기능)
    @GET("api/teacher/classes/{classId}/students")
    suspend fun getClassMemberList(
        @Path("classId") classId: Long,
        @Query("status") status: String,
        @Query("page") page: Int = 10,
        @Query("size") size: Int = 10
    ): Response<ClassMemberResponse>

    // 반 삭제 (교사 기능)
    @DELETE("api/teacher/classes/{classId}")
    suspend fun deleteClass(
        @Path("classId") classId: Long
    ): Response<MessageResponseDto>


    // 반 참여 신청
    @POST("api/student/classes/apply")
    suspend fun applyClass(
        @Query("inviteCode") inviteCode: String
    ): Response<applyClassResponse>

    // 반 탈퇴
    @DELETE("api/student/classes/{classId}/leave")
    suspend fun leaveClass(
        @Path("classId") classId: Long
    ): Response<ClassLeaveResponse>








}
