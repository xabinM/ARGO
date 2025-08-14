package com.example.bogoargo.data.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface FCMPushApi {
    @FormUrlEncoded
    @POST("api/fcm/register")
    fun register(@Field("fcmToken") token: String): Call<Unit>

    @POST("api/fcm/unregister")
    fun unregister(): Call<Unit>
}
