package no.nav.tms.token.support.entraid.token.fetcher

import no.nav.tms.token.support.entraid.token.fetcher.impl.EntraIdEnvironment
import no.nav.tms.token.support.entraid.token.fetcher.impl.TokenIssuerConsumer
import no.nav.tms.token.support.entraid.token.fetcher.impl.CachingEntraIdTokenFetcher
import no.nav.tms.token.support.entraid.token.fetcher.impl.ClientAssertionService
import no.nav.tms.token.support.entraid.token.fetcher.impl.NonCachingEntraIdTokenFetcher

object EntraIdTokenFetcherBuilder {

    private val environment =
        _root_ide_package_.no.nav.tms.token.support.entraid.token.exchange.impl.EntraIdEnvironment()

    fun buildFetcher(
        cachingEnabled: Boolean = true,
        maxCachedEntries: Long = 1000L,
        cacheExpiryMarginSeconds: Int = 5,
        enableDefaultProxy: Boolean = false
    ): no.nav.tms.token.support.entraid.token.fetcher.EntraIdTokenFetcher {

        val httpClient = _root_ide_package_.no.nav.tms.token.support.entraid.token.fetcher.impl.HttpClientBuilder.buildHttpClient(enableDefaultProxy)

        val tokenIssuerConsumer =
            _root_ide_package_.no.nav.tms.token.support.entraid.token.exchange.impl.TokenIssuerConsumer(
                httpClient,
                environment.tenantId,
                environment.clientId,
                environment.openidTokenEndpoint
            )

        val clientAssertionService =
            _root_ide_package_.no.nav.tms.token.support.entraid.token.exchange.impl.ClientAssertionService(
                audience = environment.openidIssuer,
                clientId = environment.clientId,
                privateJwk = environment.privateJwk
            )

        if (cachingEnabled) {
            require(maxCachedEntries > 0) { "'maxCachedEntries' should be at least 1" }
            require(cacheExpiryMarginSeconds > 0) { "'cacheExpiryMarginSeconds' should be at least 1" }
        }

        return if (cachingEnabled) {
            CachingEntraIdTokenFetcher(
                tokenIssuerConsumer = tokenIssuerConsumer,
                clientAssertionService = clientAssertionService,
                maxCacheEntries = maxCachedEntries,
                cacheExpiryMarginSeconds = cacheExpiryMarginSeconds
            )
        } else {
            NonCachingEntraIdTokenFetcher(
                tokenIssuerConsumer = tokenIssuerConsumer,
                clientAssertionService = clientAssertionService
            )
        }
    }
}
