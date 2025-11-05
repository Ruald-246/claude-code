package com.example.mockapi

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class LoginRequest(val username: String, val password: String)
data class RefreshRequest(val refreshToken: String)
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val userId: String
)
data class UserResponse(val userId: String, val username: String, val email: String)
data class ErrorResponse(val error: String, val message: String)

class TokenManager {
    private val secret = "your-secret-key-change-in-production"
    private val algorithm = Algorithm.HMAC256(secret)
    private val validRefreshTokens = ConcurrentHashMap.newKeySet<String>()

    // Mock user database
    private val users = mapOf(
        "demo" to "password",
        "user1" to "pass123",
        "test" to "test123"
    )

    fun validateCredentials(username: String, password: String): Boolean {
        return users[username] == password
    }

    fun generateTokens(username: String): LoginResponse {
        val userId = UUID.randomUUID().toString()
        val accessToken = JWT.create()
            .withSubject(username)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .sign(algorithm)

        val refreshToken = JWT.create()
            .withSubject(username)
            .withClaim("userId", userId)
            .withClaim("type", "refresh")
            .withExpiresAt(Date(System.currentTimeMillis() + 2592000000)) // 30 days
            .sign(algorithm)

        validRefreshTokens.add(refreshToken)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = 3600,
            userId = userId
        )
    }

    fun refreshAccessToken(refreshToken: String): LoginResponse? {
        if (!validRefreshTokens.contains(refreshToken)) {
            return null
        }

        return try {
            val jwt = JWT.require(algorithm).build().verify(refreshToken)
            val username = jwt.subject
            val userId = jwt.getClaim("userId").asString()

            val newAccessToken = JWT.create()
                .withSubject(username)
                .withClaim("userId", userId)
                .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
                .sign(algorithm)

            LoginResponse(
                accessToken = newAccessToken,
                refreshToken = refreshToken,
                expiresIn = 3600,
                userId = userId
            )
        } catch (e: Exception) {
            null
        }
    }

    fun invalidateRefreshToken(refreshToken: String) {
        validRefreshTokens.remove(refreshToken)
    }

    fun verifyAccessToken(token: String): String? {
        return try {
            val jwt = JWT.require(algorithm).build().verify(token)
            jwt.subject
        } catch (e: Exception) {
            null
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }

        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
        }

        val tokenManager = TokenManager()

        routing {
            route("/auth") {
                post("/login") {
                    val request = call.receive<LoginRequest>()

                    if (tokenManager.validateCredentials(request.username, request.password)) {
                        val response = tokenManager.generateTokens(request.username)
                        call.respond(HttpStatusCode.OK, response)
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("unauthorized", "Invalid username or password")
                        )
                    }
                }

                post("/token/refresh") {
                    val request = call.receive<RefreshRequest>()
                    val response = tokenManager.refreshAccessToken(request.refreshToken)

                    if (response != null) {
                        call.respond(HttpStatusCode.OK, response)
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("invalid_token", "Invalid or expired refresh token")
                        )
                    }
                }

                post("/logout") {
                    val request = call.receive<RefreshRequest>()
                    tokenManager.invalidateRefreshToken(request.refreshToken)
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out successfully"))
                }

                get("/user") {
                    val authHeader = call.request.header(HttpHeaders.Authorization)
                    val token = authHeader?.removePrefix("Bearer ")

                    if (token != null) {
                        val username = tokenManager.verifyAccessToken(token)
                        if (username != null) {
                            call.respond(
                                HttpStatusCode.OK,
                                UserResponse(
                                    userId = UUID.randomUUID().toString(),
                                    username = username,
                                    email = "$username@example.com"
                                )
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                ErrorResponse("invalid_token", "Invalid or expired access token")
                            )
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("missing_token", "Authorization header required")
                        )
                    }
                }
            }

            get("/") {
                call.respondText("Mock Auth API - Server is running\n\nAvailable endpoints:\n" +
                        "POST /auth/login\n" +
                        "POST /auth/token/refresh\n" +
                        "POST /auth/logout\n" +
                        "GET /auth/user\n\n" +
                        "Test credentials:\n" +
                        "- demo/password\n" +
                        "- user1/pass123\n" +
                        "- test/test123")
            }
        }
    }.start(wait = true)

    println("Mock Auth API started on http://localhost:8080")
}
