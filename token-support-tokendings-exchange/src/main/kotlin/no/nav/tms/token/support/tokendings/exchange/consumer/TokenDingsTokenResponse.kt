package no.nav.tms.token.support.tokendings.exchange.consumer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class TokenDingsTokenResponse(
        @JsonProperty(value = "access_token", required = true) val accessToken: String,
        @JsonProperty(value = "issued_token_type", required = true) val issuedTokenType: String,
        @JsonProperty(value = "token_type", required = true) val tokenType: String,
        @JsonProperty(value = "expires_in", required = true) val expiresIn: String
)
