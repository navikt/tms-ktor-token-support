package no.nav.tms.token.support.idporten.sidecar.mock.install

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.tms.token.support.idporten.sidecar.IdPortenTokenPrincipal
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.util.*

internal object IdPortenPrincipalBuilder {

    private val privateJwk = JwkBuilder.generateJwk()

    fun createPrincipal(authInfo: AuthInfo): IdPortenTokenPrincipal {
        val decodedJWT = if (authInfo.jwtOverride != null) {
            JWT.decode(authInfo.jwtOverride)
        } else {
            buildJwt(authInfo.securityLevel!!, authInfo.ident!!)
        }

        return IdPortenTokenPrincipal(decodedJWT)
    }

    private fun buildJwt(securityLevel: String, ident: String): DecodedJWT {
        val jwtString =  JWTClaimsSet.Builder()
            .issueTime(Date.from(Instant.now()))
            .expirationTime(Date.from(Instant.now().plus(1, HOURS)))
            .jwtID("STUB")
            .claim("acr", securityLevel)
            .claim("pid", ident)
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
