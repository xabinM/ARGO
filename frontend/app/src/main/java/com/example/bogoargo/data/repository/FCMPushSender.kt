package com.example.bogoargo.data.repository

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import com.example.bogoargo.data.api.FCMPushApi
import com.example.bogoargo.data.storage.SecureStorage

class FCMPushSender @Inject constructor(
    private val api: FCMPushApi,
    private val secureStorage: SecureStorage
) {
    private companion object {
        const val TAG = "PushSender"
    }

    private fun <T> logCallback(action: String) = object : Callback<T> {
        override fun onResponse(call: Call<T>, resp: Response<T>) {
            Log.d(TAG, "$action done code=${resp.code()}")
        }
        override fun onFailure(call: Call<T>, t: Throwable) {
            Log.e(TAG, "$action fail=${t.message}", t)
        }
    }

    fun sendCurrentToken() {
        val token = secureStorage.getFcmToken()
        if (token.isNullOrBlank()) {
            Log.w(TAG, "저장된 FCM 토큰 없음, 전송 불가")
            return
        }
//        Log.d(TAG, "token: $token")
        api.register(token).enqueue(logCallback<Unit>("register"))
    }

    fun unregisterToken() {
        api.unregister().enqueue(logCallback<Unit>("unregister"))
    }
}
