package no.nav.tms.token.support.user.token.verification

import com.auth0.jwt.interfaces.DecodedJWT

interface UserPrincipal {
    val ident: String
    val levelOfAssurance: LevelOfAssurance
    val accessToken: DecodedJWT
}
