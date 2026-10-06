package com.habitflow.app.data.repository

import com.habitflow.app.data.local.AppDatabase
import com.habitflow.app.data.local.CompletionEntity
import com.habitflow.app.data.local.HabitEntity
import com.habitflow.app.data.remote.ApiClient
import com.habitflow.app.data.remote.CreateHabitRequest
import com.habitflow.app.data.remote.HabitDto
import com.habitflow.app.data.remote.TodayData
import com.habitflow.app.util.ReminderScheduler
import com.habitflow.app.util.TokenStore
import com.habitflow.app.HabitFlowApp
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

class HabitRepository(
    private val db: AppDatabase,
    private val tokenStore: TokenStore
) {
    private val api get() = ApiClient.api
    private val habitDao = db.habitDao()
    private val completionDao = db.completionDao()
    private val gson = Gson()

    fun observeHabits(): Flow<List<HabitEntity>> = habitDao.observeActive()

    suspend fun syncFromServer() {
        if (!tokenStore.isLoggedIn()) return
        val res = api.listHabits()
        val habits = res.data?.habits ?: return
        val entities = habits.map { it.toEntity() }
        habitDao.upsertAll(entities)
        entities.forEach { applyReminder(it) }
    }

    suspend fun getToday(): Result<TodayData> = runCatching {
        val res = api.today()
        res.data ?: error(res.message ?: "Failed to load today")
    }

    /** Offline-friendly complete: write Room first, then try API */
    suspend fun completeHabit(habitId: String, date: String = LocalDate.now().toString()) {
        val existing = completionDao.get(habitId, date)
        if (existing != null && !existing.pendingDelete) return

        val local = CompletionEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            habitId = habitId,
            date = date,
            completedAt = System.currentTimeMillis(),
            pendingSync = true,
            pendingDelete = false
        )
        completionDao.upsert(local)

        try {
            api.complete(habitId, mapOf("date" to date))
            completionDao.upsert(local.copy(pendingSync = false))
        } catch (_: Exception) {
            // stays pendingSync = true for later sync
        }
    }

    suspend fun uncompleteHabit(habitId: String, date: String = LocalDate.now().toString()) {
        completionDao.markDeleted(habitId, date)
        try {
            api.uncomplete(habitId, mapOf("date" to date))
            val c = completionDao.get(habitId, date)
            if (c != null) completionDao.deleteById(c.id)
        } catch (_: Exception) {
            // pending delete remains
        }
    }

    suspend fun createHabit(req: CreateHabitRequest): Result<HabitEntity> = runCatching {
        val res = api.createHabit(req)
        val habit = res.data?.habit ?: error(res.message ?: "Create failed")
        val entity = habit.toEntity()
        habitDao.upsert(entity)
        applyReminder(entity)
        entity
    }

    suspend fun deleteHabit(id: String) {
        ReminderScheduler.cancel(HabitFlowApp.instance, id)
        habitDao.softDelete(id)
        try {
            api.deleteHabit(id)
        } catch (_: Exception) {
        }
    }

    fun applyReminder(entity: HabitEntity) {
        val ctx = HabitFlowApp.instance
        if (entity.reminderEnabled && !entity.reminderTime.isNullOrBlank()) {
            ReminderScheduler.schedule(ctx, entity.id, entity.name, entity.reminderTime)
        } else {
            ReminderScheduler.cancel(ctx, entity.id)
        }
    }

    /** Push pending local changes when connectivity returns */
    suspend fun pushPending() {
        if (!tokenStore.isLoggedIn()) return
        for (c in completionDao.getPendingSync()) {
            try {
                if (c.pendingDelete) {
                    api.uncomplete(c.habitId, mapOf("date" to c.date))
                    completionDao.deleteById(c.id)
                } else {
                    api.complete(c.habitId, mapOf("date" to c.date))
                    completionDao.upsert(c.copy(pendingSync = false))
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun HabitDto.toEntity() = HabitEntity(
        id = _id,
        name = name,
        description = description.orEmpty(),
        icon = icon ?: "🎯",
        color = color ?: "#6366F1",
        frequencyType = frequencyType,
        selectedDays = gson.toJson(selectedDays ?: emptyList<Int>()),
        targetPerWeek = targetPerWeek ?: 1,
        reminderEnabled = reminderEnabled ?: false,
        reminderTime = reminderTime,
        startDate = startDate?.take(10) ?: LocalDate.now().toString(),
        isActive = isActive ?: true,
        updatedAt = System.currentTimeMillis(),
        pendingSync = false
    )
}
