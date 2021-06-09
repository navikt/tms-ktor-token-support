package no.nav.tms.token.support.idporten.authentication

import com.auth0.jwk.JwkProvider
import no.nav.tms.token.support.idporten.authentication.refresh.TokenRefreshService

internal class AuthConfiguration(
        val contextPath: String,
        val accessTokenCookieName: String,
        val refreshTokenCookieName: String,
        val jwkProvider: JwkProvider,
        val clientId: String,
        val issuer: String,
        val shouldRedirect: Boolean,
        val shouldRefreshToken: Boolean,
        val tokenRefreshService: TokenRefreshService,
        val secureCookie: Boolean
)
