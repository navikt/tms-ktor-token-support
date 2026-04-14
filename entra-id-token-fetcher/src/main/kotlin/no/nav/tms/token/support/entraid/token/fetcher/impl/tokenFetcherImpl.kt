package no.nav.tms.token.support.entraid.token.fetcher.impl

import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.entraid.token.fetcher.EntraIdTokenFetcher

class NonCachingEntraIdTokenFetcher internal constructor(
    private val tokenIssuerConsumer: TokenIssuerConsumer,
    private val clientAssertionService: ClientAssertionService
) : EntraIdTokenFetcher {

    override suspend fun getAccessToken(targetApp: String): String = try {
        val jwt = clientAssertionService.createSignedAssertion()

        tokenIssuerConsumer.fetchToken(jwt, targetApp).accessToken
    } catch (e: Exception) {
        throw EntraIdTokenException(targetApp, e)
    }
}

class CachingEntraIdTokenFetcher internal constructor(
    private val tokenIssuerConsumer: TokenIssuerConsumer,
    private val clientAssertionService: ClientAssertionService,
    maxCacheEntries: Long,
    cacheExpiryMarginSeconds: Int,
) : EntraIdTokenFetcher {

    private val cache = CacheBuilder.buildCache(maxCacheEntries, cacheExpiryMarginSeconds)

    override suspend fun getAccessToken(targetApp: String): String =
        try {
            cache.get(targetApp) {
                runBlocking {
                    requestToken(targetApp)
                }
            }.accessToken
        } catch (e: Exception) {
            throw EntraIdTokenException(targetApp, e)
        }


    private suspend fun requestToken(targetApp: String): AccessTokenEntry {
        val jwt = clientAssertionService.createSignedAssertion()

        val response = tokenIssuerConsumer.fetchToken(jwt, targetApp)

        return AccessTokenEntry.fromResponse(response)
    }
}

class EntraIdTokenException(target: String, cause: Exception) :
    RuntimeException("Klarte ikke hente token for app [$target]", cause)
