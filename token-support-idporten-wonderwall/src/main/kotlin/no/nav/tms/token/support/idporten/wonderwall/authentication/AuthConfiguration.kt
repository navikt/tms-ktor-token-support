package no.nav.tms.token.support.idporten.wonderwall.authentication

import com.auth0.jwk.JwkProvider

internal class AuthConfiguration(
        jwkProvider: JwkProvider,
        clientId: String,
        issuer: String,
        loginLevel: Int,
        val fallbackTokenCookieEnabled: Boolean,
        val fallbackTokenCookieName: String
) {
        val tokenVerifier = TokenVerifier(
                jwkProvider = jwkProvider,
                clientId = clientId,
                issuer = issuer,
                minLoginLevel = loginLevel
        )
}
