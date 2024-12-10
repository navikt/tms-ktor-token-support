package no.nav.tms.token.support.tokenx.validation.user

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance.HIGH
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance.SUBSTANTIAL
import no.nav.tms.token.support.tokenx.validation.TokenXPrincipal
import no.nav.tms.token.support.tokenx.validation.install.IdPortenLevelOfAssurance
import no.nav.tms.token.support.tokenx.validation.install.IdPortenLevelOfAssurance.*
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

        val acrLoA = IdPortenLevelOfAssurance.fromAcr(token.getClaim("acr").asString())

        val levelOfAssurance = mapLevelOfAssurance(acrLoA)

        val expirationTime = getTokenExpirationLocalDateTime(token)

        return TokenXUser(ident, levelOfAssurance, expirationTime, token)
    }

    private fun mapLevelOfAssurance(levelOfAssurance: IdPortenLevelOfAssurance): LevelOfAssurance {
        return when (levelOfAssurance) {
            Level3, Substantial -> SUBSTANTIAL
            Level4, High -> HIGH
            Low -> throw RuntimeException("Level of assurance 'low' er ikke støttet.")
        }
    }

    private fun getTokenExpirationLocalDateTime(token: DecodedJWT): Instant {
        return token.expiresAt.toInstant()
    }

}
