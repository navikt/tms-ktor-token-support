package no.nav.tms.token.support.user.token.exchange.impl

import com.nimbusds.jose.jwk.RSAKey
import no.nav.tms.token.support.user.token.exchange.UserTokenExchangeException
import no.nav.tms.token.support.user.token.exchange.UserTokenExchangeService
import no.nav.tms.token.support.user.token.exchange.impl.ClientAssertion.createSignedAssertion

class NonCachingExchangeService internal constructor(
    private val tokenExchangeConsumer: TokenExchangeConsumer,
    private val jwtAudience: String,
    private val clientId: String,
    privateJwk: String
) : UserTokenExchangeService {

    private val privateRsaKey = RSAKey.parse(privateJwk)

    override suspend fun exchangeToken(token: String, targetApp: String): String {
        try {
            val jwt = createSignedAssertion(clientId, jwtAudience, privateRsaKey)

            return tokenExchangeConsumer.exchangeToken(token, jwt, targetApp).accessToken
        } catch (e: Exception) {
            throw UserTokenExchangeException(e, clientId)
        }

    }
}
