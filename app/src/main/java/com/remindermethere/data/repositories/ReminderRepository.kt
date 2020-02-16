package com.remindermethere.data.repositories

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.gson.Gson
import com.remindermethere.data.model.Reminder
import com.remindermethere.geofence.GeofenceBroadcastReceiver
import com.remindermethere.geofence.GeofenceErrorMessages
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class ReminderRepository(private val context: Context) : KoinComponent {

    companion object {
        private const val REMINDERS = "REMINDERS"
    }

    private val sharedPreferences: SharedPreferences by inject()
    private val gson: Gson by inject()
    private val geofencingClient: GeofencingClient by inject()

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun add(
        reminder: Reminder,
        success: () -> Unit,
        failure: (error: String) -> Unit
    ) {
        // 1
        val geofence = buildGeofence(reminder)
        if (geofence != null
            && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 2
            geofencingClient
                .addGeofences(buildGeofencingRequest(geofence), geofencePendingIntent)
                .addOnSuccessListener {
                    // 3
                    saveAll(getAll() + reminder)
                    success()
                }
                .addOnFailureListener {
                    // 4
                    failure(
                        GeofenceErrorMessages.getErrorString(
                            context,
                            it
                        )
                    )
                }
        }
    }

    private fun buildGeofence(reminder: Reminder): Geofence? {
        val latitude = reminder.latLng?.latitude
        val longitude = reminder.latLng?.longitude
        val radius = reminder.radius

        if (latitude != null && longitude != null && radius != null) {
            return Geofence.Builder()
                .setRequestId(reminder.id)
                .setCircularRegion(
                    latitude,
                    longitude,
                    radius.toFloat()
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
        }

        return null
    }

    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofences(listOf(geofence))
            .build()
    }

    fun remove(
        reminder: Reminder,
        success: () -> Unit,
        failure: (error: String) -> Unit
    ) {
        geofencingClient
            .removeGeofences(listOf(reminder.id))
            .addOnSuccessListener {
                saveAll(getAll() - reminder)
                success()
            }
            .addOnFailureListener {
                failure(
                    GeofenceErrorMessages.getErrorString(
                        context,
                        it
                    )
                )
            }
    }

    private fun saveAll(list: List<Reminder>) {
        sharedPreferences
            .edit()
            .putString(REMINDERS, gson.toJson(list))
            .apply()
    }

    fun getAll(): List<Reminder> {
        if (sharedPreferences.contains(REMINDERS)) {
            val remindersString = sharedPreferences.getString(REMINDERS, null)
            val arrayOfReminders = gson.fromJson(
                remindersString,
                Array<Reminder>::class.java
            )
            if (arrayOfReminders != null) {
                return arrayOfReminders.toList()
            }
        }
        return listOf()
    }

    fun get(requestId: String?) = getAll().firstOrNull { it.id == requestId }

    fun getLast() = getAll().lastOrNull()

}