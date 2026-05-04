package no.nav.tms.token.support.user.token.exchange.impl

import com.auth0.jwt.JWT
import com.nimbusds.jose.jwk.RSAKey
import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.user.token.exchange.UserTokenExchangeException
import no.nav.tms.token.support.user.token.exchange.UserTokenExchanger
import no.nav.tms.token.support.user.token.exchange.impl.ClientAssertion.createSignedAssertion

class CachingExchanger internal constructor(
    private val tokenExchangeConsumer: TokenExchangeConsumer,
    private val jwtAudience: String,
    private val clientId: String,
    privateJwk: String,
    maxCacheEntries: Long,
    cacheExpiryMarginSeconds: Int,
) : UserTokenExchanger {

    private val cache = CacheBuilder.buildCache(maxCacheEntries, cacheExpiryMarginSeconds)

    private val privateRsaKey = RSAKey.parse(privateJwk)

    override suspend fun exchangeToken(token: String, targetApp: String): String {
        try {
            val cacheKey = TokenStringUtil.createCacheKey(token, targetApp)

            return cache.get(cacheKey) {
                runBlocking {
                    performTokenExchange(token, targetApp)
                }
            }.accessToken
        } catch (e: Exception) {
            throw UserTokenExchangeException(e, clientId)
        }
    }

    private suspend fun performTokenExchange(token: String, targetApp: String): AccessTokenEntry {
        val jwt = createSignedAssertion(clientId, jwtAudience, privateRsaKey)

        val response = tokenExchangeConsumer.exchangeToken(token, jwt, targetApp)

        return AccessTokenEntry.fromResponse(response)
    }
}

internal object TokenStringUtil {
    fun createCacheKey(tokenString: String, targetApp: String): AccessTokenKey {
        val decodedToken = JWT.decode(tokenString)

        val subject = decodedToken.subject
        val securityLevel = decodedToken.getClaim("acr").asString()

        return AccessTokenKey(subject, securityLevel, targetApp)
    }
}
