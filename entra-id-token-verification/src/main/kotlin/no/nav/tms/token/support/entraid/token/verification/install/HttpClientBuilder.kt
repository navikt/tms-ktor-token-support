package no.nav.tms.token.support.entraid.token.verification.install

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import io.ktor.client.*
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.engine.apache5.Apache5EngineConfig
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner
import java.net.ProxySelector

internal object HttpClientBuilder {
    internal fun build(enableDefaultProxy: Boolean): HttpClient {
        return HttpClient(Apache5) {
            install(ContentNegotiation) {
                jackson {
                    configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
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

