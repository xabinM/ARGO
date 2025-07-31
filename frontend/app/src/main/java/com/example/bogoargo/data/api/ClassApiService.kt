package com.example.bogoargo.data.api

import com.example.bogoargo.data.dto.ApplyClassRequest
import com.example.bogoargo.data.dto.ClassCreateRequest
import com.example.bogoargo.data.response.ClassDataDto
import com.example.bogoargo.data.response.ClassDetailResponse
import com.example.bogoargo.data.response.ClassListResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ClassDetailApiService {

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


    // 반 상세 정보 조회
    @GET("api/classes/{classId}")
    suspend fun getClassDetail(@Path("classId") classId: String): Response<ClassDataDto>

    @POST("/api/teacher/classes/create")
    suspend fun createClass(@Body  classCreateRequest: ClassCreateRequest) : Response<ClassDetailResponse>










}
