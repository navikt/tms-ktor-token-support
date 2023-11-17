package no.nav.tms.token.support.tokendings.exchange.consumer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TokendingsTokenResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("issued_token_type") val issuedTokenType: String,
        @SerialName("token_type") val tokenType: String,
        @SerialName("expires_in") val expiresIn: Int
)
