package no.nav.tms.token.support.azure.exchange.config

import no.nav.tms.token.support.azure.exchange.consumer.AzureConsumer
import no.nav.tms.token.support.azure.exchange.service.NonCachingAzureService

internal class AzureContext {

    val httpClient = HttpClientBuilder.buildHttpClient()

    val environment = Environment()

    val azureConsumer = AzureConsumer(
            httpClient,
            environment.azureTenantId,
            environment.azureClientId,
            environment.azureOpenidTokenEndpoint
    )

    val azureService = NonCachingAzureService(
            azureConsumer,
            environment.azureOpenidIssuer,
            environment.azureClientId,
            environment.azureJwk
    )

}
