package no.nav.tms.token.support.tokenx.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.util.*
import no.nav.tms.token.support.tokenx.validation.config.OauthServerConfigurationMetadata



@KtorExperimentalAPI
internal fun createMockedMockedClient() = HttpClient(MockEngine) {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
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

private val objectMapper = ObjectMapper().apply {
    registerModule(KotlinModule())
    registerModule(JavaTimeModule())
}

internal val idportenMetadata = OauthServerConfigurationMetadata(
        issuer = "http://tokendings-url/provider",
        authorizationEndpoint = "http://tokendings-url/auth",
        tokenEndpoint = "http://tokendings-url/token",
        jwksUri = "http://tokendings-url/jwks",
)

private val metadataJson: ByteArray = idportenMetadata.let { metadata ->
    objectMapper.writeValueAsBytes(metadata)
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
