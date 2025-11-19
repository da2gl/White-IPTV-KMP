package com.simplevideo.whiteiptv.data.network

import co.touchlab.kermit.Logger
import com.simplevideo.whiteiptv.common.AppLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.Logger as KtorLogger

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
                logger = object : KtorLogger {
                    override fun log(message: String) {
                        Logger.withTag(AppLogger.Tags.HTTP).i { message }
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
