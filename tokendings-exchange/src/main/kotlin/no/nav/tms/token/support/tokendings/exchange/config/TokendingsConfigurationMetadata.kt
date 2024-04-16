package no.nav.tms.token.support.tokendings.exchange.config

import com.fasterxml.jackson.annotation.JsonAlias

internal data class TokendingsConfigurationMetadata(
    @JsonAlias("issuer") val issuer: String,
    @JsonAlias("token_endpoint") val tokenEndpoint: String,
    @JsonAlias("jwks_uri") val jwksUri: String
)
