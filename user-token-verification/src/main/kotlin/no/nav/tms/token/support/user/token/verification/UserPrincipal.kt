package no.nav.tms.token.support.user.token.verification

data class UserPrincipal(
    val ident: String,
    val levelOfAssurance: LevelOfAssurance,
    val accessToken: String
)
