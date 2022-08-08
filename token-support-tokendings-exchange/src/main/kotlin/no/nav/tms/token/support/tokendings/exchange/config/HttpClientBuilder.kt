package no.nav.tms.token.support.tokendings.exchange.config

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

internal object HttpClientBuilder {
    internal fun buildHttpClient(): HttpClient {
        return HttpClient(Apache) {
            install(ContentNegotiation) {
                json(kotlinxSerializer())
            }
            install(HttpTimeout)
        }
    }

    private fun kotlinxSerializer() =
        Json {
            ignoreUnknownKeys = true
        }
}

