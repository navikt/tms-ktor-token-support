package no.nav.tms.token.support.tokenx.validation

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.auth.*

data class TokenXPrincipal(val decodedJWT: DecodedJWT) {
    fun ident(identClaim: String = "pid"): String = decodedJWT.getClaim(identClaim).asString()
}
