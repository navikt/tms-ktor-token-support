package no.nav.tms.token.support.tokendings.exchange.config

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*

internal object HttpClientBuilder {
    internal fun buildHttpClient(): HttpClient {
        return HttpClient(Apache) {
            install(JsonFeature) {
                serializer = buildJsonSerializer()
            }
            install(HttpTimeout)
        }
    }

    private fun buildJsonSerializer() = KotlinxSerializer (
            kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            }
    )
}

