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

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(EXTRA_HABIT_NAME) ?: "habit"
        val open = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, 0, open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, HabitFlowApp.CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("HabitFlow")
            .setContentText("Time for your \"$name\" habit 💧")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(name.hashCode(), notification)
    }

    companion object {
        const val EXTRA_HABIT_NAME = "habit_name"
        const val EXTRA_HABIT_ID = "habit_id"
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Re-schedule reminders after boot if needed (extend with WorkManager/AlarmManager)
    }
}
