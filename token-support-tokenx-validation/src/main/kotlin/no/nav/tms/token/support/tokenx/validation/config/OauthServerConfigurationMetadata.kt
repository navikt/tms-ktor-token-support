package no.nav.tms.token.support.tokenx.validation.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OauthServerConfigurationMetadata(
        @SerialName("issuer") val issuer: String,
        @SerialName("token_endpoint") val tokenEndpoint: String,
        @SerialName("jwks_uri") val jwksUri: String,
        @SerialName("authorization_endpoint") var authorizationEndpoint: String = ""
)
