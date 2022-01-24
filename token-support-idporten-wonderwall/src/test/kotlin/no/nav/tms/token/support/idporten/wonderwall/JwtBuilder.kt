package no.nav.tms.token.support.idporten.wonderwall

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.*

object JwtBuilder {

    fun generateJwtString(issueTime: Date, expiryTime: Date, rsaKey: RSAKey): String {
        return JWTClaimsSet.Builder()
                .issueTime(issueTime)
                .expirationTime(expiryTime)
                .jwtID(UUID.randomUUID().toString())
                .build()
                .sign(rsaKey)
                .serialize()
    }
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
