package no.nav.tms.token.support.user.token.verification

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.engine.apache5.Apache5EngineConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner
import java.net.ProxySelector

internal object HttpClientBuilder {
    internal fun buildHttpClient(enableDefaultProxy: Boolean): HttpClient {
        return HttpClient(Apache5) {
            install(ContentNegotiation) {
                jackson {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            install(HttpTimeout)

            if (enableDefaultProxy) {
                enableSystemDefaultProxy()
            }
        }
    }

    private fun HttpClientConfig<Apache5EngineConfig>.enableSystemDefaultProxy() {
        engine {
            customizeClient { setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault())) }
        }
    }
}
