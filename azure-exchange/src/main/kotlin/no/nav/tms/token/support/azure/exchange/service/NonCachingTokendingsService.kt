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
) : AzureService {

    private val clientAssertionService = ClientAssertionService(privateJwk, clientId, issuer)

    override suspend fun getAccessToken(targetApp: String): String = try {
        val jwt = clientAssertionService.createClientAssertion()

        azureConsumer.fetchToken(jwt, targetApp).accessToken
    } catch (throwable: Throwable) {
        throw AzureExchangeException(throwable, targetApp)
    }


}

class CachingAzureService internal constructor(
    private val azureConsumer: AzureConsumer,
    issuer: String,
    clientId: String,
    privateJwk: String,
    maxCacheEntries: Long,
    cacheExpiryMarginSeconds: Int,
) : AzureService {

    private val cache = CacheBuilder.buildCache(maxCacheEntries, cacheExpiryMarginSeconds)

    private val clientAssertionService = ClientAssertionService(privateJwk, clientId, issuer)


    override suspend fun getAccessToken(targetApp: String): String =
        try {
            cache.get(targetApp) {
                runBlocking {
                    performTokenExchange(targetApp)
                }
            }.accessToken
        } catch (throwable: Throwable) {
            throw AzureExchangeException(throwable, targetApp)
        }


    private suspend fun performTokenExchange(targetApp: String): AccessTokenEntry {
        val jwt = clientAssertionService.createClientAssertion()

        val response = azureConsumer.fetchToken(jwt, targetApp)

        return AccessTokenEntry.fromResponse(response)
    }
}

class AzureExchangeException(val originalThrowable: Throwable, targetApp: String) :
    Exception() {

    val stackTraceSummary =
        originalThrowable.stackTrace.firstOrNull()?.let { stacktraceElement ->
            """    Tokendingsexchange feiler for $targetApp 
                   Origin: ${stacktraceElement.fileName ?: "---"} ${stacktraceElement.methodName ?: "----"} linenumber:${stacktraceElement.lineNumber}
                   Message: "${originalThrowable::class.simpleName} ${originalThrowable.message?.let { ":$it" }}"
                """.trimIndent()
        } ?: "${originalThrowable::class.simpleName} ${originalThrowable.message?.let { ":$it" }}"
}

