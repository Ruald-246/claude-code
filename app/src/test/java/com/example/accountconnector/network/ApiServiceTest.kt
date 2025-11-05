package com.example.accountconnector.network

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class ApiServiceTest {

    @Test
    fun `LoginRequest data class creates correctly`() {
        val request = LoginRequest("testuser", "testpass")
        assertEquals("testuser", request.username)
        assertEquals("testpass", request.password)
    }

    @Test
    fun `RefreshRequest data class creates correctly`() {
        val request = RefreshRequest("token123")
        assertEquals("token123", request.refreshToken)
    }

    @Test
    fun `LoginResponse data class creates correctly`() {
        val response = LoginResponse(
            accessToken = "access123",
            refreshToken = "refresh123",
            expiresIn = 3600,
            userId = "user123"
        )
        assertEquals("access123", response.accessToken)
        assertEquals("refresh123", response.refreshToken)
        assertEquals(3600, response.expiresIn)
        assertEquals("user123", response.userId)
    }

    @Test
    fun `UserResponse data class creates correctly`() {
        val response = UserResponse(
            userId = "user123",
            username = "testuser",
            email = "test@example.com"
        )
        assertEquals("user123", response.userId)
        assertEquals("testuser", response.username)
        assertEquals("test@example.com", response.email)
    }
}
