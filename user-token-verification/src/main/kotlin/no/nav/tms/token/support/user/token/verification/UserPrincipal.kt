package no.nav.tms.token.support.user.token.verification

import com.auth0.jwt.interfaces.DecodedJWT

data class UserPrincipal(
    val ident: String,
    val levelOfAssurance: LevelOfAssurance,
    val accessToken: DecodedJWT
)
