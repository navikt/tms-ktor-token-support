package no.nav.tms.token.support.azure.validation.mock

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.tms.token.support.azure.validation.mock.intercept.JwkBuilder
import java.time.Instant
import java.util.*

object JwtBuilder {
    private val privateJwk = JwkBuilder.generateJwk()

    fun generateJwt(): DecodedJWT {
        val now = Date.from(Instant.now())
        val jwtString = JWTClaimsSet.Builder()
                .issueTime(now)
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .jwtID(UUID.randomUUID().toString())
                .build()
                .sign()
                .serialize()

        return JWT.decode(jwtString)
    }

    private fun JWTClaimsSet.sign(): SignedJWT =
        SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(privateJwk.keyID)
                .type(JOSEObjectType.JWT).build(),
            this
        ).apply {
            sign(RSASSASigner(privateJwk.toPrivateKey()))
        }
}


