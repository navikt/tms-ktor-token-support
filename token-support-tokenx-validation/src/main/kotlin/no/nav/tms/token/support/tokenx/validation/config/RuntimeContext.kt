package no.nav.tms.token.support.tokenx.validation.config

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.tokenx.validation.config.JwkProviderBuilder.createJwkProvider
import no.nav.tms.token.support.tokenx.validation.tokendings.LevelOfAssuranceInternal
import no.nav.tms.token.support.tokenx.validation.tokendings.TokenVerifier

internal class RuntimeContext(
    minLevelOfAssurance: LevelOfAssuranceInternal
) {
    private val environment = Environment()

    private val httpClient = HttpClientBuilder.build()
    private val metadata = fetchMetadata(httpClient, environment.tokenxWellKnownUrl)

    private val jwkProvider = createJwkProvider(metadata)

    val verifierWrapper = TokenVerifier(
            jwkProvider = jwkProvider,
            clientId = environment.tokenxClientId,
            issuer = metadata.issuer,
            minLevelOfAssurance = minLevelOfAssurance
    )
}

private fun fetchMetadata(httpClient: HttpClient, idPortenUrl: String) = runBlocking {
    httpClient.getOAuthServerConfigurationMetadata(idPortenUrl)
}
