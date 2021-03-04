package no.nav.tms.token.support.tokendings.exchange.config

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.tokendings.exchange.TargetAppNameBuilder
import no.nav.tms.token.support.tokendings.exchange.consumer.TokenDingsConsumer
import no.nav.tms.token.support.tokendings.exchange.TokenDingsService


internal class TokenDingsContext {
    private val httpClient = HttpClientBuilder.buildHttpClient()

    private val cluster = getTokenxEnvVar("NAIS_CLUSTER_NAME")
    private val namespace = getTokenxEnvVar("NAIS_NAMESPACE")

    private val tokenxWellKnownUrl: String = getTokenxEnvVar("TOKEN_X_WELL_KNOWN_URL")
    private val tokenxClientId: String = getTokenxEnvVar("TOKEN_X_CLIENT_ID")
    private val tokenxClientJwk: String = getTokenxEnvVar("TOKEN_X_PRIVATE_JWK")

    private val metadata = fetchMetadata(httpClient, tokenxWellKnownUrl)

    private val tokenDingsConsumer = TokenDingsConsumer(httpClient, metadata.tokenEndpoint)
    internal val tokenDingsService = TokenDingsService(tokenDingsConsumer, metadata.tokenEndpoint, tokenxClientId, tokenxClientJwk)
    internal val targetAppNameBuilder = TargetAppNameBuilder(cluster, namespace)
}

private fun fetchMetadata(httpClient: HttpClient, tokenDingsUrl: String) = runBlocking {
    httpClient.getTokenDingsConfigurationMetadata(tokenDingsUrl)
}

private fun getTokenxEnvVar(varName: String): String {
    return System.getenv(varName)
            ?: throw IllegalArgumentException("Fant ikke $varName for tokenx. Påse at nais.yaml er konfigurert riktig.")
}
