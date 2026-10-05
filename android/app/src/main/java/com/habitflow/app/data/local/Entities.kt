package com.habitflow.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val color: String,
    val frequencyType: String,
    val selectedDays: String, // JSON array
    val targetPerWeek: Int,
    val reminderEnabled: Boolean,
    val reminderTime: String?,
    val startDate: String,
    val isActive: Boolean,
    val updatedAt: Long,
    val pendingSync: Boolean = false,
)

@Entity(
    tableName = "completions",
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class CompletionEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val date: String, // yyyy-MM-dd
    val completedAt: Long,
    val pendingSync: Boolean = false,
    val pendingDelete: Boolean = false,
)
