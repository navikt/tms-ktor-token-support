package no.nav.tms.token.support.idporten.authentication.config

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.idporten.SecurityLevel
import no.nav.tms.token.support.idporten.SecurityLevel.*
import no.nav.tms.token.support.idporten.authentication.IdportenClientInterceptor
import no.nav.tms.token.support.idporten.authentication.OauthServerConfigurationMetadata
import no.nav.tms.token.support.idporten.authentication.config.HttpClientBuilder.buildHttpClient
import java.net.URL
import java.util.concurrent.TimeUnit

internal class RuntimeContext(
        val tokenCookieName: String,
        val contextPath: String,
        val postLoginRedirectUri: String,
        val secureCookie: Boolean,
        val postLogoutRedirectUri: String,
        val securityLevel: SecurityLevel
) {
    val environment = Environment()

    private val httpClient = buildHttpClient()
    val metadata = fetchMetadata(httpClient, environment.idportenWellKnownUrl)

    private val idportenClientInterceptor = createIdPortenClientInterceptor(environment, metadata)
    val oauth2ServerSettings = createOAuth2ServerSettings(environment, securityLevel, metadata, idportenClientInterceptor)
    val jwkProvider = createJwkProvider(metadata)
}

private fun createOAuth2ServerSettings(
        environment: Environment,
        securityLevel: SecurityLevel,
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
        authorizeUrlInterceptor = createAuthorizeUrlInterceptior(securityLevel),
        accessTokenInterceptor = idportenClientInterceptor.appendClientAssertion
)

private fun createAuthorizeUrlInterceptior(securityLevel: SecurityLevel): URLBuilder.() -> Unit {
    return {
        parameters.append("response_mode", "query")

        when (securityLevel) {
            LEVEL_3 -> parameters.append("acr_values", "Level3")
            LEVEL_4 -> parameters.append("acr_values", "Level4")
            NOT_SPECIFIED -> {}
        }
    }
}

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
