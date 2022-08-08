package no.nav.tms.token.support.idporten.sidecar.user

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.tms.token.support.idporten.sidecar.authentication.IdPortenTokenPrincipal
import java.time.Instant

// This creates an IdportenUser based on user jwt claims
object IdportenUserFactory {

    private val IDENT_CLAIM = "pid"

    fun createIdportenUser(call: ApplicationCall, identClaim: String = IDENT_CLAIM): IdportenUser {
        val principal = call.principal<IdPortenTokenPrincipal>()
                ?: throw Exception("Principal har ikke blitt satt for authentication context.")

        return createIdportenUser(principal, identClaim)
    }

    private fun createIdportenUser(principal: IdPortenTokenPrincipal, identClaim: String): IdportenUser {
        val accessToken = principal.accessToken

        val ident: String = accessToken.getClaim(identClaim).asString()
        val loginLevel = extractLoginLevel(accessToken)
        val expirationTime =
            getTokenExpirationLocalDateTime(
                accessToken
            )

        return IdportenUser(ident, loginLevel, expirationTime, accessToken)
    }

    internal fun extractLoginLevel(token: DecodedJWT): Int {

        return when (token.getClaim("acr").asString()) {
            "Level3" -> 3
            "Level4" -> 4
            else -> throw Exception("Innloggingsnivå ble ikke funnet. Dette skal ikke kunne skje.")
        }
    }

    private fun getTokenExpirationLocalDateTime(token: DecodedJWT): Instant {
        return token.expiresAt.toInstant()
    }

}
