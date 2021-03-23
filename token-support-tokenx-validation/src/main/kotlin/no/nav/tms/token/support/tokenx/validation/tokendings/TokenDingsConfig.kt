package no.nav.tms.token.support.tokenx.validation.tokendings

import com.auth0.jwk.JwkProvider

internal class TokenDingsConfig (
    val jwkProvider: JwkProvider,
    val clientId: String,
    val issuer: String
)
