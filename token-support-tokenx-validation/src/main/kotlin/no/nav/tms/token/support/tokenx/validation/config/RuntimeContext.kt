package no.nav.tms.token.support.tokenx.validation.config

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.util.concurrent.TimeUnit

internal class RuntimeContext {
    val environment = Environment()

    private val httpClient = HttpClientBuilder.build()
    val metadata = fetchMetadata(httpClient, environment.tokenxWellKnownUrl)

    val jwkProvider = createJwkProvider(metadata)
}

private fun fetchMetadata(httpClient: HttpClient, idPortenUrl: String) = runBlocking {
    httpClient.getOAuthServerConfigurationMetadata(idPortenUrl)
}

private fun createJwkProvider(metadata: OauthServerConfigurationMetadata): JwkProvider = JwkProviderBuilder(URL(metadata.jwksUri))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()
