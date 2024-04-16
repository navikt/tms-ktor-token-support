package no.nav.tms.token.support.tokendings.exchange.consumer

import com.fasterxml.jackson.annotation.JsonAlias

internal data class TokendingsTokenResponse(
    @JsonAlias("access_token") val accessToken: String,
    @JsonAlias("issued_token_type") val issuedTokenType: String,
    @JsonAlias("token_type") val tokenType: String,
    @JsonAlias("expires_in") val expiresIn: Int
)
