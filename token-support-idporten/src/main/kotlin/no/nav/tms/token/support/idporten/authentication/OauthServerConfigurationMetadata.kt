package no.nav.tms.token.support.idporten.authentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This object holds info returned from idporten's well-known-url
@Serializable
internal data class OauthServerConfigurationMetadata(
        @SerialName("issuer") val issuer: String,
        @SerialName("token_endpoint") val tokenEndpoint: String,
        @SerialName("jwks_uri") val jwksUri: String,
        @SerialName("authorization_endpoint") var authorizationEndpoint: String = ""
)
