package no.nav.tms.token.support.tokendings.exchange

import no.nav.tms.token.support.tokendings.exchange.consumer.TokendingsTokenResponse

internal object TokendingsResponseObjectMother {
    fun createTokendingsResponse(accessToken: String, expiresIn: Int = 300) = TokendingsTokenResponse (
            accessToken = accessToken,
            tokenType = "",
            issuedTokenType = "",
            expiresIn = expiresIn
    )
}
