package no.nav.tms.token.support.tokendings.exchange

import no.nav.tms.token.support.tokendings.exchange.config.TokendingsContext
import no.nav.tms.token.support.tokendings.exchange.service.CachingTokendingsService
import no.nav.tms.token.support.tokendings.exchange.service.NonCachingTokendingsService

object TokendingsServiceBuilder {

    private val context = TokendingsContext()

    fun buildTokendingsService(configure: TokendingsServiceConfig.() -> Unit): TokendingsService {
        val config = TokendingsServiceConfig()
                .also(configure)
                .also(validateConfig)

        return if (config.cachingEnabled) {
            createCachingService(config)
        } else {
            createNonCachingService()
        }
    }

    private fun createNonCachingService() = NonCachingTokendingsService(
            context.tokendingsConsumer,
            context.metadata.tokenEndpoint,
            context.environment.tokenxClientId,
            context.environment.tokenxClientJwk
    )

    private fun createCachingService(config: TokendingsServiceConfig) = CachingTokendingsService(
            context.tokendingsConsumer,
            context.metadata.tokenEndpoint,
            context.environment.tokenxClientId,
            context.environment.tokenxClientJwk,
            config.maxCachedEntries,
            config.cacheExpiryMarginSeconds
    )

    private val validateConfig: TokendingsServiceConfig.() -> Unit = {
        if (cachingEnabled) {
            require(maxCachedEntries > 0) { "'maxCachedEntries' should be at least 1" }
            require(cacheExpiryMarginSeconds > 0) { "'cacheExpiryMarginSeconds' should be at least 1" }
        }
    }
}
