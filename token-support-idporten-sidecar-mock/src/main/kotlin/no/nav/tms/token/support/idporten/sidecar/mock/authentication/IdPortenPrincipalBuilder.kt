package no.nav.tms.token.support.idporten.sidecar.mock.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.nimbusds.jwt.JWTClaimsSet
import no.nav.tms.token.support.idporten.sidecar.authentication.IdPortenTokenPrincipal
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.util.*

internal object IdPortenPrincipalBuilder {

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
            .claim("acr_values", securityLevel)
            .claim("pid", ident)
            .build()
            .toString()

        return JWT.decode(jwtString)
    }
}
