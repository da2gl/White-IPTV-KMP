package com.simplevideo.whiteiptv.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating configured HttpClient instances
 */
object HttpClientFactory {

    /**
     * Creates HttpClient with:
     * - JSON content negotiation
     * - Request/response logging
     * - Configured timeouts
     */
    fun create(): HttpClient {
        return HttpClient {
            // JSON serialization/deserialization
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }

            // HTTP request/response logging
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[HTTP] $message")
                    }
                }
                level = LogLevel.INFO
            }

            // Timeout configuration
            install(HttpTimeout) {
                connectTimeoutMillis = NetworkConfig.CONNECT_TIMEOUT
                socketTimeoutMillis = NetworkConfig.SOCKET_TIMEOUT
                requestTimeoutMillis = NetworkConfig.REQUEST_TIMEOUT
            }
        }
    }
}
