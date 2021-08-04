package no.nav.tms.token.support.azure.exchange.consumer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AzureTokenResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("token_type") val tokenType: String,
        @SerialName("expires_in") val expiresIn: Int,
        @SerialName("ext_expires_in") val extExpiresIn: Int
)
