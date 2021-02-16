package no.nav.tms.token.support.idporten

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.TimeUnit

internal class RuntimeContext(
        val tokenCookieName: String,
        val contextPath: String,
        val postLoginRedirectUri: String
) {

    val environment = Environment(contextPath)

    val httpClient = buildHttpClient()
    val metadata = fetchMetadata(httpClient, environment.idportenWellKnownUrl)
    val idportenClientInterceptor = createIdPortenClientInterceptor(environment, metadata)
    val oauth2ServerSettings = createOAuth2ServerSettings(environment, metadata, idportenClientInterceptor)
    val jwkProvider = createJwkProvider(metadata)
}

private fun createOAuth2ServerSettings(
        environment: Environment,
        metadata: OauthServerConfigurationMetadata,
        idportenClientInterceptor: IdportenClientInterceptor
) = OAuthServerSettings.OAuth2ServerSettings(
        name = "IdPorten",
        authorizeUrl = metadata.authorizationEndpoint,
        accessTokenUrl = metadata.tokenEndpoint,
        clientId = environment.idportenClientId,
        clientSecret = "",
        accessTokenRequiresBasicAuth = false,
        requestMethod = HttpMethod.Post,
        defaultScopes = listOf(Idporten.scope),
        authorizeUrlInterceptor = { this.parameters.append("response_mode", "query") },
        accessTokenInterceptor = idportenClientInterceptor.appendClientAssertion
)

private fun fetchMetadata(httpClient: HttpClient, idPortenUrl: String) = runBlocking {
    httpClient.getOAuthServerConfigurationMetadata(idPortenUrl)
}

private fun createJwkProvider(metadata: OauthServerConfigurationMetadata) = JwkProviderBuilder(URL(metadata.jwksUri))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

private fun createIdPortenClientInterceptor(environment: Environment, metadata: OauthServerConfigurationMetadata) = IdportenClientInterceptor(
        privateJwk = environment.idportenClientJwk,
        clientId = environment.idportenClientId,
        audience = metadata.issuer
)

private fun buildHttpClient(): HttpClient {
    return HttpClient(Apache) {
        install(JsonFeature) {
            serializer = buildJsonSerializer()
        }
        install(HttpTimeout)
    }
}

private fun buildJsonSerializer(): JacksonSerializer {
    return JacksonSerializer {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}

private suspend fun HttpClient.getOAuthServerConfigurationMetadata(url: String): OauthServerConfigurationMetadata = withContext(Dispatchers.IO) {
    request {
        method = HttpMethod.Get
        url(url)
        accept(ContentType.Application.Json)
    }
}
