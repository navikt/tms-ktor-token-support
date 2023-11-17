package no.nav.tms.token.support.azure.exchange.service

import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.azure.exchange.AzureService
import no.nav.tms.token.support.azure.exchange.config.cache.AccessTokenEntry
import no.nav.tms.token.support.azure.exchange.config.cache.CacheBuilder
import no.nav.tms.token.support.azure.exchange.consumer.AzureConsumer


class NonCachingAzureService internal constructor(
        private val azureConsumer: AzureConsumer,
        issuer: String,
        clientId: String,
        privateJwk: String
): AzureService {

    private val clientAssertionService = ClientAssertionService(privateJwk, clientId, issuer)

    override suspend fun getAccessToken(targetApp: String): String {
        val jwt = clientAssertionService.createClientAssertion()

        return azureConsumer.fetchToken(jwt, targetApp).accessToken
    }
}

class CachingAzureService internal constructor(
        private val azureConsumer: AzureConsumer,
        issuer: String,
        clientId: String,
        privateJwk: String,
        maxCacheEntries: Long,
        cacheExpiryMarginSeconds: Int,
): AzureService {

    private val cache = CacheBuilder.buildCache(maxCacheEntries, cacheExpiryMarginSeconds)

    private val clientAssertionService = ClientAssertionService(privateJwk, clientId, issuer)


    override suspend fun getAccessToken(targetApp: String): String {

        return cache.get(targetApp) {
            runBlocking {
                performTokenExchange(targetApp)
            }
        }.accessToken
    }

    private suspend fun performTokenExchange(targetApp: String): AccessTokenEntry {
        val jwt = clientAssertionService.createClientAssertion()

        val response = azureConsumer.fetchToken(jwt, targetApp)

        return AccessTokenEntry.fromResponse(response)
    }
}
