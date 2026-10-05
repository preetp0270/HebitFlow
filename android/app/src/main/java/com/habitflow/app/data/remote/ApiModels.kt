package com.habitflow.app.data.remote

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

data class AuthData(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String
)

data class TokensData(
    val accessToken: String,
    val refreshToken: String
)

data class UserDto(
    val _id: String,
    val name: String,
    val email: String,
    val timezone: String? = null,
    val themePreference: String? = null
)

data class HabitDto(
    val _id: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val frequencyType: String,
    val selectedDays: List<Int>? = null,
    val targetPerWeek: Int? = null,
    val reminderEnabled: Boolean? = null,
    val reminderTime: String? = null,
    val startDate: String? = null,
    val isActive: Boolean? = null
)

data class HabitsData(val habits: List<HabitDto>)
data class HabitData(val habit: HabitDto, val stats: StatsDto? = null)

data class TodayData(
    val date: String,
    val items: List<TodayItemDto>,
    val progress: ProgressDto
)

data class TodayItemDto(
    val habit: HabitDto,
    val completed: Boolean,
    val currentStreak: Int,
    val longestStreak: Int
)

data class ProgressDto(
    val completed: Int,
    val total: Int,
    val percentage: Int
)

data class StatsDto(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompleted: Int = 0,
    val completionPercentage: Int = 0
)

data class CompletionDto(
    val _id: String? = null,
    val habitId: String? = null,
    val date: String? = null
)

data class CreateHabitRequest(
    val name: String,
    val description: String = "",
    val icon: String = "🎯",
    val color: String = "#6366F1",
    val frequencyType: String = "DAILY",
    val selectedDays: List<Int> = emptyList(),
    val targetPerWeek: Int = 1,
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null
)

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)
data class RefreshRequest(val refreshToken: String)
