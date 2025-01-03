package no.nav.tms.token.support.idporten.sidecar.user

import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.application.*
import io.ktor.server.auth.*
import no.nav.tms.token.support.idporten.sidecar.IdPortenTokenPrincipal
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance.HIGH
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance.SUBSTANTIAL
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
        val levelOfAssurance = mapLevelOfAssurance(acrLoA)

        val expirationTime =
            getTokenExpirationLocalDateTime(
                accessToken
            )

        return IdportenUser(ident, levelOfAssurance, expirationTime, accessToken)
    }

    private fun mapLevelOfAssurance(levelOfAssurance: IdPortenLevelOfAssurance): LevelOfAssurance {
        return when (levelOfAssurance) {
            Substantial -> SUBSTANTIAL
            High -> HIGH
            Low -> throw RuntimeException("Level of assurance 'low' er ikke støttet.")
        }
    }

    private fun getTokenExpirationLocalDateTime(token: DecodedJWT): Instant {
        return token.expiresAt.toInstant()
    }

}
