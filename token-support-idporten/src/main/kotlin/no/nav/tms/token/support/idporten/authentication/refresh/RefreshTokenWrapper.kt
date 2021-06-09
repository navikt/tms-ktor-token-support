package no.nav.tms.token.support.idporten.authentication.refresh

data class RefreshTokenWrapper(
        val accessToken: String,
        val refreshToken: String
)
