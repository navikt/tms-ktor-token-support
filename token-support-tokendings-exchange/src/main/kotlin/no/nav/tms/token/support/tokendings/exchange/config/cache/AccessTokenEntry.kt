package no.nav.tms.token.support.tokendings.exchange.config.cache

import no.nav.tms.token.support.tokendings.exchange.consumer.TokendingsTokenResponse

internal data class AccessTokenEntry(
        val accessToken: String,
        val expiresInSeconds: Long
) {
    companion object {
        fun fromResponse(response: TokendingsTokenResponse) = AccessTokenEntry (
                accessToken = response.accessToken,
                expiresInSeconds = response.expiresIn.toLong()
        )
    }
}
