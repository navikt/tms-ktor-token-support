package no.nav.tms.token.support.idporten.sidecar.authentication.config

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.idporten.sidecar.authentication.AuthConfiguration
import no.nav.tms.token.support.idporten.sidecar.authentication.LevelOfAssuranceInternal
import no.nav.tms.token.support.idporten.sidecar.authentication.OauthServerConfigurationMetadata
import no.nav.tms.token.support.idporten.sidecar.authentication.config.HttpClientBuilder.buildHttpClient
import java.net.URL
import java.util.concurrent.TimeUnit

internal class RuntimeContext(
    val postLoginRedirectUri: String,
    val usesRootPath: Boolean,
    val contextPath: String,
    fallbackTokenCookieEnabled: Boolean,
    fallbackTokenCookieName: String,
    minLevelOfAssurance: LevelOfAssuranceInternal,

    enableDefaultProxy: Boolean
) {
    val environment = Environment()

    val httpClient = buildHttpClient(enableDefaultProxy)

    val metadata = fetchMetadata(httpClient, environment.idportenWellKnownUrl)
    val jwkProvider = createJwkProvider(metadata)

    val authConfiguration = AuthConfiguration(
        jwkProvider = jwkProvider,
        issuer = metadata.issuer,
        fallbackTokenCookieEnabled = fallbackTokenCookieEnabled,
        fallbackTokenCookieName = fallbackTokenCookieName,
        minLevelOfAssurance = minLevelOfAssurance
    )
}

private fun fetchMetadata(httpClient: HttpClient, idPortenUrl: String) = runBlocking {
    httpClient.getOAuthServerConfigurationMetadata(idPortenUrl)
}

private fun createJwkProvider(metadata: OauthServerConfigurationMetadata): JwkProvider = JwkProviderBuilder(URL(metadata.jwksUri))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
