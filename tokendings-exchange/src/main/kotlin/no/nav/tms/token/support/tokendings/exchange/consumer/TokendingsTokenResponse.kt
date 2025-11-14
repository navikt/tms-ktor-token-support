package no.nav.tms.token.support.tokendings.exchange.consumer

import com.fasterxml.jackson.annotation.JsonAlias

internal data class TokendingsTokenResponse(
    @param:JsonAlias("access_token") val accessToken: String,
    @param:JsonAlias("issued_token_type") val issuedTokenType: String,
    @param:JsonAlias("token_type") val tokenType: String,
    @param:JsonAlias("expires_in") val expiresIn: Int
)
