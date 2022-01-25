package no.nav.tms.token.support.idporten.wonderwall.authentication

import com.auth0.jwk.JwkProvider

// Allow easier mocking of TokenVerifier
internal object TokenVerifierBuilder {
    fun buildTokenVerifier(
        jwkProvider: JwkProvider,
        clientId: String,
        issuer: String,
        loginLevel: Int,
    ) = TokenVerifier(
        jwkProvider = jwkProvider,
        clientId = clientId,
        issuer = issuer,
        minLoginLevel = loginLevel
    )
}
