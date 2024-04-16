package no.nav.tms.token.support.azure.exchange.consumer

import com.fasterxml.jackson.annotation.JsonAlias

internal data class AzureTokenResponse(
    @JsonAlias("access_token") val accessToken: String,
    @JsonAlias("token_type") val tokenType: String,
    @JsonAlias("expires_in") val expiresIn: Int,
    @JsonAlias("ext_expires_in") val extExpiresIn: Int
)
