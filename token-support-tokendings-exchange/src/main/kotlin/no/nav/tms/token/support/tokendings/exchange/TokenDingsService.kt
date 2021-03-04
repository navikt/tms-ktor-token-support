package no.nav.tms.token.support.tokendings.exchange

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.tms.token.support.tokendings.exchange.consumer.TokenDingsConsumer
import java.time.Instant
import java.util.*

class TokenDingsService internal constructor(
        private val tokenDingsConsumer: TokenDingsConsumer,
        private val jwtAudience: String,
        private val clientId: String,
        privateJwk: String
) {

    private val privateRsaKey = RSAKey.parse(privateJwk)

    suspend fun exchangeToken(token: String, targetApp: String): String {
        val jwt = clientAssertion(clientId, jwtAudience, privateRsaKey)

        return tokenDingsConsumer.exchangeToken(token, jwt, targetApp).accessToken
    }

    private fun clientAssertion(clientId: String, audience: String, rsaKey: RSAKey): String {
        val now = Date.from(Instant.now())
        return JWTClaimsSet.Builder()
                .subject(clientId)
                .issuer(clientId)
                .audience(audience)
                .issueTime(now)
                .notBeforeTime(now)
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .jwtID(UUID.randomUUID().toString())
                .build()
                .sign(rsaKey)
                .serialize()
    }

    private fun JWTClaimsSet.sign(rsaKey: RSAKey): SignedJWT =
            SignedJWT(
                    JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(rsaKey.keyID)
                            .type(JOSEObjectType.JWT).build(),
                    this
            ).apply {
                sign(RSASSASigner(rsaKey.toPrivateKey()))
            }
}
