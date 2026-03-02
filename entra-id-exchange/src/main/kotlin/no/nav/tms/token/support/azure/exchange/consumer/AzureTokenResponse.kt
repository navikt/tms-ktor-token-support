package no.nav.tms.token.support.azure.exchange.consumer

import com.fasterxml.jackson.annotation.JsonAlias

internal data class AzureTokenResponse(
    @param:JsonAlias("access_token") val accessToken: String,
    @param:JsonAlias("token_type") val tokenType: String,
    @param:JsonAlias("expires_in") val expiresIn: Int,
    @param:JsonAlias("ext_expires_in") val extExpiresIn: Int
)
