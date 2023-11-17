package no.nav.tms.token.support.tokendings.exchange.config

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.tokendings.exchange.consumer.TokendingsConsumer


internal class TokendingsContext {
    private val httpClient = HttpClientBuilder.buildHttpClient()

    internal val environment = Environment()

    internal val metadata = fetchMetadata(httpClient, environment.tokenxWellKnownUrl)

    internal val tokendingsConsumer = TokendingsConsumer(httpClient, metadata.tokenEndpoint)
}

private fun fetchMetadata(httpClient: HttpClient, tokendingsUrl: String) = runBlocking {
    httpClient.getTokendingsConfigurationMetadata(tokendingsUrl)
}
