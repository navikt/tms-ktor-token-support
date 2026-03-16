package no.nav.tms.token.support.user.token.verification.idporten

import com.auth0.jwt.interfaces.DecodedJWT
import no.nav.tms.token.support.user.token.verification.LevelOfAssurance
import no.nav.tms.token.support.user.token.verification.UserPrincipal

data class IdPortenUserPrincipal(
    override val accessToken: DecodedJWT,
    override val ident: String,
    override val levelOfAssurance: LevelOfAssurance
): UserPrincipal {
    companion object {
        fun fromJwt(jwt: DecodedJWT): IdPortenUserPrincipal {
            return IdPortenUserPrincipal(
                jwt,
                jwt.getClaim("pid").asString(),
                levelOfAssurance = IdPortenLevelOfAssurance
                    .fromAcr(jwt.getClaim("acr").asString())
                    .toLevelOfAssurance()
            )
        }
    }
}
