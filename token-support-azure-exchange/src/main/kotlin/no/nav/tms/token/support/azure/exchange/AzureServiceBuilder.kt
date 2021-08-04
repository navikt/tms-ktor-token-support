package no.nav.tms.token.support.azure.exchange

import no.nav.tms.token.support.azure.exchange.config.AzureContext
import no.nav.tms.token.support.azure.exchange.service.CachingAzureService
import no.nav.tms.token.support.azure.exchange.service.NonCachingAzureService

object AzureServiceBuilder {

    private val context = AzureContext()

    fun buildAzureService(
            cachingEnabled: Boolean = true,
            maxCachedEntries: Long = 1000L,
            cacheExpiryMarginSeconds: Int = 5
    ): AzureService {

        if (cachingEnabled) {
            require(maxCachedEntries > 0) { "'maxCachedEntries' should be at least 1" }
            require(cacheExpiryMarginSeconds > 0) { "'cacheExpiryMarginSeconds' should be at least 1" }
        }

        return if (cachingEnabled) {
            createCachingService(maxCachedEntries, cacheExpiryMarginSeconds)
        } else {
            createNonCachingService()
        }
    }

    private fun createNonCachingService()
            = NonCachingAzureService(
                    azureConsumer = context.azureConsumer,
                    issuer = context.environment.azureOpenidIssuer,
                    clientId = context.environment.azureClientId,
                    privateJwk = context.environment.azureJwk
            )

    private fun createCachingService(maxCachedEntries: Long, cacheExpiryMarginSeconds: Int)
            = CachingAzureService(
                    azureConsumer = context.azureConsumer,
                    issuer = context.environment.azureOpenidIssuer,
                    clientId = context.environment.azureClientId,
                    privateJwk = context.environment.azureJwk,
                    maxCacheEntries = maxCachedEntries,
                    cacheExpiryMarginSeconds = cacheExpiryMarginSeconds
            )

}
