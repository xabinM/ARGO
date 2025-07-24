package com.example.bogoargo

import android.app.Application
import com.example.bogoargo.data.api.ApiClient

class ArgoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}