package no.nav.tms.token.support.tokendings.exchange.service

import com.auth0.jwt.JWT
import com.nimbusds.jose.jwk.RSAKey
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.tokendings.exchange.TokendingsService
import no.nav.tms.token.support.tokendings.exchange.config.cache.AccessTokenEntry
import no.nav.tms.token.support.tokendings.exchange.config.cache.CacheBuilder
import no.nav.tms.token.support.tokendings.exchange.consumer.TokendingsConsumer
import no.nav.tms.token.support.tokendings.exchange.service.ClientAssertion.createSignedAssertion

class NonCachingTokendingsService internal constructor(
        private val tokendingsConsumer: TokendingsConsumer,
        private val jwtAudience: String,
        private val clientId: String,
        privateJwk: String
): TokendingsService {

    private val privateRsaKey = RSAKey.parse(privateJwk)

    override suspend fun exchangeToken(token: String, targetApp: String): String {
        val jwt = createSignedAssertion(clientId, jwtAudience, privateRsaKey)

        return tokendingsConsumer.exchangeToken(token, jwt, targetApp).accessToken
    }
}

class CachingTokendingsService internal constructor(
        private val tokendingsConsumer: TokendingsConsumer,
        private val jwtAudience: String,
        private val clientId: String,
        privateJwk: String,
        maxCacheEntries: Long,
        cacheExpiryMarginSeconds: Int,
): TokendingsService {

    private val cache = CacheBuilder.buildCache(maxCacheEntries, cacheExpiryMarginSeconds)

    private val privateRsaKey = RSAKey.parse(privateJwk)

    override suspend fun exchangeToken(token: String, targetApp: String): String {
        val subject = TokenStringUtil.extractSubject(token)

        return cache.get(subject) {
            runBlocking {
                performTokenExchange(token, targetApp)
            }
        }.accessToken
    }

    private suspend fun performTokenExchange(token: String, targetApp: String): AccessTokenEntry {
        val jwt = createSignedAssertion(clientId, jwtAudience, privateRsaKey)

        val response = tokendingsConsumer.exchangeToken(token, jwt, targetApp)

        return AccessTokenEntry.fromResponse(response)
    }
}

internal object TokenStringUtil {
    fun extractSubject(tokenString: String): String {
        return JWT.decode(tokenString).subject
    }
}
