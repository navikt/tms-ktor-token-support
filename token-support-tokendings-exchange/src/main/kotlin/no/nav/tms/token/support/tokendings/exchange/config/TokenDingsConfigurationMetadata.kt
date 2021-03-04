package no.nav.tms.token.support.tokendings.exchange.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class TokenDingsConfigurationMetadata(
        @JsonProperty(value = "issuer", required = true) val issuer: String,
        @JsonProperty(value = "token_endpoint", required = true) val tokenEndpoint: String,
        @JsonProperty(value = "jwks_uri", required = true) val jwksUri: String
)
