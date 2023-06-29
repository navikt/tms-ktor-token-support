package no.nav.tms.token.support.tokenx.validation.user

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance.HIGH
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance.SUBSTANTIAL
import no.nav.tms.token.support.tokenx.validation.tokendings.LevelOfAssuranceInternal
import no.nav.tms.token.support.tokenx.validation.tokendings.LevelOfAssuranceInternal.*
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

        val acrLoA = LevelOfAssuranceInternal.fromAcr(token.getClaim("acr").asString())

        val loginLevel = mapLoginLevel(acrLoA)
        val levelOfAssurance = mapLevelOfAssurance(acrLoA)

        val expirationTime =
            getTokenExpirationLocalDateTime(
                token
            )

        return TokenXUser(ident, loginLevel, levelOfAssurance, expirationTime, token)
    }

    private fun mapLoginLevel(levelOfAssurance: LevelOfAssuranceInternal): Int {

        return when (levelOfAssurance) {
            Level3, Substantial -> 3
            Level4, High -> 4
            Low -> throw RuntimeException("Level of assurance 'low' er ikke støttet.")
        }
    }

    private fun mapLevelOfAssurance(levelOfAssurance: LevelOfAssuranceInternal): LevelOfAssurance {
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
