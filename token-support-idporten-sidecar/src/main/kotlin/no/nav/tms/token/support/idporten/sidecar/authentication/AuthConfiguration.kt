package no.nav.tms.token.support.idporten.sidecar.authentication

import com.auth0.jwk.JwkProvider

internal class AuthConfiguration(
        jwkProvider: JwkProvider,
        issuer: String,
        loginLevel: Int,
        val fallbackTokenCookieEnabled: Boolean,
        val fallbackTokenCookieName: String
) {
        val tokenVerifier = TokenVerifierBuilder.buildTokenVerifier(
                jwkProvider = jwkProvider,
                issuer = issuer,
                loginLevel = loginLevel
        )
}
