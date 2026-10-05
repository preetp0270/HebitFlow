package com.habitflow.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.habitflow.app.data.local.AppDatabase
import com.habitflow.app.data.remote.ApiClient
import com.habitflow.app.data.repository.AuthRepository
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.util.TokenStore

class HabitFlowApp : Application() {
    lateinit var tokenStore: TokenStore
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var habitRepository: HabitRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        tokenStore = TokenStore(this)
        database = AppDatabase.getInstance(this)
        ApiClient.init(tokenStore)
        authRepository = AuthRepository(tokenStore)
        habitRepository = HabitRepository(database, tokenStore)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_REMINDERS,
                getString(R.string.channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_reminders_desc)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_REMINDERS = "habit_reminders"
        lateinit var instance: HabitFlowApp
            private set
    }
}
