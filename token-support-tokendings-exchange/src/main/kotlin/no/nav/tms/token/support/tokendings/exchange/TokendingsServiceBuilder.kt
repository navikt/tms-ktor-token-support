package no.nav.tms.token.support.tokendings.exchange

import no.nav.tms.token.support.tokendings.exchange.config.TokendingsContext
import no.nav.tms.token.support.tokendings.exchange.service.CachingTokendingsService
import no.nav.tms.token.support.tokendings.exchange.service.NonCachingTokendingsService

object TokendingsServiceBuilder {

    private val context = TokendingsContext()

    fun buildTokendingsService(
            cachingEnabled: Boolean = true,
            maxCachedEntries: Long = 1000L,
            cacheExpiryMarginSeconds: Int = 5
    ): TokendingsService {

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
            = NonCachingTokendingsService(
                    context.tokendingsConsumer,
                    context.metadata.tokenEndpoint,
                    context.environment.tokenxClientId,
                    context.environment.tokenxClientJwk
            )

    private fun createCachingService(maxCachedEntries: Long, cacheExpiryMarginSeconds: Int)
            = CachingTokendingsService(
                    context.tokendingsConsumer,
                    context.metadata.tokenEndpoint,
                    context.environment.tokenxClientId,
                    context.environment.tokenxClientJwk,
                    maxCachedEntries,
                    cacheExpiryMarginSeconds
            )

}
