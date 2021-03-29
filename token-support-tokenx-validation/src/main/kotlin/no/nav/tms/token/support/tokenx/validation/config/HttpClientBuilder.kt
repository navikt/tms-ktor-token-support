package no.nav.tms.token.support.tokenx.validation.config

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.serialization.json.Json

internal object HttpClientBuilder {
    internal fun build(): HttpClient {
        return HttpClient(Apache) {
            install(JsonFeature) {
                serializer = buildJsonSerializer()
            }
            install(HttpTimeout)
        }
    }

    private fun buildJsonSerializer() = KotlinxSerializer(
            Json {
                ignoreUnknownKeys = true
            }
    )
}

