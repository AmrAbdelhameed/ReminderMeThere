package com.remindermethere.ui.base

import androidx.appcompat.app.AppCompatActivity
import com.remindermethere.data.repositories.ReminderRepository
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity() {
    private val reminderRepository: ReminderRepository by inject()
    fun getRepository() = reminderRepository
}