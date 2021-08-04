package no.nav.tms.token.support.azure.validation

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.util.*
import kotlinx.serialization.encodeToString
import no.nav.tms.token.support.azure.validation.ObjectMapper.objectMapper
import no.nav.tms.token.support.azure.validation.config.OauthServerConfigurationMetadata



@KtorExperimentalAPI
internal fun createMockedMockedClient() = HttpClient(MockEngine) {
    install(JsonFeature) {
        serializer = KotlinxSerializer(objectMapper)
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
    objectMapper.encodeToString(metadata)
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
