package no.nav.tms.token.support.azure.exchange

import no.nav.tms.token.support.azure.exchange.config.AzureContext
import no.nav.tms.token.support.azure.exchange.service.CachingAzureService
import no.nav.tms.token.support.azure.exchange.service.NonCachingAzureService

object AzureServiceBuilder {

    fun buildAzureService(
            cachingEnabled: Boolean = true,
            maxCachedEntries: Long = 1000L,
            cacheExpiryMarginSeconds: Int = 5,
            enableDefaultProxy: Boolean = false
    ): AzureService {

        val context = AzureContext(enableDefaultProxy)

        if (cachingEnabled) {
            require(maxCachedEntries > 0) { "'maxCachedEntries' should be at least 1" }
            require(cacheExpiryMarginSeconds > 0) { "'cacheExpiryMarginSeconds' should be at least 1" }
        }

        return if (cachingEnabled) {
            createCachingService(context, maxCachedEntries, cacheExpiryMarginSeconds)
        } else {
            createNonCachingService(context)
        }
    }

    private fun createNonCachingService(context: AzureContext)
            = NonCachingAzureService(
                    azureConsumer = context.azureConsumer,
                    issuer = context.environment.azureOpenidIssuer,
                    clientId = context.environment.azureClientId,
                    privateJwk = context.environment.azureJwk
            )

    private fun createCachingService(context: AzureContext, maxCachedEntries: Long, cacheExpiryMarginSeconds: Int)
            = CachingAzureService(
                    azureConsumer = context.azureConsumer,
                    issuer = context.environment.azureOpenidIssuer,
                    clientId = context.environment.azureClientId,
                    privateJwk = context.environment.azureJwk,
                    maxCacheEntries = maxCachedEntries,
                    cacheExpiryMarginSeconds = cacheExpiryMarginSeconds
            )

}
