package no.nav.tms.token.support.user.token.verification

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.HttpResponseData
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.serialization.jackson.*
import no.nav.tms.token.support.user.token.verification.TokenVerifier.Companion.OauthServerConfigurationMetadata


fun testClient(
    idPortenUrl: String,
    tokenxUrl: String,
): HttpClient {
    return HttpClient(MockEngine) {
        install(ContentNegotiation) {
            jackson {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }

        val idportenMetadata = OauthServerConfigurationMetadata(
            issuer = idPortenUrl,
            jwksUri = "$idPortenUrl/jwks",
        )

        val tokenxMetadata = OauthServerConfigurationMetadata(
            issuer = tokenxUrl,
            jwksUri = "$tokenxUrl/jwks",
        )

        engine {
            addHandler { request ->
                when (request.url.fullUrl) {
                    "$idPortenUrl/config" -> {
                        respondJson(idportenMetadata)
                    }
                    "$tokenxUrl/config" -> {
                        respondJson(tokenxMetadata)
                    }
                    else -> error("Unhandled ${request.url.fullUrl}")
                }
            }
        }
    }
}

private val objectMapper = jacksonObjectMapper()

private fun MockRequestHandleScope.respondJson(body: Any): HttpResponseData {
    val responseHeaders = headersOf("Content-Type", ContentType.Application.Json.toString())

    return respond(objectMapper.writeValueAsString(body), OK, responseHeaders)
}

private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
