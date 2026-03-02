package no.nav.tms.token.support.azure.exchange.config

import no.nav.tms.token.support.azure.exchange.consumer.AzureConsumer

internal class AzureContext(enableDefaultProxy: Boolean) {

    val httpClient = HttpClientBuilder.buildHttpClient(enableDefaultProxy)

    val environment = Environment()

    val azureConsumer = AzureConsumer(
            httpClient,
            environment.azureTenantId,
            environment.azureClientId,
            environment.azureOpenidTokenEndpoint
    )
}
