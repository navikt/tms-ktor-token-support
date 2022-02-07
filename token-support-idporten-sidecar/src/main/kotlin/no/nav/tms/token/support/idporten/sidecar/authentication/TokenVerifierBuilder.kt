package no.nav.tms.token.support.idporten.sidecar.authentication

import com.auth0.jwk.JwkProvider

// Allow easier mocking of TokenVerifier
internal object TokenVerifierBuilder {
    fun buildTokenVerifier(
        jwkProvider: JwkProvider,
        issuer: String,
        loginLevel: Int,
    ) = TokenVerifier(
        jwkProvider = jwkProvider,
        issuer = issuer,
        minLoginLevel = loginLevel
    )
}
