package no.nav.tms.token.support.azure.validation.mock.intercept

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.tms.token.support.azure.validation.AzurePrincipal
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.util.*

internal object AzurePrincipalBuilder {

    private val privateJwk = JwkBuilder.generateJwk()

    fun createPrincipal(authInfo: AuthInfo): AzurePrincipal {
        val decodedJWT = if (authInfo.azureJwt != null) {
            JWT.decode(authInfo.azureJwt)
        } else {
            stubJwt()
        }

        return AzurePrincipal(decodedJWT)
    }

    private fun stubJwt(): DecodedJWT {
        val jwtString =  JWTClaimsSet.Builder()
            .issueTime(Date.from(Instant.now()))
            .expirationTime(Date.from(Instant.now().plus(1, HOURS)))
            .jwtID("STUB")
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
