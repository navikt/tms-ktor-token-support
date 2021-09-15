package no.nav.tms.token.support.tokenx.validation.user

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.*
import io.ktor.auth.*
import no.nav.tms.token.support.tokenx.validation.tokendings.TokenXPrincipal
import java.time.Instant

object TokenXUserFactory {

    private val IDENT_CLAIM = "pid"

    fun createTokenXUser(call: ApplicationCall, identClaim: String = IDENT_CLAIM): TokenXUser {
        val principal = call.principal<TokenXPrincipal>()
                ?: throw Exception("Principal har ikke blitt satt for authentication context.")

        return createIdportenUser(principal, identClaim)
    }

    private fun createIdportenUser(principal: TokenXPrincipal, identClaim: String): TokenXUser {
        val token = principal.decodedJWT

        val ident: String = token.getClaim(identClaim).asString()
        val loginLevel = extractLoginLevel(token)
        val expirationTime =
            getTokenExpirationLocalDateTime(
                token
            )

        return TokenXUser(ident, loginLevel, expirationTime, token)
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
