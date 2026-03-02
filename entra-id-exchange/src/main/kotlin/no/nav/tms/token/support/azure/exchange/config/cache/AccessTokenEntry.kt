package no.nav.tms.token.support.azure.exchange.config.cache

import no.nav.tms.token.support.azure.exchange.consumer.AzureTokenResponse

internal data class AccessTokenEntry(
        val accessToken: String,
        val expiresInSeconds: Long
) {
    companion object {
        fun fromResponse(response: AzureTokenResponse) = AccessTokenEntry (
                accessToken = response.accessToken,
                expiresInSeconds = response.expiresIn.toLong()
        )
    }
}
