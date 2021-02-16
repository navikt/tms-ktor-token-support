package no.nav.tms.token.support.idporten

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OauthServerConfigurationMetadata(
        @JsonProperty(value = "issuer", required = true) val issuer: String,
        @JsonProperty(value = "token_endpoint", required = true) val tokenEndpoint: String,
        @JsonProperty(value = "jwks_uri", required = true) val jwksUri: String,
        @JsonProperty(value = "authorization_endpoint") var authorizationEndpoint: String = ""
)
