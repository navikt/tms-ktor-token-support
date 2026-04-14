package no.nav.tms.token.support.entraid.token.verification

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.serialization.jackson.*


internal fun createMockedMockedClient(azureUrl: String) = HttpClient(MockEngine) {
    val azureMetadataResponse = """
        {
            "issuer": "$azureUrl",
            "jwks_uri": "$azureUrl/jwks"
        }
        """

    install(ContentNegotiation) {
        jackson {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    engine {
        addHandler { request ->
            when (request.url.fullUrl) {
                "$azureUrl/config" -> {
                    val responseHeaders = headersOf("Content-Type", ContentType.Application.Json.toString())
                    respond(azureMetadataResponse, OK, responseHeaders)
                }
                else -> error("Unhandled ${request.url.fullUrl}")
            }
        }
    }
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
