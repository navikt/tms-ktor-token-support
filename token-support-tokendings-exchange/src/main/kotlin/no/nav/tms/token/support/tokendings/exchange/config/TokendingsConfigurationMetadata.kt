package no.nav.tms.token.support.tokendings.exchange.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TokendingsConfigurationMetadata(
        @SerialName("issuer") val issuer: String,
        @SerialName("token_endpoint") val tokenEndpoint: String,
        @SerialName("jwks_uri") val jwksUri: String
)
