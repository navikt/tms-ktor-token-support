package no.nav.tms.token.support.tokendings.exchange.service

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.util.*

internal object ClientAssertion {
    fun createSignedAssertion(clientId: String, audience: String, rsaKey: RSAKey): String {
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
