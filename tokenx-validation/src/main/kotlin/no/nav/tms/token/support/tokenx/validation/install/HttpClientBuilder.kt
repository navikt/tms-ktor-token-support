package no.nav.tms.token.support.tokenx.validation.install

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*

internal object HttpClientBuilder {
    internal fun build(): HttpClient {
        return HttpClient(Apache) {
            install(ContentNegotiation) {
                jackson {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }

            install(HttpTimeout)
        }
    }
}

