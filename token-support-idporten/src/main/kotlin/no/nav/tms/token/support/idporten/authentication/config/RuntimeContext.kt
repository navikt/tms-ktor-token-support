package no.nav.tms.token.support.idporten.authentication.config

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.idporten.authentication.IdportenClientInterceptor
import no.nav.tms.token.support.idporten.authentication.OauthServerConfigurationMetadata
import no.nav.tms.token.support.idporten.authentication.config.HttpClientBuilder.buildHttpClient
import java.net.URL
import java.util.concurrent.TimeUnit

internal class RuntimeContext(
        val tokenCookieName: String,
        val contextPath: String,
        val postLoginRedirectUri: String
) {
    val environment = Environment()

    private val httpClient = buildHttpClient()
    val metadata = fetchMetadata(httpClient, environment.idportenWellKnownUrl)

    private val idportenClientInterceptor = createIdPortenClientInterceptor(environment, metadata)
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

private fun createJwkProvider(metadata: OauthServerConfigurationMetadata): JwkProvider = JwkProviderBuilder(URL(metadata.jwksUri))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

private fun createIdPortenClientInterceptor(environment: Environment, metadata: OauthServerConfigurationMetadata) = IdportenClientInterceptor(
        privateJwk = environment.idportenClientJwk,
        clientId = environment.idportenClientId,
        audience = metadata.issuer
)
