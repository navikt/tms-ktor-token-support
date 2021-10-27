package no.nav.tms.token.support.idporten.authentication.config

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

internal object HttpClientBuilder {
    internal fun buildHttpClient(enableDefaultProxy: Boolean): HttpClient {
        return HttpClient(Apache) {
            install(JsonFeature) {
                serializer = buildJsonSerializer()
            }
            install(HttpTimeout)

            if (enableDefaultProxy) {
                enableSystemDefaultProxy()
            }
        }
    }

    private fun buildJsonSerializer() = KotlinxSerializer(
            kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            }
    )

    private fun HttpClientConfig<ApacheEngineConfig>.enableSystemDefaultProxy() {
        engine {
            customizeClient { setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault())) }
        }
    }
}

