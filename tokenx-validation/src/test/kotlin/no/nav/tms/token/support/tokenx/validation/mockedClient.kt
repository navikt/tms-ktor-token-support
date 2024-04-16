package no.nav.tms.token.support.tokenx.validation

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.serialization.jackson.*
import no.nav.tms.token.support.tokenx.validation.install.OauthServerConfigurationMetadata



internal fun createMockedMockedClient() = HttpClient(MockEngine) {
    install(ContentNegotiation) {
        jackson {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    engine {
        addHandler { request ->
            when (request.url.fullUrl) {
                "http://tokendings-url/config" -> {
                    val responseHeaders = headersOf("Content-Type", ContentType.Application.Json.toString())
                    respond(metadataJson, OK,responseHeaders)
                }
                else -> error("Unhandled ${request.url.fullUrl}")
            }
        }
    }
}

internal val idportenMetadata = OauthServerConfigurationMetadata(
        issuer = "http://tokendings-url/provider",
        authorizationEndpoint = "http://tokendings-url/auth",
        tokenEndpoint = "http://tokendings-url/token",
        jwksUri = "http://tokendings-url/jwks",
)

private val metadataJson: String = idportenMetadata.let { metadata ->
    jacksonObjectMapper().writeValueAsString(metadata)
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
