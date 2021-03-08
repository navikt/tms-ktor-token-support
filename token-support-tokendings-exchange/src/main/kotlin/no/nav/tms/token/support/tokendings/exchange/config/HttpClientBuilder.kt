package no.nav.tms.token.support.tokendings.exchange.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*

internal object HttpClientBuilder {
    internal fun buildHttpClient(): HttpClient {
        return HttpClient(Apache) {
            install(JsonFeature) {
                serializer = buildJsonSerializer()
            }
            install(HttpTimeout)
        }
    }

    private fun buildJsonSerializer(): JacksonSerializer {
        return JacksonSerializer {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }
}

