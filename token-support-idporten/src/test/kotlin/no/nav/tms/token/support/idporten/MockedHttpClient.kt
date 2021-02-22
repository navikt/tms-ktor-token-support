package no.nav.tms.token.support.idporten

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
import no.nav.tms.token.support.idporten.authentication.OauthServerConfigurationMetadata


@KtorExperimentalAPI
val mockedClient = HttpClient(MockEngine) {
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
                "http://mocked-issuer/config" -> {
                    val responseHeaders = headersOf("Content-Type", ContentType.Application.Json.toString())
                    respond(metadataJson, OK,responseHeaders)
                }
                "http://mocked-issuer/token" -> {
                    respondOk()
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
        issuer = "http://mocked-issuer/provider",
        authorizationEndpoint = "http://mocked-issuer/auth",
        tokenEndpoint = "http://mocked-issuer/token",
        jwksUri = "http://mocked-issuer/jwks",
)

private val metadataJson: ByteArray = idportenMetadata.let { metadata ->
    objectMapper.writeValueAsBytes(metadata)
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
