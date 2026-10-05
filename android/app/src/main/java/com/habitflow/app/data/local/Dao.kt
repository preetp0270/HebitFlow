package com.habitflow.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY name")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isActive = 1")
    suspend fun getActive(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(habits: List<HabitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: HabitEntity)

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("UPDATE habits SET isActive = 0, pendingSync = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("SELECT * FROM habits WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<HabitEntity>
}

@Dao
interface CompletionDao {
    @Query("SELECT * FROM completions WHERE date = :date AND pendingDelete = 0")
    suspend fun getForDate(date: String): List<CompletionEntity>

    @Query("SELECT * FROM completions WHERE habitId = :habitId AND pendingDelete = 0 ORDER BY date DESC")
    suspend fun getForHabit(habitId: String): List<CompletionEntity>

    @Query("SELECT * FROM completions WHERE habitId = :habitId AND date = :date AND pendingDelete = 0 LIMIT 1")
    suspend fun get(habitId: String, date: String): CompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(completion: CompletionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<CompletionEntity>)

    @Query("UPDATE completions SET pendingDelete = 1, pendingSync = 1 WHERE habitId = :habitId AND date = :date")
    suspend fun markDeleted(habitId: String, date: String)

    @Query("SELECT * FROM completions WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<CompletionEntity>

    @Query("DELETE FROM completions WHERE id = :id")
    suspend fun deleteById(id: String)
}
