package no.nav.tms.token.support.idporten.sidecar.user

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance.*
import no.nav.tms.token.support.idporten.sidecar.IdPortenTokenPrincipal
import no.nav.tms.token.support.idporten.sidecar.install.IdPortenLevelOfAssurance
import no.nav.tms.token.support.idporten.sidecar.install.IdPortenLevelOfAssurance.*
import java.time.Instant

// This creates an IdportenUser based on user jwt claims
object IdportenUserFactory {

    private val IDENT_CLAIM = "pid"

    fun createIdportenUser(call: ApplicationCall, identClaim: String = IDENT_CLAIM): IdportenUser {
        val principal = call.principal<IdPortenTokenPrincipal>()
                ?: throw Exception("Principal har ikke blitt satt for authentication context.")

        return createIdportenUser(principal, identClaim)
    }

    internal fun extractLevelOfAssurance(accessToken: DecodedJWT): IdPortenLevelOfAssurance {
        return IdPortenLevelOfAssurance.fromAcr(accessToken.getClaim("acr").asString())
    }

    private fun createIdportenUser(principal: IdPortenTokenPrincipal, identClaim: String): IdportenUser {
        val accessToken = principal.accessToken

        val ident: String = accessToken.getClaim(identClaim).asString()


        val acrLoA = extractLevelOfAssurance(accessToken)
        val loginLevel = mapLoginLevel(acrLoA)
        val levelOfAssurance = mapLevelOfAssurance(acrLoA)

        val expirationTime =
            getTokenExpirationLocalDateTime(
                accessToken
            )

        return IdportenUser(ident, loginLevel, levelOfAssurance, expirationTime, accessToken)
    }

    private fun mapLoginLevel(levelOfAssurance: IdPortenLevelOfAssurance): Int {

        return when (levelOfAssurance) {
            Level3, Substantial -> 3
            Level4, High -> 4
            Low -> throw RuntimeException("Level of assurance 'low' er ikke støttet.")
        }
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
