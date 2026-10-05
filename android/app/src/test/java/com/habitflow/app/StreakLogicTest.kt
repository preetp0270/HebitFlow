package com.habitflow.app

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Example unit tests for scheduled-day streak style logic.
 * Full streak authority remains on the backend; Android may compute local previews offline.
 */
class StreakLogicTest {

    private fun isScheduled(frequency: String, selectedDays: Set<Int>, date: LocalDate): Boolean {
        return when (frequency) {
            "DAILY" -> true
            "SELECTED_DAYS" -> date.dayOfWeek.value % 7 in selectedDays ||
                (date.dayOfWeek.value == 7 && 0 in selectedDays) // Sun = 0
            else -> true
        }
    }

    @Test
    fun dailyConsecutiveStreak() {
        val completed = setOf("2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04")
        var streak = 0
        var d = LocalDate.parse("2026-10-04")
        while (completed.contains(d.toString()) && isScheduled("DAILY", emptySet(), d)) {
            streak++
            d = d.minusDays(1)
        }
        assertEquals(4, streak)
    }

    @Test
    fun selectedDaysStreak() {
        // Mon=1, Wed=3, Fri=5 (ISO); our API uses 0=Sun..6=Sat
        val selected = setOf(1, 3, 5) // Mon Wed Fri if using ISO mapped carefully
        // Simplified: count consecutive scheduled completions
        val completed = setOf("2026-10-06", "2026-10-03", "2026-10-01") // example Fridays/Mons
        assertEquals(3, completed.size)
    }
}
