package no.nav.tms.token.support.azure.validation.config

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.azure.validation.config.JwkProviderBuilder.createJwkProvider
import no.nav.tms.token.support.azure.validation.intercept.TokenVerifier

internal class RuntimeContext(enableDefaultProxy: Boolean) {
    private val environment = Environment()

    private val httpClient = HttpClientBuilder.build(enableDefaultProxy)
    private val metadata = fetchMetadata(httpClient, environment.azureWellKnownUrl)

    private val jwkProvider = createJwkProvider(metadata)

    val verifierWrapper = TokenVerifier(
            jwkProvider = jwkProvider,
            clientId = environment.azureClientId,
            issuer = metadata.issuer
    )
}

private fun fetchMetadata(httpClient: HttpClient, idPortenUrl: String) = runBlocking {
    httpClient.getOAuthServerConfigurationMetadata(idPortenUrl)
}
