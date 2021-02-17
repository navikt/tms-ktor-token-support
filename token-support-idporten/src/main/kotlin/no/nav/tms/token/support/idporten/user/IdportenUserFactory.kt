package no.nav.tms.token.support.idporten.user

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.idporten.IdTokenPrincipal
import java.time.Instant

object IdportenUserFactory {

    private val IDENT_CLAIM = "pid"

    fun createNewAuthenticatedUser(call: ApplicationCall): IdportenUser {
        val principal = call.principal<IdTokenPrincipal>()
                ?: throw Exception("Principal har ikke blitt satt for authentication context.")

        return createNewAuthenticatedUser(principal)
    }

    private fun createNewAuthenticatedUser(principal: IdTokenPrincipal): IdportenUser {
        val token = principal.decodedJWT

        val ident: String = token.getClaim(IDENT_CLAIM).asString()
        val loginLevel = extractLoginLevel(token)
        val expirationTime =
            getTokenExpirationLocalDateTime(
                token
            )

        return IdportenUser(ident, loginLevel, token.token, expirationTime)
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
