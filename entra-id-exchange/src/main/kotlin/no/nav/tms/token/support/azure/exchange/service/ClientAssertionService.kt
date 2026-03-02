package no.nav.tms.token.support.azure.exchange.service

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.util.*

class ClientAssertionService(privateJwk: String, private val clientId: String, private val issuer: String) {

    private val privateRsaKey = RSAKey.parse(privateJwk)

    fun createClientAssertion(): String {
        val now = Date.from(Instant.now())
        return JWTClaimsSet.Builder()
                .issuer(clientId)
                .subject(clientId)
                .audience(issuer)
                .issueTime(now)
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .jwtID(UUID.randomUUID().toString())
                .build()
                .sign(privateRsaKey)
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
