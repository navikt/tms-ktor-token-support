package no.nav.tms.token.support.tokendings.exchange.config

import com.fasterxml.jackson.annotation.JsonAlias

internal data class TokendingsConfigurationMetadata(
    @param:JsonAlias("issuer") val issuer: String,
    @param:JsonAlias("token_endpoint") val tokenEndpoint: String,
    @param:JsonAlias("jwks_uri") val jwksUri: String
)
