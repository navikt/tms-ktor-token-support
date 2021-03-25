package no.nav.tms.token.support.tokenx.validation.config

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.tokenx.validation.config.JwkProviderBuilder.createJwkProvider
import no.nav.tms.token.support.tokenx.validation.tokendings.VerifierWrapper

internal class RuntimeContext {
    private val environment = Environment()

    private val httpClient = HttpClientBuilder.build()
    private val metadata = fetchMetadata(httpClient, environment.tokenxWellKnownUrl)

    private val jwkProvider = createJwkProvider(metadata)

    val verifierWrapper = VerifierWrapper(
            jwkProvider = jwkProvider,
            clientId = environment.tokenxClientId,
            issuer = metadata.issuer
    )
}

private fun fetchMetadata(httpClient: HttpClient, idPortenUrl: String) = runBlocking {
    httpClient.getOAuthServerConfigurationMetadata(idPortenUrl)
}
