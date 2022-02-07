package no.nav.tms.token.support.idporten.sidecar.authentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This object holds info returned from idporten's well-known-url
@Serializable
internal data class OauthServerConfigurationMetadata(
        @SerialName("issuer") val issuer: String,
        @SerialName("jwks_uri") val jwksUri: String,
)
