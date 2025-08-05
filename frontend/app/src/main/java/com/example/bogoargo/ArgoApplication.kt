package com.example.bogoargo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ArgoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}