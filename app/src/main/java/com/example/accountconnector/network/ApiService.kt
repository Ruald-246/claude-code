package com.example.accountconnector.network

import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val username: String, val password: String)
data class RefreshRequest(val refreshToken: String)
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val userId: String
)
data class UserResponse(val userId: String, val username: String, val email: String)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/token/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequest): Response<Map<String, String>>

    @GET("auth/user")
    suspend fun getUser(@Header("Authorization") authorization: String): Response<UserResponse>
}
