package no.nav.tms.token.support.idporten.authentication

import com.auth0.jwk.JwkProvider

internal class AuthConfiguration(
        val contextPath: String,
        val accessTokenCookieName: String,
        val idTokenCookieName: String,
        val refreshTokenCookieName: String,
        val jwkProvider: JwkProvider,
        val clientId: String,
        val issuer: String,
        val shouldRedirect: Boolean
)
