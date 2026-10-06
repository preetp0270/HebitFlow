package com.habitflow.app.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.habitflow.app.HabitFlowApp
import com.habitflow.app.MainActivity
import com.habitflow.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "habit"
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: ""

        val open = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, 0, open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, HabitFlowApp.CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("HabitFlow reminder")
            .setContentText("Time for \"$name\"")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify((habitId.ifBlank { name }).hashCode(), notification)

        // Re-arm for tomorrow (daily reminder)
        if (habitId.isNotBlank()) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val habit = HabitFlowApp.instance.database.habitDao().getById(habitId)
                    val time = habit?.reminderTime
                    if (habit != null && habit.reminderEnabled && !time.isNullOrBlank()) {
                        ReminderScheduler.schedule(context, habitId, habit.name, time)
                    }
                } catch (_: Exception) {
                } finally {
                    pending.finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_NAME = "habit_name"
        const val EXTRA_HABIT_ID = "habit_id"
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = HabitFlowApp.instance.database.habitDao().getActive()
                val list = habits
                    .filter { it.reminderEnabled && !it.reminderTime.isNullOrBlank() }
                    .map { Triple(it.id, it.name, it.reminderTime!!) }
                ReminderScheduler.rescheduleAll(context, list)
            } catch (_: Exception) {
            } finally {
                pending.finish()
            }
        }
    }
}
