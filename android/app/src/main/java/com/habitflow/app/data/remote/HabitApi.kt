package com.habitflow.app.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HabitApi {
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<AuthData>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<AuthData>

    @POST("api/auth/logout")
    suspend fun logout(@Body body: RefreshRequest): ApiResponse<Any>

    @POST("api/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): ApiResponse<TokensData>

    @GET("api/auth/me")
    suspend fun me(): ApiResponse<Map<String, UserDto>>

    @GET("api/habits")
    suspend fun listHabits(): ApiResponse<HabitsData>

    @GET("api/habits/today")
    suspend fun today(@Query("date") date: String? = null): ApiResponse<TodayData>

    @GET("api/habits/{id}")
    suspend fun getHabit(@Path("id") id: String): ApiResponse<HabitData>

    @POST("api/habits")
    suspend fun createHabit(@Body body: CreateHabitRequest): ApiResponse<HabitData>

    @PUT("api/habits/{id}")
    suspend fun updateHabit(@Path("id") id: String, @Body body: CreateHabitRequest): ApiResponse<HabitData>

    @DELETE("api/habits/{id}")
    suspend fun deleteHabit(@Path("id") id: String): ApiResponse<Any>

    @POST("api/habits/{id}/complete")
    suspend fun complete(@Path("id") id: String, @Body body: Map<String, String> = emptyMap()): ApiResponse<Any>

    @DELETE("api/habits/{id}/complete")
    suspend fun uncomplete(@Path("id") id: String, @Body body: Map<String, String> = emptyMap()): ApiResponse<Any>
}
