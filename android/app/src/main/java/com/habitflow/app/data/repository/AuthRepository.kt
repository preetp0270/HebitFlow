package com.habitflow.app.data.repository

import com.habitflow.app.data.remote.ApiClient
import com.habitflow.app.data.remote.LoginRequest
import com.habitflow.app.data.remote.RefreshRequest
import com.habitflow.app.data.remote.RegisterRequest
import com.habitflow.app.data.remote.UserDto
import com.habitflow.app.util.TokenStore

class AuthRepository(private val tokenStore: TokenStore) {
    private val api get() = ApiClient.api

    suspend fun login(email: String, password: String): Result<UserDto> = runCatching {
        val res = api.login(LoginRequest(email, password))
        val data = res.data ?: error(res.message ?: "Login failed")
        tokenStore.saveTokens(data.accessToken, data.refreshToken)
        data.user
    }

    suspend fun register(name: String, email: String, password: String): Result<UserDto> = runCatching {
        val res = api.register(RegisterRequest(name, email, password))
        val data = res.data ?: error(res.message ?: "Register failed")
        tokenStore.saveTokens(data.accessToken, data.refreshToken)
        data.user
    }

    suspend fun logout() {
        val refresh = tokenStore.getRefreshToken()
        try {
            if (!refresh.isNullOrBlank()) {
                api.logout(RefreshRequest(refresh))
            }
        } catch (_: Exception) {
        }
        tokenStore.clear()
    }

    suspend fun isLoggedIn(): Boolean = tokenStore.isLoggedIn()
}
