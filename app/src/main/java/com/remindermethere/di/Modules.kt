package com.remindermethere.di

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.remindermethere.data.repositories.ReminderRepository
import com.remindermethere.utils.PREF_NAME
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val applicationModule = module(override = true) {
    single { Gson() }
    single<SharedPreferences> {
        androidContext().getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
    }
    single { ReminderRepository(androidContext()) }
    single { LocationServices.getGeofencingClient(androidContext()) }
}
