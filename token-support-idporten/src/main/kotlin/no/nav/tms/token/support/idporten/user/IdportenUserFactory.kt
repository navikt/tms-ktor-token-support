package no.nav.tms.token.support.idporten.user

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.idporten.authentication.IdTokenPrincipal
import java.time.Instant

// This creates an IdportenUser based on user jwt claims
object IdportenUserFactory {

    private val IDENT_CLAIM = "pid"

    fun createIdportenUser(call: ApplicationCall, identClaim: String = IDENT_CLAIM): IdportenUser {
        val principal = call.principal<IdTokenPrincipal>()
                ?: throw Exception("Principal har ikke blitt satt for authentication context.")

        return createIdportenUser(principal)
    }

    private fun createIdportenUser(principal: IdTokenPrincipal, identClaim: String): IdportenUser {
        val token = principal.decodedJWT

        val ident: String = token.getClaim(identClaim).asString()
        val loginLevel = extractLoginLevel(token)
        val expirationTime =
            getTokenExpirationLocalDateTime(
                token
            )

        return IdportenUser(ident, loginLevel, expirationTime, token)
    }

    private fun extractLoginLevel(token: DecodedJWT): Int {

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
