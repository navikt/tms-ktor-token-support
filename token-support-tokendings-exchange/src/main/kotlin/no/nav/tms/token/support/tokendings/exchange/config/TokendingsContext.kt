package no.nav.tms.token.support.tokendings.exchange.config

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.tokendings.exchange.TargetAppNameBuilder
import no.nav.tms.token.support.tokendings.exchange.consumer.TokendingsConsumer
import no.nav.tms.token.support.tokendings.exchange.TokendingsService


internal class TokendingsContext {
    private val httpClient = HttpClientBuilder.buildHttpClient()

    private val environment = Environment()

    private val metadata = fetchMetadata(httpClient, environment.tokenxWellKnownUrl)

    private val tokendingsConsumer = TokendingsConsumer(httpClient, metadata.tokenEndpoint)

    internal val tokendingsService = TokendingsService(
            tokendingsConsumer,
            metadata.tokenEndpoint,
            environment.tokenxClientId,
            environment.tokenxClientJwk
    )

    internal val targetAppNameBuilder = TargetAppNameBuilder(environment.cluster, environment.namespace)
}

private fun fetchMetadata(httpClient: HttpClient, tokendingsUrl: String) = runBlocking {
    httpClient.getTokendingsConfigurationMetadata(tokendingsUrl)
}
