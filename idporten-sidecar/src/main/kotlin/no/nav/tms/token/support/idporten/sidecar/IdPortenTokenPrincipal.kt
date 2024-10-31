package no.nav.tms.token.support.idporten.sidecar

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.auth.*

data class IdPortenTokenPrincipal(val accessToken: DecodedJWT) {
    fun ident(identClaim: String = "pid"): String = accessToken.getClaim(identClaim).asString()
}
