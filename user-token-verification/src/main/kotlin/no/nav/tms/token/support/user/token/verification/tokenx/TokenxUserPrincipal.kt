package no.nav.tms.token.support.user.token.verification.tokenx

import com.auth0.jwt.interfaces.DecodedJWT
import no.nav.tms.token.support.user.token.verification.UserPrincipal

data class TokenxUserPrincipal(

): UserPrincipal {
    companion object {
        fun fromJwt(decodedJWT: DecodedJWT): TokenxUserPrincipal {

        }
    }
}
