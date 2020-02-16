package com.remindermethere

import android.app.Application
import com.remindermethere.di.applicationModule
import org.koin.android.ext.android.startKoin

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(applicationModule))
    }
}