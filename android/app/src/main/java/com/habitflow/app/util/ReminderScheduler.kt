package com.habitflow.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

/**
 * Schedules local daily habit reminders with AlarmManager.
 * Time format from API/UI: "HH:mm" (24-hour).
 */
object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    fun schedule(context: Context, habitId: String, habitName: String, timeHHmm: String) {
        val (hour, minute) = parseTime(timeHHmm) ?: run {
            Log.w(TAG, "Invalid time: $timeHHmm")
            return
        }

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, habitId, habitName)

        val triggerAt = nextTriggerMillis(hour, minute)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
            Log.i(TAG, "Scheduled \"$habitName\" at $timeHHmm (next: $triggerAt)")
        } catch (e: SecurityException) {
            // Android 12+ may block exact alarms without permission
            Log.e(TAG, "Exact alarm not allowed, falling back to inexact", e)
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context, habitId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, habitId, "")
        am.cancel(pi)
        pi.cancel()
        Log.i(TAG, "Cancelled reminder for $habitId")
    }

    /** Re-schedule all active habits that have reminders (e.g. after boot). */
    fun rescheduleAll(
        context: Context,
        habits: List<Triple<String, String, String>> // id, name, timeHHmm
    ) {
        habits.forEach { (id, name, time) ->
            if (time.isNotBlank()) schedule(context, id, name, time)
        }
    }

    private fun pendingIntent(context: Context, habitId: String, habitName: String): PendingIntent {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = "com.habitflow.app.REMINDER"
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, habitName)
        }
        val requestCode = habitId.hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun parseTime(timeHHmm: String): Pair<Int, Int>? {
        val parts = timeHHmm.trim().split(":")
        if (parts.size < 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h to m
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis
    }
}
